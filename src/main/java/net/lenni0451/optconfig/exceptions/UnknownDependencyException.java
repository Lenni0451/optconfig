package net.lenni0451.optconfig.exceptions;

public class UnknownDependencyException extends RuntimeException {

    public UnknownDependencyException(final String option, final String dependency) {
        super("Unknown dependency '" + dependency + "' for option '" + option + "'");
    }

}
