package net.lenni0451.optconfig.access.impl.reflection;

import lombok.SneakyThrows;
import net.lenni0451.optconfig.access.types.MethodAccess;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * A default implementation of {@link MethodAccess} using reflection.
 */
public class ReflectionMethodAccess implements MethodAccess {

    protected final Method method;

    public ReflectionMethodAccess(final Method method) {
        this.method = method;
    }

    @Override
    public String getName() {
        return this.method.getName();
    }

    @Override
    public int getModifiers() {
        return this.method.getModifiers();
    }

    @Override
    public int getParameterCount() {
        return this.method.getParameterCount();
    }

    @Override
    public Class<?> getReturnType() {
        return this.method.getReturnType();
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return this.method.getParameterTypes();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return this.method.getDeclaredAnnotation(annotationClass);
    }

    @Override
    @SneakyThrows
    public Object invoke(Object instance, Object... args) {
        this.method.setAccessible(true);
        return this.method.invoke(instance, args);
    }

}
