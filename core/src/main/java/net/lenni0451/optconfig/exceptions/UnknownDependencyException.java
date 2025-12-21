package net.lenni0451.optconfig.exceptions;

/**
 * An exception that is thrown when an option has a dependency to an option that does not exist.
 */
public class UnknownDependencyException extends RuntimeException {

    public UnknownDependencyException(final String option, final String dependency) {
        super("Unknown dependency '" + dependency + "' for option '" + option + "'");
    }

}
