package net.lenni0451.optconfig.access.impl.methodhandles;

import lombok.SneakyThrows;
import net.lenni0451.optconfig.access.impl.reflection.ReflectionFieldAccess;
import net.lenni0451.optconfig.access.types.FieldAccess;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

/**
 * A default implementation of {@link FieldAccess} using method handles.
 */
public class MethodHandleFieldAccess extends ReflectionFieldAccess {

    final MethodHandles.Lookup lookup;

    public MethodHandleFieldAccess(final MethodHandles.Lookup lookup, final Field field) {
        super(field);
        this.lookup = lookup;
    }

    @Override
    @SneakyThrows
    public Object getValue(Object instance) {
        return this.lookup.unreflectGetter(this.field).invoke(instance);
    }

    @Override
    @SneakyThrows
    public void setValue(Object instance, Object value) {
        this.lookup.unreflectSetter(this.field).invoke(instance, value);
    }

}
