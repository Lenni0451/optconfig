package net.lenni0451.optconfig.index;

import net.lenni0451.optconfig.access.ClassAccessFactory;
import net.lenni0451.optconfig.access.types.ClassAccess;
import net.lenni0451.optconfig.access.types.FieldAccess;
import net.lenni0451.optconfig.access.types.MethodAccess;
import net.lenni0451.optconfig.annotations.*;
import net.lenni0451.optconfig.annotations.internal.Migrators;
import net.lenni0451.optconfig.exceptions.InvalidValidatorException;
import net.lenni0451.optconfig.exceptions.UnknownDependencyException;
import net.lenni0451.optconfig.index.dummy.DummyDescription;
import net.lenni0451.optconfig.index.dummy.DummyFieldAccess;
import net.lenni0451.optconfig.index.dummy.DummyOption;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.ConfigOption;
import net.lenni0451.optconfig.index.types.SectionIndex;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public class ClassIndexer {

    public static SectionIndex indexClass(final ConfigType configType, final Class<?> clazz, final ClassAccessFactory classAccessFactory) {
        SectionIndex sectionIndex;
        if (clazz.getDeclaredAnnotation(OptConfig.class) != null) {
            OptConfig optConfig = clazz.getDeclaredAnnotation(OptConfig.class);
            ConfigIndex configIndex = new ConfigIndex(configType, clazz, optConfig);
            loadMigrators(clazz, configIndex);
            sectionIndex = configIndex;
        } else if (clazz.getDeclaredAnnotation(Section.class) != null) {
            sectionIndex = new SectionIndex(configType, clazz);
        } else {
            throw new IllegalArgumentException("The class " + clazz.getName() + " is not annotated with @OptConfig or @Section");
        }
        indexFields(sectionIndex, classAccessFactory);
        addInMemoryFields(sectionIndex);
        return sectionIndex;
    }

    private static void loadMigrators(final Class<?> clazz, final ConfigIndex configIndex) {
        Migrators migrators = clazz.getDeclaredAnnotation(Migrators.class);
        if (migrators != null) {
            for (Migrator migrator : migrators.value()) {
                configIndex.addMigrator(migrator.from(), migrator.to(), migrator.migrator());
            }
        }

        Migrator migrator = clazz.getDeclaredAnnotation(Migrator.class);
        if (migrator != null) {
            configIndex.addMigrator(migrator.from(), migrator.to(), migrator.migrator());
        }
    }

    private static void indexFields(final SectionIndex sectionIndex, final ClassAccessFactory classAccessFactory) {
        Class<?> clazz = sectionIndex.getClazz();
        ClassAccess classAccess = classAccessFactory.create(clazz);
        Map<String, MethodAccess> validatorMethods = new HashMap<>();
        for (MethodAccess method : classAccess.getMethods()) {
            if (!sectionIndex.getConfigType().matches(method.getModifiers())) continue;
            Validator validator = method.getAnnotation(Validator.class);
            if (validator == null) continue;
            if (method.getParameterCount() != 1) throw new InvalidValidatorException(clazz, method, "does not have exactly one parameter");
            if (!method.getReturnType().equals(method.getParameterTypes()[0])) throw new InvalidValidatorException(clazz, method, "does not return the same type as the parameter");
            for (String option : validator.value()) validatorMethods.put(option, method);
        }
        for (FieldAccess field : classAccess.getFields()) {
            if (!sectionIndex.getConfigType().matches(field.getModifiers())) continue;
            Option option = field.getAnnotation(Option.class);
            if (option == null) continue;
            Description description = field.getAnnotation(Description.class);
            NotReloadable notReloadable = field.getAnnotation(NotReloadable.class);
            TypeSerializer typeSerializer = field.getAnnotation(TypeSerializer.class);
            ConfigOption configOption = new ConfigOption(field, option, description, notReloadable, typeSerializer, validatorMethods);
            if (configOption.getName().equals(OptConfig.CONFIG_VERSION_OPTION)) {
                throw new IllegalStateException("The option name '" + OptConfig.CONFIG_VERSION_OPTION + "' is reserved for the config version");
            }
            sectionIndex.addOption(configOption);

            Section section = field.getType().getDeclaredAnnotation(Section.class);
            if (section != null) sectionIndex.addSubSection(configOption, indexClass(sectionIndex.getConfigType(), field.getType(), classAccessFactory));
        }
        if (!validatorMethods.isEmpty()) throw new InvalidValidatorException(clazz, validatorMethods.values().iterator().next(), "has no corresponding option");
        for (ConfigOption option : sectionIndex.getOptions()) {
            //Check if all dependencies are valid
            for (String dependency : option.getDependencies()) {
                if (sectionIndex.getOption(dependency) == null) throw new UnknownDependencyException(option.getName(), dependency);
            }
        }
        sectionIndex.sortOptions();
    }

    private static void addInMemoryFields(final SectionIndex sectionIndex) {
        if (sectionIndex instanceof ConfigIndex) {
            ConfigIndex configIndex = (ConfigIndex) sectionIndex;
            if (configIndex.getVersion() != OptConfig.DEFAULT_VERSION) {
                configIndex.addOption(new ConfigOption(
                        new DummyFieldAccess(OptConfig.CONFIG_VERSION_OPTION, int.class, configIndex.getVersion()),
                        new DummyOption(OptConfig.CONFIG_VERSION_OPTION),
                        new DummyDescription("The current version of the config file.", "DO NOT CHANGE THIS VALUE!", "CHANGING THIS VALUE CAN BREAK THE CONFIG FILE!"),
                        null,
                        null,
                        Collections.emptyMap()
                ));
            }
        }
    }

}
