package net.lenni0451.optconfig.access.impl.methodhandles;

import net.lenni0451.optconfig.access.types.ClassAccess;
import net.lenni0451.optconfig.access.types.ConstructorAccess;
import net.lenni0451.optconfig.access.types.FieldAccess;
import net.lenni0451.optconfig.access.types.MethodAccess;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

/**
 * A default implementation of {@link ClassAccess} using method handles.
 */
public class MethodHandleClassAccess implements ClassAccess {

    private final MethodHandles.Lookup lookup;
    private final Class<?> clazz;

    public MethodHandleClassAccess(final MethodHandles.Lookup lookup, final Class<?> clazz) {
        this.lookup = lookup;
        this.clazz = clazz;
    }

    @Override
    public ConstructorAccess[] getConstructors() {
        return Arrays.stream(this.clazz.getDeclaredConstructors()).map(constructor -> new MethodHandleConstructorAccess(this.lookup, constructor)).toArray(ConstructorAccess[]::new);
    }

    @Override
    public FieldAccess[] getFields() {
        return Arrays.stream(this.clazz.getDeclaredFields()).map(field -> new MethodHandleFieldAccess(this.lookup, field)).toArray(FieldAccess[]::new);
    }

    @Override
    public MethodAccess[] getMethods() {
        return Arrays.stream(this.clazz.getDeclaredMethods()).map(method -> new MethodHandleMethodAccess(this.lookup, method)).toArray(MethodAccess[]::new);
    }

}
