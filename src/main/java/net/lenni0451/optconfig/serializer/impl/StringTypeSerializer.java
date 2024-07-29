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

    public StringTypeSerializer(final C config) {
        super(config);
    }

    @Override
    public String deserialize(Class<String> typeClass, Object serializedObject) {
        if (serializedObject instanceof String) return (String) serializedObject;
        else if (serializedObject instanceof Number || serializedObject instanceof Boolean) return serializedObject.toString();
        else throw new InvalidSerializedObjectException(String.class, serializedObject.getClass());
    }

    @Override
    public Object serialize(String object) {
        return object;
    }

}
