package net.lenni0451.optconfig.utils;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

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

    public static <T> T invoke(final Method method, @Nullable final Object instance, final Object... args) {
        try {
            return (T) method.invoke(instance, args);
        } catch (Throwable t) {
            throw new RuntimeException("An error occurred while invoking the method " + method.getName() + " in class " + method.getDeclaringClass().getName(), t);
        }
    }

}
