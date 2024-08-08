package net.lenni0451.optconfig.access.impl.reflection;

import net.lenni0451.optconfig.access.types.ClassAccess;
import net.lenni0451.optconfig.access.types.ConstructorAccess;
import net.lenni0451.optconfig.access.types.FieldAccess;
import net.lenni0451.optconfig.access.types.MethodAccess;
import net.lenni0451.optconfig.utils.ArrayUtils;

import java.lang.annotation.Annotation;

/**
 * A default implementation of {@link ClassAccess} using reflection.
 */
public class ReflectionClassAccess implements ClassAccess {

    protected final Class<?> clazz;
    protected boolean reverseInnerClasses;

    public ReflectionClassAccess(final Class<?> clazz) {
        this(clazz, false);
    }

    public ReflectionClassAccess(final Class<?> clazz, final boolean reverseInnerClasses) {
        this.clazz = clazz;
        this.reverseInnerClasses = reverseInnerClasses;
    }

    @Override
    public Class<?> getClazz() {
        return this.clazz;
    }

    @Override
    public ConstructorAccess[] getConstructors() {
        return ArrayUtils.map(this.clazz.getDeclaredConstructors(), ReflectionConstructorAccess::new, ReflectionConstructorAccess[]::new);
    }

    @Override
    public FieldAccess[] getFields() {
        return ArrayUtils.map(this.clazz.getDeclaredFields(), ReflectionFieldAccess::new, ReflectionFieldAccess[]::new);
    }

    @Override
    public MethodAccess[] getMethods() {
        return ArrayUtils.map(this.clazz.getDeclaredMethods(), ReflectionMethodAccess::new, ReflectionMethodAccess[]::new);
    }

    @Override
    public ClassAccess[] getInnerClasses() {
        ClassAccess[] classAccesses = ArrayUtils.map(this.clazz.getDeclaredClasses(), ReflectionClassAccess::new, ReflectionClassAccess[]::new);
        if (this.reverseInnerClasses) ArrayUtils.reverse(classAccesses);
        return classAccesses;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return this.clazz.getDeclaredAnnotation(annotationClass);
    }

}
