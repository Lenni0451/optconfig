package net.lenni0451.optconfig.exceptions;

public class DuplicateOptionException extends RuntimeException {

    public DuplicateOptionException(final String option) {
        super("The option '" + option + "' is already registered");
    }

}
