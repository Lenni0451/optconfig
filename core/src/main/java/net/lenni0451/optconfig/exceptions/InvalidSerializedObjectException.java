package net.lenni0451.optconfig.exceptions;

import lombok.Getter;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;

import javax.annotation.Nullable;

/**
 * An exception that is thrown when a serialized object is invalid.<br>
 * This exception can be used in custom {@link ConfigTypeSerializer}s.
 */
@Getter
public class InvalidSerializedObjectException extends RuntimeException {

    private final Class<?> expectedType;
    @Nullable
    private final Class<?> actualType;

    public InvalidSerializedObjectException(final Class<?> expected, @Nullable final Object actual) {
        this(expected, actual == null ? null : actual.getClass());
    }

    public InvalidSerializedObjectException(final Class<?> expected, @Nullable final Class<?> actual) {
        super("Invalid serialized object, expected " + expected.getName() + " but got " + (actual == null ? "null" : actual.getName()));
        this.expectedType = expected;
        this.actualType = actual;
    }

}
