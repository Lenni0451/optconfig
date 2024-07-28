package net.lenni0451.optconfig.exceptions;

public class CircularDependencyException extends RuntimeException {

    public CircularDependencyException(final String message) {
        super(message);
    }

}
