package net.lenni0451.optconfig.index;

import net.lenni0451.optconfig.ConfigLoader;
import net.lenni0451.optconfig.access.ClassAccessFactory;
import net.lenni0451.optconfig.access.types.ClassAccess;
import net.lenni0451.optconfig.access.types.FieldAccess;
import net.lenni0451.optconfig.access.types.MethodAccess;
import net.lenni0451.optconfig.annotations.*;
import net.lenni0451.optconfig.annotations.internal.Migrators;
import net.lenni0451.optconfig.exceptions.ConfigNotAnnotatedException;
import net.lenni0451.optconfig.exceptions.EmptyConfigException;
import net.lenni0451.optconfig.exceptions.InvalidValidatorException;
import net.lenni0451.optconfig.exceptions.UnknownDependencyException;
import net.lenni0451.optconfig.index.dummy.DummyDescription;
import net.lenni0451.optconfig.index.dummy.DummyFieldAccess;
import net.lenni0451.optconfig.index.dummy.DummyNotReloadable;
import net.lenni0451.optconfig.index.dummy.DummyOption;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.ConfigOption;
import net.lenni0451.optconfig.index.types.SectionIndex;
import net.lenni0451.optconfig.utils.ReflectionUtils;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public class ClassIndexer {

    private static final Annotation[] NO_EXTRA_ANNOTATIONS = new Annotation[0];

    @SafeVarargs
    public static <C> ConfigIndex indexClassAndInit(final ConfigType configType, final ConfigLoader<C> configLoader, @Nullable final C config, final Class<? extends Annotation>... extraAnnotations) {
        SectionIndex index = ClassIndexer.indexClass(configType, configLoader.getConfigClass(), config, configLoader.getConfigOptions().getClassAccessFactory(), extraAnnotations);
        if (!(index instanceof ConfigIndex configIndex)) throw new ConfigNotAnnotatedException(configLoader.getConfigClass());
        if (index.isEmpty()) throw new EmptyConfigException(configLoader.getConfigClass());
        switch (configType) {
            case STATIC -> {
                if (config != null) {
                    throw new IllegalArgumentException("Config instance must be null for STATIC config type");
                }
            }
            case INSTANCED -> {
                if (config == null) {
                    throw new NullPointerException("Config instance cannot be null for INSTANCED config type");
                }
            }
        }
        return configIndex;
    }

    @SafeVarargs
    public static <C> SectionIndex indexClass(final ConfigType configType, final Class<C> clazz, final C config, final ClassAccessFactory classAccessFactory, final Class<? extends Annotation>... extraAnnotations) {
        ClassAccess classAccess = classAccessFactory.create(clazz);
        return indexClass(configType, clazz, config, classAccess, classAccessFactory, extraAnnotations, false);
    }

    private static SectionIndex indexClass(final ConfigType configType, final Class<?> clazz, final Object instance, final ClassAccess classAccess, final ClassAccessFactory classAccessFactory, final Class<? extends Annotation>[] extraAnnotations, final boolean loadOnly) {
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
        indexFields(sectionIndex, instance, classAccess, classAccessFactory, extraAnnotations);
        loadSuperClasses(configType, clazz, instance, classAccess, sectionIndex, classAccessFactory, extraAnnotations);
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

    private static void indexFields(final SectionIndex sectionIndex, final Object sectionInstance, final ClassAccess classAccess, final ClassAccessFactory classAccessFactory, final Class<? extends Annotation>[] extraAnnotations) {
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
            Annotation[] extra = new Annotation[extraAnnotations.length];
            for (int i = 0; i < extraAnnotations.length; i++) {
                extra[i] = field.getAnnotation(extraAnnotations[i]);
            }
            ConfigOption configOption = new ConfigOption(field, option, description, notReloadable, typeSerializer, hidden, order, extra, validatorMethods, classAccess);
            if (configOption.getName().equals(OptConfig.CONFIG_VERSION_OPTION)) {
                throw new IllegalStateException("The option name '" + OptConfig.CONFIG_VERSION_OPTION + "' is reserved for the config version");
            }
            sectionIndex.addOption(configOption);

            Section section = field.getType().getDeclaredAnnotation(Section.class);
            if (section != null) {
                if (!section.name().isEmpty() || section.description().length != 0 || !section.reloadable()) {
                    throw new IllegalStateException("Sections included using fields must not have a name, description or reloadable state");
                }
                ConfigType subSectionType = switch (section.type()) {
                    case PARENT -> sectionIndex.getConfigType();
                    case STATIC -> ConfigType.STATIC;
                    case INSTANCED -> ConfigType.INSTANCED;
                };
                Object subSectionInstance = null;
                if (subSectionType.equals(ConfigType.INSTANCED)) {
                    subSectionInstance = field.getValue(sectionInstance);
                    if (subSectionInstance == null) {
                        subSectionInstance = ReflectionUtils.instantiate(classAccessFactory, field.getType());
                        field.setValue(sectionInstance, subSectionInstance);
                    }
                }
                sectionIndex.addSubSection(configOption, indexClass(subSectionType, (Class) field.getType(), subSectionInstance, classAccessFactory));
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
                        NO_EXTRA_ANNOTATIONS,
                        Collections.emptyMap(),
                        null
                );
                sectionIndex.addOption(subSectionOption);
                sectionIndex.addSubSection(subSectionOption, indexClass(sectionIndex.getConfigType(), innerClass.getClazz(), null, classAccessFactory));
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

    private static void loadSuperClasses(final ConfigType configType, final Class<?> clazz, final Object instance, final ClassAccess classAccess, final SectionIndex sectionIndex, final ClassAccessFactory classAccessFactory, final Class<? extends Annotation>[] extraAnnotations) {
        if (clazz.getDeclaredAnnotation(CheckSuperclasses.class) == null) return;
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null) {
            ClassAccess superClassAccess = classAccessFactory.create(superClass);
            if (classAccess.getAnnotation(OptConfig.class) != null && superClassAccess.getAnnotation(OptConfig.class) != null
                    || classAccess.getAnnotation(Section.class) != null && superClassAccess.getAnnotation(Section.class) != null) {
                //Load the section index of the super class but without any in memory fields (config version)
                SectionIndex superClassIndex = indexClass(configType, superClass, instance, superClassAccess, classAccessFactory, extraAnnotations, true);
                sectionIndex.merge(superClassIndex);
                break;
            } else {
                superClass = superClass.getSuperclass();
            }
        }
    }

    private static void addInMemoryFields(final SectionIndex sectionIndex) {
        if (sectionIndex instanceof ConfigIndex configIndex) {
            if (configIndex.getVersion() != OptConfig.DEFAULT_VERSION) {
                configIndex.addOption(new ConfigOption(
                        new DummyFieldAccess(OptConfig.CONFIG_VERSION_OPTION, int.class, configIndex.getVersion()),
                        new DummyOption(OptConfig.CONFIG_VERSION_OPTION),
                        new DummyDescription("The current version of the config file.", "DO NOT CHANGE THIS VALUE!", "CHANGING THIS VALUE WILL BREAK THE CONFIG FILE!"),
                        null,
                        null,
                        null,
                        null,
                        NO_EXTRA_ANNOTATIONS,
                        Collections.emptyMap(),
                        null
                ));
            }
        }
    }

}
