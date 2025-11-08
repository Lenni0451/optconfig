package net.lenni0451.optconfig.serializer;

import net.lenni0451.optconfig.serializer.info.DeserializerInfo;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * A serializer translating between yaml objects and the correct type.
 *
 * @param <T> The type to serialize
 */
public interface ConfigTypeSerializer<T> {

    /**
     * Deserializes the given yaml object to the type {@code T}.
     *
     * @param info The deserializer info
     * @return The deserialized object
     * @see Tag YAML Tag for the possible types
     */
    T deserialize(final DeserializerInfo<T> info);

    /**
     * Serializes the given object to a yaml object.
     *
     * @param info The serializer info
     * @return The serialized object
     * @see Tag YAML Tag for the possible types
     */
    Object serialize(final SerializerInfo<T> info);

}
