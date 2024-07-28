package net.lenni0451.optconfig.exceptions;

public class EmptyConfigException extends RuntimeException {

    public EmptyConfigException(final Class<?> clazz) {
        super("The config class " + clazz.getName() + " does not contain any options");
    }

}
