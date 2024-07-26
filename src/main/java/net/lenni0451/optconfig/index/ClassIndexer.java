package net.lenni0451.optconfig.index;

import net.lenni0451.optconfig.annotations.*;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.ConfigOption;
import net.lenni0451.optconfig.index.types.SectionIndex;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;

@ApiStatus.Internal
public class ClassIndexer {

    public static SectionIndex indexClass(final ConfigType configType, final Class<?> clazz) {
        SectionIndex sectionIndex;
        if (clazz.getDeclaredAnnotation(OptConfig.class) != null) {
            OptConfig optConfig = clazz.getDeclaredAnnotation(OptConfig.class);
            sectionIndex = new ConfigIndex(configType, clazz, optConfig.version());
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
        for (Field field : clazz.getDeclaredFields()) {
            if (!sectionIndex.getConfigType().matches(field)) continue;
            Option option = field.getDeclaredAnnotation(Option.class);
            if (option == null) continue;
            Description description = field.getDeclaredAnnotation(Description.class);
            NotReloadable notReloadable = field.getDeclaredAnnotation(NotReloadable.class);
            ConfigOption configOption = new ConfigOption(field, option.value(), new String[0]/*TODO*/, description == null ? new String[0] : description.value(), notReloadable == null);
            sectionIndex.addOption(configOption);

            Section section = field.getType().getDeclaredAnnotation(Section.class);
            if (section != null) sectionIndex.addSubSection(configOption, indexClass(sectionIndex.getConfigType(), field.getType()));
        }
    }

}
