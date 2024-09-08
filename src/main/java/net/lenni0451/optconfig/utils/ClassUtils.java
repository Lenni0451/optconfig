package net.lenni0451.optconfig.utils;

import java.util.Collection;

public class ClassUtils {

    public static Class<?> getCollectionType(final Collection<?> collection) {
        if (collection == null || collection.isEmpty()) return Object.class;
        Class<?> type = null;
        for (Object o : collection) {
            if (o == null) continue;
            type = ClassUtils.getCommonType(type, o.getClass());
        }
        if (type == null) return Object.class;
        return type;
    }

    public static Class<?> getCommonType(final Class<?> type1, final Class<?> type2) {
        if (type1 == null && type2 == null) return Object.class;
        if (type1 == null) return type2;
        if (type2 == null) return type1;
        if (type1 == type2) return type1;
        if (type1.isAssignableFrom(type2)) return type1;
        if (type2.isAssignableFrom(type1)) return type2;

        Class<?> currentType = type1;
        while (currentType != null) {
            if (currentType.isAssignableFrom(type2)) return currentType;
            currentType = currentType.getSuperclass();
        }
        return Object.class;
    }

}
