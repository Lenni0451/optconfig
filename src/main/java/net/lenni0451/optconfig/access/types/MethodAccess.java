package net.lenni0451.optconfig.access.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * A method which can be accessed by optconfig (e.g. using reflection).
 */
public interface MethodAccess {

    /**
     * @return The name of the method
     * @see Method#getName()
     */
    String getName();

    /**
     * @return The modifiers of the method
     * @see Method#getModifiers()
     */
    int getModifiers();

    /**
     * @return The number of parameters of the method
     * @see Method#getParameterCount()
     */
    int getParameterCount();

    /**
     * @return The return type of the method
     * @see Method#getReturnType()
     */
    Class<?> getReturnType();

    /**
     * @return The types of the parameters of the method
     * @see Method#getParameterTypes()
     */
    Class<?>[] getParameterTypes();

    /**
     * Get an annotation of the method.
     *
     * @param annotationClass The class of the annotation
     * @param <T>             The type of the annotation
     * @return The annotation or null if the method does not have this annotation
     * @see Method#getDeclaredAnnotation(Class)
     */
    <T extends Annotation> T getAnnotation(final Class<T> annotationClass);

    /**
     * Invoke the method with the given instance and arguments.
     *
     * @param instance The instance to invoke the method on
     * @param args     The arguments to pass to the method
     * @return The return value of the method
     * @see Method#invoke(Object, Object...)
     */
    Object invoke(final Object instance, final Object... args);

}
