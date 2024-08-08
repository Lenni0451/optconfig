package net.lenni0451.optconfig.access.types;

import java.util.Arrays;

/**
 * A class which can be accessed by optconfig (e.g. using reflection).
 */
public interface ClassAccess {

    /**
     * @return All constructors of the class
     */
    ConstructorAccess[] getConstructors();

    /**
     * Get a constructor with the given parameter types.
     *
     * @param parameterTypes The parameter types of the constructor
     * @return The constructor
     * @throws IllegalArgumentException If no constructor with the given parameter types was found
     */
    default ConstructorAccess getConstructor(Class<?>... parameterTypes) {
        for (ConstructorAccess constructor : this.getConstructors()) {
            if (Arrays.equals(constructor.getParameterTypes(), parameterTypes)) {
                return constructor;
            }
        }
        throw new IllegalArgumentException("No constructor found with the parameter types: " + Arrays.toString(parameterTypes));
    }

    /**
     * @return All fields of the class
     */
    FieldAccess[] getFields();

    /**
     * @return All methods of the class
     */
    MethodAccess[] getMethods();

}
