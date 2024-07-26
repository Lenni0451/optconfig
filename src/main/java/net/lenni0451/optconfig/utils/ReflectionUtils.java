package net.lenni0451.optconfig.utils;

import java.lang.reflect.Constructor;

public class ReflectionUtils {

    public static <T> T instantiate(final Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Throwable t) {
            throw new IllegalArgumentException("The class " + clazz.getName() + " must have a public no-args constructor", t);
        }
    }

}
