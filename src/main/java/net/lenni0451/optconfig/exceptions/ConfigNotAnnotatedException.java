package net.lenni0451.optconfig.exceptions;

public class ConfigNotAnnotatedException extends RuntimeException {

    public ConfigNotAnnotatedException(final Class<?> clazz) {
        super("The config class " + clazz.getName() + " must be annotated with @OptConfig ");
    }

}
