package net.lenni0451.optconfig.serializer.impl;

import lombok.RequiredArgsConstructor;
import net.lenni0451.optconfig.exceptions.InvalidSerializedObjectException;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.info.DeserializerInfo;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;

/**
 * A serializer for the String type.<br>
 * Yaml allows having strings without quotes which causes issues if the value is also a number or boolean.<br>
 * The config deserializer will throw an exception if the value is not a string, which is why this serializer is needed.<br>
 * <br>
 * Example:
 * <pre>
 *     key: 123
 *     key2: true
 *     key3: "string"
 *     key4: string
 * </pre>
 * All the values are valid strings, but only `key3` and `key4` are interpreted as strings by yaml.<br>
 * If `key` and `key2` are string option types, a {@link ClassCastException} would be thrown because the value is a number or boolean.
 */
@RequiredArgsConstructor
public class StringTypeSerializer implements ConfigTypeSerializer<String> {

    /**
     * If empty strings should be deserialized as null and null serialized as empty strings.
     */
    private final boolean emptyIsNull;

    @Override
    public String deserialize(DeserializerInfo<String> info) {
        if (info.value() == null) {
            return null;
        } else if (info.value() instanceof String s) {
            if (this.emptyIsNull && s.isEmpty()) return null;
            else return s;
        } else if (info.value() instanceof Number || info.value() instanceof Boolean) {
            return info.value().toString();
        } else {
            throw new InvalidSerializedObjectException(String.class, info.value().getClass());
        }
    }

    @Override
    public Object serialize(SerializerInfo<String> info) {
        if (this.emptyIsNull && info.value() == null) {
            return "";
        } else {
            return info.value();
        }
    }

}
