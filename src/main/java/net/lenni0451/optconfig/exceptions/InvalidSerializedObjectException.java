package net.lenni0451.optconfig.exceptions;

public class InvalidSerializedObjectException extends RuntimeException {

    public InvalidSerializedObjectException(final Class<?> expected, final Class<?> actual) {
        super("Invalid serialized object, expected " + expected.getName() + " but got " + actual.getName());
    }

}
