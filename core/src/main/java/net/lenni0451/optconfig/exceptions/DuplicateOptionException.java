package net.lenni0451.optconfig.exceptions;

/**
 * An exception that is thrown when two or more options with the same name are registered.
 */
public class DuplicateOptionException extends RuntimeException {

    public DuplicateOptionException(final String option) {
        super("The option '" + option + "' is already registered");
    }

}
