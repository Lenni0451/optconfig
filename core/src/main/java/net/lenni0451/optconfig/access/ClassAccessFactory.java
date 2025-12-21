package net.lenni0451.optconfig.access;

import net.lenni0451.optconfig.access.types.ClassAccess;

/**
 * A factory to create {@link ClassAccess} instances for a specific class.<br>
 * This is required if reflection access to the config class is not available by default.
 */
@FunctionalInterface
public interface ClassAccessFactory {

    /**
     * Create a new {@link ClassAccess} instance for the given class.
     *
     * @param clazz The class to create the access for
     * @return The created access instance
     */
    ClassAccess create(final Class<?> clazz);

}
