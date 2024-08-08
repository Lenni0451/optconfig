package net.lenni0451.optconfig.access.impl.reflection;

import net.lenni0451.optconfig.access.types.ClassAccess;
import net.lenni0451.optconfig.access.types.ConstructorAccess;
import net.lenni0451.optconfig.access.types.FieldAccess;
import net.lenni0451.optconfig.access.types.MethodAccess;

import java.util.Arrays;

/**
 * A default implementation of {@link ClassAccess} using reflection.
 */
public class ReflectionClassAccess implements ClassAccess {

    private final Class<?> clazz;

    public ReflectionClassAccess(final Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public ConstructorAccess[] getConstructors() {
        return Arrays.stream(this.clazz.getDeclaredConstructors()).map(ReflectionConstructorAccess::new).toArray(ConstructorAccess[]::new);
    }

    @Override
    public FieldAccess[] getFields() {
        return Arrays.stream(this.clazz.getDeclaredFields()).map(ReflectionFieldAccess::new).toArray(FieldAccess[]::new);
    }

    @Override
    public MethodAccess[] getMethods() {
        return Arrays.stream(this.clazz.getDeclaredMethods()).map(ReflectionMethodAccess::new).toArray(MethodAccess[]::new);
    }

}
