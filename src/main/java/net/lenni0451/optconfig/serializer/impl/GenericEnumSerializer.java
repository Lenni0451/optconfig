package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.exceptions.InvalidSerializedObjectException;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.info.DeserializerInfo;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;

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
    public Enum deserialize(DeserializerInfo<C, Enum> info) {
        if (!(info.serializedValue() instanceof String)) {
            throw new InvalidSerializedObjectException(String.class, info.serializedValue().getClass());
        }
        return Enum.valueOf(info.type(), ((String) info.serializedValue()).toUpperCase(Locale.ROOT));
    }

    @Override
    public Object serialize(SerializerInfo<C, Enum> info) {
        return info.value().name();
    }

}
