package net.lenni0451.optconfig.access.impl.methodhandles;

import net.lenni0451.optconfig.access.impl.reflection.ReflectionClassAccess;
import net.lenni0451.optconfig.access.types.ClassAccess;
import net.lenni0451.optconfig.access.types.ConstructorAccess;
import net.lenni0451.optconfig.access.types.FieldAccess;
import net.lenni0451.optconfig.access.types.MethodAccess;

import java.lang.invoke.MethodHandles;

/**
 * A default implementation of {@link ClassAccess} using method handles.
 */
public class MethodHandleClassAccess extends ReflectionClassAccess {

    private final MethodHandles.Lookup lookup;

    public MethodHandleClassAccess(final MethodHandles.Lookup lookup, final Class<?> clazz) {
        super(clazz);
        this.lookup = lookup;
    }

    @Override
    public ConstructorAccess[] getConstructors() {
        return this.map(this.clazz.getDeclaredConstructors(), constructor -> new MethodHandleConstructorAccess(this.lookup, constructor), MethodHandleConstructorAccess[]::new);
    }

    @Override
    public FieldAccess[] getFields() {
        return this.map(this.clazz.getDeclaredFields(), field -> new MethodHandleFieldAccess(this.lookup, field), MethodHandleFieldAccess[]::new);
    }

    @Override
    public MethodAccess[] getMethods() {
        return this.map(this.clazz.getDeclaredMethods(), method -> new MethodHandleMethodAccess(this.lookup, method), MethodHandleMethodAccess[]::new);
    }

    @Override
    public ClassAccess[] getInnerClasses() {
        return this.map(this.clazz.getDeclaredClasses(), innerClass -> new MethodHandleClassAccess(this.lookup, innerClass), MethodHandleClassAccess[]::new);
    }

}
