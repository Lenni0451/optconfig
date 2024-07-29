package net.lenni0451.optconfig.exceptions;

import net.lenni0451.optconfig.annotations.OptConfig;

/**
 * An exception that is thrown when a config class is not annotated with {@link OptConfig}.
 */
public class ConfigNotAnnotatedException extends RuntimeException {

    public ConfigNotAnnotatedException(final Class<?> clazz) {
        super("The config class " + clazz.getName() + " must be annotated with @OptConfig ");
    }

}
