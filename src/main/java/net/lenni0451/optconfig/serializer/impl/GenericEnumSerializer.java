package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.exceptions.InvalidSerializedObjectException;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;

import java.util.Locale;

/**
 * A generic serializer for enums.<br>
 * The enum deserialization is case-insensitive.
 *
 * @param <C> The type of the config instance
 */
@SuppressWarnings("rawtypes")
public class GenericEnumSerializer<C> extends ConfigTypeSerializer<C, Enum> {

    public GenericEnumSerializer(final C config) {
        super(config);
    }

    @Override
    public Enum deserialize(Class<Enum> typeClass, Object serializedObject) {
        if (!(serializedObject instanceof String)) throw new InvalidSerializedObjectException(String.class, serializedObject.getClass());
        return Enum.valueOf(typeClass, ((String) serializedObject).toUpperCase(Locale.ROOT));
    }

    @Override
    public Object serialize(Enum object) {
        return object.name();
    }

}
