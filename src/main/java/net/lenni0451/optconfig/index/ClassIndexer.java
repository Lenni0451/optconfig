package net.lenni0451.optconfig.index;

import net.lenni0451.optconfig.access.ClassAccessFactory;
import net.lenni0451.optconfig.access.types.ClassAccess;
import net.lenni0451.optconfig.access.types.FieldAccess;
import net.lenni0451.optconfig.access.types.MethodAccess;
import net.lenni0451.optconfig.annotations.*;
import net.lenni0451.optconfig.annotations.internal.DummyNotReloadable;
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
        ClassAccess classAccess = classAccessFactory.create(clazz);
        return indexClass(configType, clazz, classAccess, classAccessFactory, false);
    }

    public static SectionIndex indexClass(final ConfigType configType, final Class<?> clazz, final ClassAccess classAccess, final ClassAccessFactory classAccessFactory, final boolean loadOnly) {
        SectionIndex sectionIndex;
        if (classAccess.getAnnotation(OptConfig.class) != null) {
            OptConfig optConfig = classAccess.getAnnotation(OptConfig.class);
            ConfigIndex configIndex = new ConfigIndex(configType, clazz, optConfig);
            loadMigrators(classAccess, configIndex);
            sectionIndex = configIndex;
        } else if (classAccess.getAnnotation(Section.class) != null) {
            sectionIndex = new SectionIndex(configType, clazz);
        } else {
            throw new IllegalArgumentException("The class " + clazz.getName() + " is not annotated with @OptConfig or @Section");
        }
        indexFields(sectionIndex, classAccess, classAccessFactory);
        loadSuperClasses(configType, clazz, classAccess, sectionIndex, classAccessFactory);
        if (!loadOnly) {
            sectionIndex.sortOptions();
            addInMemoryFields(sectionIndex);
        }
        return sectionIndex;
    }

    private static void loadMigrators(final ClassAccess classAccess, final ConfigIndex configIndex) {
        Migrators migrators = classAccess.getAnnotation(Migrators.class);
        if (migrators != null) {
            for (Migrator migrator : migrators.value()) {
                configIndex.addMigrator(migrator.from(), migrator.to(), migrator.migrator());
            }
        }

        Migrator migrator = classAccess.getAnnotation(Migrator.class);
        if (migrator != null) {
            configIndex.addMigrator(migrator.from(), migrator.to(), migrator.migrator());
        }
    }

    private static void indexFields(final SectionIndex sectionIndex, final ClassAccess classAccess, final ClassAccessFactory classAccessFactory) {
        Class<?> clazz = sectionIndex.getClazz();
        Map<String, MethodAccess> validatorMethods = new HashMap<>();
        for (MethodAccess method : classAccess.getMethods()) { //Index all validator method
            if (!sectionIndex.getConfigType().matches(method.getModifiers())) continue;
            Validator validator = method.getAnnotation(Validator.class);
            if (validator == null) continue;
            if (method.getParameterCount() != 1) throw new InvalidValidatorException(clazz, method, "does not have exactly one parameter");
            if (!method.getReturnType().equals(method.getParameterTypes()[0])) throw new InvalidValidatorException(clazz, method, "does not return the same type as the parameter");
            for (String option : validator.value()) validatorMethods.put(option, method);
        }
        for (FieldAccess field : classAccess.getFields()) { //Index all options
            if (!sectionIndex.getConfigType().matches(field.getModifiers())) continue;
            Option option = field.getAnnotation(Option.class);
            if (option == null) continue;
            Description description = field.getAnnotation(Description.class);
            NotReloadable notReloadable = field.getAnnotation(NotReloadable.class);
            TypeSerializer typeSerializer = field.getAnnotation(TypeSerializer.class);
            Hidden hidden = field.getAnnotation(Hidden.class);
            Order order = field.getAnnotation(Order.class);
            ConfigOption configOption = new ConfigOption(field, option, description, notReloadable, typeSerializer, hidden, order, validatorMethods, classAccess);
            if (configOption.getName().equals(OptConfig.CONFIG_VERSION_OPTION)) {
                throw new IllegalStateException("The option name '" + OptConfig.CONFIG_VERSION_OPTION + "' is reserved for the config version");
            }
            sectionIndex.addOption(configOption);

            Section section = field.getType().getDeclaredAnnotation(Section.class);
            if (section != null) {
                if (!section.name().isEmpty() || section.description().length != 0 || !section.reloadable()) {
                    throw new IllegalStateException("Sections included using fields must not have a name, description or reloadable state");
                }
                sectionIndex.addSubSection(configOption, indexClass(sectionIndex.getConfigType(), field.getType(), classAccessFactory));
            }
        }
        if (sectionIndex.getConfigType().equals(ConfigType.STATIC)) {
            for (ClassAccess innerClass : classAccess.getInnerClasses()) { //Index all independent sections
                Section section = innerClass.getAnnotation(Section.class);
                if (section == null) continue;
                if (section.name().isEmpty()) continue; //Not an independent section

                ConfigOption subSectionOption = new ConfigOption(
                        new DummyFieldAccess(section.name(), innerClass.getClazz(), null),
                        new DummyOption(section.name()),
                        new DummyDescription(section.description()),
                        section.reloadable() ? null : new DummyNotReloadable(),
                        null,
                        null,
                        null,
                        Collections.emptyMap(),
                        null
                );
                sectionIndex.addOption(subSectionOption);
                sectionIndex.addSubSection(subSectionOption, indexClass(sectionIndex.getConfigType(), innerClass.getClazz(), classAccessFactory));
            }
        }
        if (!validatorMethods.isEmpty()) throw new InvalidValidatorException(clazz, validatorMethods.values().iterator().next(), "has no corresponding option");
        for (ConfigOption option : sectionIndex.getOptions()) {
            //Check if all dependencies are valid
            for (String dependency : option.getDependencies()) {
                if (sectionIndex.getOption(dependency) == null) throw new UnknownDependencyException(option.getName(), dependency);
            }
        }
    }

    private static void addInMemoryFields(final SectionIndex sectionIndex) {
        if (sectionIndex instanceof ConfigIndex) {
            ConfigIndex configIndex = (ConfigIndex) sectionIndex;
            if (configIndex.getVersion() != OptConfig.DEFAULT_VERSION) {
                configIndex.addOption(new ConfigOption(
                        new DummyFieldAccess(OptConfig.CONFIG_VERSION_OPTION, int.class, configIndex.getVersion()),
                        new DummyOption(OptConfig.CONFIG_VERSION_OPTION),
                        new DummyDescription("The current version of the config file.", "DO NOT CHANGE THIS VALUE!", "CHANGING THIS VALUE WILL BREAK THE CONFIG FILE!"),
                        null,
                        null,
                        null,
                        null,
                        Collections.emptyMap(),
                        null
                ));
            }
        }
    }

    private static void loadSuperClasses(final ConfigType configType, final Class<?> clazz, final ClassAccess classAccess, final SectionIndex sectionIndex, final ClassAccessFactory classAccessFactory) {
        if (clazz.getDeclaredAnnotation(CheckSuperclasses.class) == null) return;
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null) {
            ClassAccess superClassAccess = classAccessFactory.create(superClass);
            if (classAccess.getAnnotation(OptConfig.class) != null && superClassAccess.getAnnotation(OptConfig.class) != null
                    || classAccess.getAnnotation(Section.class) != null && superClassAccess.getAnnotation(Section.class) != null) {
                //Load the section index of the super class but without any in memory fields (config version)
                SectionIndex superClassIndex = indexClass(configType, superClass, superClassAccess, classAccessFactory, true);
                sectionIndex.merge(superClassIndex);
                break;
            } else {
                superClass = superClass.getSuperclass();
            }
        }
    }

}
