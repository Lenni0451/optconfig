package net.lenni0451.optconfig.exceptions;

import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;

/**
 * An exception that is thrown when a serialized object is invalid.<br>
 * This exception can be used in custom {@link ConfigTypeSerializer}s.
 */
public class InvalidSerializedObjectException extends RuntimeException {

    public InvalidSerializedObjectException(final Class<?> expected, final Class<?> actual) {
        super("Invalid serialized object, expected " + expected.getName() + " but got " + actual.getName());
    }

}
