package net.lenni0451.optconfig.serializer;

import org.yaml.snakeyaml.nodes.Tag;

public abstract class ConfigTypeSerializer<C, T> {

    protected final C config;

    public ConfigTypeSerializer(final C config) {
        this.config = config;
    }

    /**
     * Deserializes the given yaml object to the type {@code T}.<br>
     * See {@link Tag} for the possible types.
     *
     * @param serializedObject The object to deserialize
     * @return The deserialized object
     */
    public abstract T deserialize(final Class<T> typeClass, final Object serializedObject);

    /**
     * Serializes the given object to a yaml object.<br>
     * See {@link Tag} for the possible types.
     *
     * @param object The object to serialize
     * @return The serialized object
     */
    public abstract Object serialize(final T object);

}
