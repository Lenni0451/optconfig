package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.exceptions.InvalidSerializedObjectException;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;

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
 * If `key` and `key2` are string option types, a {@link ClassCastException} will be thrown because the value is a number or boolean.
 *
 * @param <C> The type of the config instance
 */
public class StringTypeSerializer<C> extends ConfigTypeSerializer<C, String> {

    private final boolean emptyIsNull;

    /**
     * @param config      The config instance
     * @param emptyIsNull If empty strings should be deserialized as null and null serialized as empty strings
     */
    public StringTypeSerializer(final C config, final boolean emptyIsNull) {
        super(config);
        this.emptyIsNull = emptyIsNull;
    }

    @Override
    public String deserialize(Object serializedObject) {
        if (serializedObject == null) {
            return null;
        } else if (serializedObject instanceof String) {
            String s = (String) serializedObject;
            if (this.emptyIsNull && s.isEmpty()) return null;
            else return s;
        } else if (serializedObject instanceof Number || serializedObject instanceof Boolean) {
            return serializedObject.toString();
        } else {
            throw new InvalidSerializedObjectException(String.class, serializedObject.getClass());
        }
    }

    @Override
    public Object serialize(String object) {
        if (this.emptyIsNull && object == null) return "";
        else return object;
    }

}
