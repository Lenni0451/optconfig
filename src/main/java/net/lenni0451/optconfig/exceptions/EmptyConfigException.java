package net.lenni0451.optconfig.exceptions;

/**
 * An exception that is thrown when a config class does not contain any options.<br>
 * This can also happen when trying to register an instanced config with a static config class.
 */
public class EmptyConfigException extends RuntimeException {

    public EmptyConfigException(final Class<?> clazz) {
        super("The config class " + clazz.getName() + " does not contain any options");
    }

}
