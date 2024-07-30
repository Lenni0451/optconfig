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

    public static <T> T instantiate(final Class<T> clazz, final Class<?> argType, final Object arg) {
        try {
            Constructor<T> constructor = getConstructor(clazz, argType);
            constructor.setAccessible(true);
            return constructor.newInstance(arg);
        } catch (Throwable t) {
            throw new IllegalArgumentException("The class " + clazz.getName() + " must have a public constructor with " + argType.getName() + " as type", t);
        }
    }

    private static <T> Constructor<T> getConstructor(final Class<T> clazz, final Class<?> argType) {
        Class<?> currentType = argType;
        while (currentType != null) {
            try {
                return unsafeCast(clazz.getDeclaredConstructor(currentType));
            } catch (NoSuchMethodException ignored) {
            }
            currentType = currentType.getSuperclass();
        }
        throw new IllegalArgumentException("The class " + clazz.getName() + " must have a public constructor with " + argType.getName() + " as type");
    }

    public static <T> T invoke(final Method method, @Nullable final Object instance, final Object... args) {
        try {
            return (T) method.invoke(instance, args);
        } catch (Throwable t) {
            throw new RuntimeException("An error occurred while invoking the method " + method.getName() + " in class " + method.getDeclaringClass().getName(), t);
        }
    }

    @Deprecated //Not for removal, but dangerous
    public static <T> T unsafeCast(final Object o) {
        //Not really reflection, but surely fits here
        return (T) o;
    }

}
