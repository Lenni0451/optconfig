package net.lenni0451.optconfig.access.types;

import javax.annotation.Nullable;
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
    @Nullable
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
     * Get a method with the given name and parameter types.
     *
     * @param name           The name of the method
     * @param returnType     The return type of the method
     * @param parameterTypes The parameter types of the method
     * @return The method
     */
    default MethodAccess getMethod(final String name, final Class<?> returnType, final Class<?>... parameterTypes) {
        MethodAccess method = this.tryGetMethod(name, returnType, parameterTypes);
        if (method != null) return method;
        throw new IllegalArgumentException("No method found with the name: " + name + " and return type: " + returnType + " and the parameter types: " + Arrays.toString(parameterTypes));
    }

    /**
     * Try to get a method with the given name and parameter types.
     *
     * @param name           The name of the method
     * @param returnType     The return type of the method
     * @param parameterTypes The parameter types of the method
     * @return The method or null if no method with the given name and parameter types was found
     */
    @Nullable
    default MethodAccess tryGetMethod(final String name, final Class<?> returnType, final Class<?>... parameterTypes) {
        for (MethodAccess method : this.getMethods()) {
            if (method.getName().equals(name) && method.getReturnType().equals(returnType) && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                return method;
            }
        }
        return null;
    }

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
