package net.lenni0451.optconfig.access.impl.methodhandles;

import lombok.SneakyThrows;
import net.lenni0451.optconfig.access.impl.reflection.ReflectionConstructorAccess;
import net.lenni0451.optconfig.access.types.ConstructorAccess;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

/**
 * A default implementation of {@link ConstructorAccess} using method handles.
 */
public class MethodHandleConstructorAccess extends ReflectionConstructorAccess {

    final MethodHandles.Lookup lookup;

    public MethodHandleConstructorAccess(final MethodHandles.Lookup lookup, final Constructor<?> constructor) {
        super(constructor);
        this.lookup = lookup;
    }

    @Override
    @SneakyThrows
    public Object newInstance(Object... args) {
        return this.lookup.unreflectConstructor(this.constructor).invokeWithArguments(args);
    }

}
