package net.lenni0451.optconfig.access.impl.methodhandles;

import net.lenni0451.optconfig.access.impl.reflection.ReflectionClassAccess;
import net.lenni0451.optconfig.access.types.ClassAccess;
import net.lenni0451.optconfig.access.types.ConstructorAccess;
import net.lenni0451.optconfig.access.types.FieldAccess;
import net.lenni0451.optconfig.access.types.MethodAccess;
import net.lenni0451.optconfig.utils.ArrayUtils;

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

    public MethodHandleClassAccess(final MethodHandles.Lookup lookup, final Class<?> clazz, final boolean reverseInnerClasses) {
        super(clazz, reverseInnerClasses);
        this.lookup = lookup;
    }

    @Override
    public ConstructorAccess[] getConstructors() {
        return ArrayUtils.map(this.clazz.getDeclaredConstructors(), constructor -> new MethodHandleConstructorAccess(this.lookup, constructor), MethodHandleConstructorAccess[]::new);
    }

    @Override
    public FieldAccess[] getFields() {
        return ArrayUtils.map(this.clazz.getDeclaredFields(), field -> new MethodHandleFieldAccess(this.lookup, field), MethodHandleFieldAccess[]::new);
    }

    @Override
    public MethodAccess[] getMethods() {
        return ArrayUtils.map(this.clazz.getDeclaredMethods(), method -> new MethodHandleMethodAccess(this.lookup, method), MethodHandleMethodAccess[]::new);
    }

    @Override
    public ClassAccess[] getInnerClasses() {
        ClassAccess[] classAccesses = ArrayUtils.map(this.clazz.getDeclaredClasses(), innerClass -> new MethodHandleClassAccess(this.lookup, innerClass), MethodHandleClassAccess[]::new);
        if (this.reverseInnerClasses) ArrayUtils.reverse(classAccesses);
        return classAccesses;
    }

}
