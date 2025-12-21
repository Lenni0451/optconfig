package net.lenni0451.optconfig.exceptions;

/**
 * An exception that is thrown when a circular option dependency is detected.<br>
 * e.g. {@code A -> B -> A}<br>
 * This also works over multiple options, e.g. {@code A -> B -> C -> A}
 */
public class CircularDependencyException extends RuntimeException {

    public CircularDependencyException(final String message) {
        super(message);
    }

}
