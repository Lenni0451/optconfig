package net.lenni0451.optconfig.access.impl.reflection;

import lombok.SneakyThrows;
import net.lenni0451.optconfig.access.types.ConstructorAccess;

import java.lang.reflect.Constructor;

/**
 * A default implementation of {@link ConstructorAccess} using reflection.
 */
public class ReflectionConstructorAccess implements ConstructorAccess {

    protected final Constructor<?> constructor;

    public ReflectionConstructorAccess(final Constructor<?> constructor) {
        this.constructor = constructor;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return this.constructor.getParameterTypes();
    }

    @Override
    @SneakyThrows
    public Object newInstance(Object... args) {
        this.constructor.setAccessible(true);
        return this.constructor.newInstance(args);
    }

}
