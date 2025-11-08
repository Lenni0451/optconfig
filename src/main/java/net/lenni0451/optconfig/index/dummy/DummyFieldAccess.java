package net.lenni0451.optconfig.index.dummy;

import net.lenni0451.optconfig.access.types.FieldAccess;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

@ApiStatus.Internal
public class DummyFieldAccess implements FieldAccess {

    private final String name;
    private final Class<?> type;
    private final Object value;

    public DummyFieldAccess(final String name, final Class<?> type, final Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Class<?> getType() {
        return this.type;
    }

    @Override
    public Type getGenericType() {
        return null;
    }

    @Override
    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    @Override
    public Object getValue(Object instance) {
        return this.value;
    }

    @Override
    public void setValue(Object instance, Object value) {
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

}
