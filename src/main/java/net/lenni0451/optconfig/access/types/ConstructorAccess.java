package net.lenni0451.optconfig.access.types;

import java.lang.reflect.Constructor;

/**
 * A constructor which can be accessed by optconfig (e.g. using reflection).
 */
public interface ConstructorAccess {

    /**
     * @return The parameter types of the constructor
     * @see Constructor#getParameterTypes()
     */
    Class<?>[] getParameterTypes();

    /**
     * Create a new instance of the target class with the given arguments.
     *
     * @param args The arguments to pass to the constructor
     * @return The new instance of the target class
     * @see Constructor#newInstance(Object...)
     */
    Object newInstance(Object... args);

    /**
     * Create a new instance of the target class with the given arguments and cast it to the wanted type.
     *
     * @param args The arguments to pass to the constructor
     * @param <T>  The type to cast the instance to
     * @return The new instance of the target class
     * @throws ClassCastException If the instance cannot be casted to the wanted type
     */
    default <T> T castInstance(Object... args) {
        return (T) this.newInstance(args);
    }

}
