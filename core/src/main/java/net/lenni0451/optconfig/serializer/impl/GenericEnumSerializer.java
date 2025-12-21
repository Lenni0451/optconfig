package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.exceptions.InvalidSerializedObjectException;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.info.DeserializerInfo;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;

import java.util.Locale;

/**
 * A generic serializer for enums.<br>
 * The enum deserialization is case-insensitive and can also be done via the ordinal value.
 */
@SuppressWarnings("rawtypes")
public class GenericEnumSerializer implements ConfigTypeSerializer<Enum> {

    @Override
    public Enum deserialize(DeserializerInfo<Enum> info) {
        if (info.serializedValue() instanceof Integer i) {
            return info.type().getEnumConstants()[i];
        } else if (info.serializedValue() instanceof String s) {
            return Enum.valueOf(info.type(), s.toUpperCase(Locale.ROOT));
        }
        throw new InvalidSerializedObjectException(String.class, info.serializedValue().getClass());
    }

    @Override
    public Object serialize(SerializerInfo<Enum> info) {
        return info.value().name();
    }

}
