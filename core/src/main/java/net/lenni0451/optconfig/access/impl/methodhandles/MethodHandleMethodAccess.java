package net.lenni0451.optconfig.access.impl.methodhandles;

import lombok.SneakyThrows;
import net.lenni0451.optconfig.access.impl.reflection.ReflectionMethodAccess;
import net.lenni0451.optconfig.access.types.MethodAccess;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * A default implementation of {@link MethodAccess} using method handles.
 */
public class MethodHandleMethodAccess extends ReflectionMethodAccess {

    private final MethodHandles.Lookup lookup;

    public MethodHandleMethodAccess(final MethodHandles.Lookup lookup, final Method method) {
        super(method);
        this.lookup = lookup;
    }

    @Override
    @SneakyThrows
    public Object invoke(Object instance, Object... args) {
        return this.lookup.unreflect(this.method).asSpreader(Object[].class, args.length).invoke(instance, args);
    }

}
