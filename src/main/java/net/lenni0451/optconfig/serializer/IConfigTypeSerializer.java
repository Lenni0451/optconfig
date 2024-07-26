package net.lenni0451.optconfig.serializer;

import org.yaml.snakeyaml.nodes.Tag;

public interface IConfigTypeSerializer<T> {

    /**
     * Deserializes the given yaml object to the type {@code T}.<br>
     * See {@link Tag} for the possible types.
     *
     * @param serializedObject The object to deserialize
     * @return The deserialized object
     */
    T deserialize(final Object serializedObject);

    /**
     * Serializes the given object to a yaml object.<br>
     * See {@link Tag} for the possible types.
     *
     * @param object The object to serialize
     * @return The serialized object
     */
    Object serialize(final T object);

}
