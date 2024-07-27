package net.lenni0451.optconfig.index;

import net.lenni0451.optconfig.annotations.*;
import net.lenni0451.optconfig.annotations.internal.Migrators;
import net.lenni0451.optconfig.exceptions.InvalidValidatorException;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.ConfigOption;
import net.lenni0451.optconfig.index.types.SectionIndex;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public class ClassIndexer {

    public static SectionIndex indexClass(final ConfigType configType, final Class<?> clazz) {
        SectionIndex sectionIndex;
        if (clazz.getDeclaredAnnotation(OptConfig.class) != null) {
            OptConfig optConfig = clazz.getDeclaredAnnotation(OptConfig.class);
            Migrators migrators = clazz.getDeclaredAnnotation(Migrators.class);
            ConfigIndex configIndex = new ConfigIndex(configType, clazz, optConfig.version());
            for (Migrator migrator : migrators.value()) configIndex.addMigrator(migrator.from(), migrator.to(), migrator.migrator());
            sectionIndex = configIndex;
        } else if (clazz.getDeclaredAnnotation(Section.class) != null) {
            sectionIndex = new SectionIndex(configType, clazz);
        } else {
            throw new IllegalArgumentException("The class " + clazz.getName() + " is not annotated with @OptConfig or @Section");
        }
        indexFields(sectionIndex);
        return sectionIndex;
    }

    private static void indexFields(final SectionIndex sectionIndex) {
        Class<?> clazz = sectionIndex.getClazz();
        Map<String, Method> validatorMethods = new HashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!sectionIndex.getConfigType().matches(method.getModifiers())) continue;
            Validator validator = method.getDeclaredAnnotation(Validator.class);
            if (validator == null) continue;
            method.setAccessible(true);
            if (method.getParameterCount() != 1) throw new InvalidValidatorException(clazz, method, "does not have exactly one parameter");
            if (!method.getReturnType().equals(method.getParameterTypes()[0])) throw new InvalidValidatorException(clazz, method, "does not return the same type as the parameter");
            validatorMethods.put(validator.value(), method);
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (!sectionIndex.getConfigType().matches(field.getModifiers())) continue;
            Option option = field.getDeclaredAnnotation(Option.class);
            if (option == null) continue;
            field.setAccessible(true);
            Description description = field.getDeclaredAnnotation(Description.class);
            NotReloadable notReloadable = field.getDeclaredAnnotation(NotReloadable.class);
            ConfigOption configOption = new ConfigOption(field, option.value(), description == null ? new String[0] : description.value(), notReloadable == null, validatorMethods);
            if (configOption.getName().equals(OptConfig.CONFIG_VERSION_OPTION)) {
                throw new IllegalStateException("The option name '" + OptConfig.CONFIG_VERSION_OPTION + "' is reserved for the config version");
            }
            sectionIndex.addOption(configOption);

            Section section = field.getType().getDeclaredAnnotation(Section.class);
            if (section != null) sectionIndex.addSubSection(configOption, indexClass(sectionIndex.getConfigType(), field.getType()));
        }
    }

}
