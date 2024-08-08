package net.lenni0451.optconfig.access.impl.reflection;

import lombok.SneakyThrows;
import net.lenni0451.optconfig.access.types.FieldAccess;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * A default implementation of {@link FieldAccess} using reflection.
 */
public class ReflectionFieldAccess implements FieldAccess {

    protected final Field field;

    public ReflectionFieldAccess(final Field field) {
        this.field = field;
    }

    @Override
    public String getName() {
        return this.field.getName();
    }

    @Override
    public int getModifiers() {
        return this.field.getModifiers();
    }

    @Override
    public Class<?> getType() {
        return this.field.getType();
    }

    @Override
    @SneakyThrows
    public Object getValue(Object instance) {
        this.field.setAccessible(true);
        return this.field.get(instance);
    }

    @Override
    @SneakyThrows
    public void setValue(Object instance, Object value) {
        this.field.setAccessible(true);
        this.field.set(instance, value);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return this.field.getDeclaredAnnotation(annotationClass);
    }

}
