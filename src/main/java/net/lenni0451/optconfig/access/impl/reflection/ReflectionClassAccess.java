package net.lenni0451.optconfig.access.impl.reflection;

import net.lenni0451.optconfig.access.types.ClassAccess;
import net.lenni0451.optconfig.access.types.ConstructorAccess;
import net.lenni0451.optconfig.access.types.FieldAccess;
import net.lenni0451.optconfig.access.types.MethodAccess;

import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * A default implementation of {@link ClassAccess} using reflection.
 */
public class ReflectionClassAccess implements ClassAccess {

    protected final Class<?> clazz;

    public ReflectionClassAccess(final Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<?> getClazz() {
        return this.clazz;
    }

    @Override
    public ConstructorAccess[] getConstructors() {
        return this.map(this.clazz.getDeclaredConstructors(), ReflectionConstructorAccess::new, ReflectionConstructorAccess[]::new);
    }

    @Override
    public FieldAccess[] getFields() {
        return this.map(this.clazz.getDeclaredFields(), ReflectionFieldAccess::new, ReflectionFieldAccess[]::new);
    }

    @Override
    public MethodAccess[] getMethods() {
        return this.map(this.clazz.getDeclaredMethods(), ReflectionMethodAccess::new, ReflectionMethodAccess[]::new);
    }

    @Override
    public ClassAccess[] getInnerClasses() {
        return this.map(this.clazz.getDeclaredClasses(), ReflectionClassAccess::new, ReflectionClassAccess[]::new);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return this.clazz.getDeclaredAnnotation(annotationClass);
    }

    protected <I, O> O[] map(final I[] input, final Function<I, O> mapper, IntFunction<O[]> arrayCreator) {
        O[] output = arrayCreator.apply(input.length);
        for (int i = 0; i < input.length; i++) output[i] = mapper.apply(input[i]);
        return output;
    }

}
