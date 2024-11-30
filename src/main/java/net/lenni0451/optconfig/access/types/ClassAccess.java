package net.lenni0451.optconfig.access.types;

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * A class which can be accessed by optconfig (e.g. using reflection).
 */
public interface ClassAccess {

    /**
     * @return The class
     */
    Class<?> getClazz();

    /**
     * @return All constructors of the class
     * @see Class#getDeclaredConstructors()
     */
    ConstructorAccess[] getConstructors();

    /**
     * Get a constructor with the given parameter types.
     *
     * @param parameterTypes The parameter types of the constructor
     * @return The constructor
     * @throws IllegalArgumentException If no constructor with the given parameter types was found
     */
    default ConstructorAccess getConstructor(final Class<?>... parameterTypes) {
        ConstructorAccess constructor = this.tryGetConstructor(parameterTypes);
        if (constructor != null) return constructor;
        throw new IllegalArgumentException("No constructor found with the parameter types: " + Arrays.toString(parameterTypes));
    }

    /**
     * Try to get a constructor with the given parameter types.
     *
     * @param parameterTypes The parameter types of the constructor
     * @return The constructor or null if no constructor with the given parameter types was found
     */
    default ConstructorAccess tryGetConstructor(final Class<?>... parameterTypes) {
        for (ConstructorAccess constructor : this.getConstructors()) {
            if (Arrays.equals(constructor.getParameterTypes(), parameterTypes)) {
                return constructor;
            }
        }
        return null;
    }

    /**
     * @return All fields of the class
     * @see Class#getDeclaredFields()
     */
    FieldAccess[] getFields();

    /**
     * @return All methods of the class
     * @see Class#getDeclaredMethods()
     */
    MethodAccess[] getMethods();

    /**
     * @return All inner classes of the class
     * @see Class#getDeclaredClasses()
     */
    ClassAccess[] getInnerClasses();

    /**
     * Get an annotation of the class.
     *
     * @param annotationClass The class of the annotation
     * @param <T>             The type of the annotation
     * @return The annotation or null if the class does not have this annotation
     * @see Class#getDeclaredAnnotation(Class)
     */
    <T extends Annotation> T getAnnotation(final Class<T> annotationClass);

}
