package net.lenni0451.optconfig.serializer;

import org.yaml.snakeyaml.nodes.Tag;

/**
 * A serializer translating between yaml objects and the correct type.
 *
 * @param <C> The type of the config instance
 * @param <T> The type to serialize
 */
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
    public T deserialize(final Object serializedObject) {
        throw new UnsupportedOperationException("You need to override this method or one of the other deserialize methods");
    }

    /**
     * Deserializes the given yaml object to the type {@code T}.<br>
     * See {@link Tag} for the possible types.
     *
     * @param typeClass        The class of the type to deserialize
     * @param serializedObject The object to deserialize
     * @return The deserialized object
     */
    public T deserialize(final Class<T> typeClass, final Object serializedObject) {
        return this.deserialize(serializedObject);
    }

    /**
     * Deserializes the given yaml object to the type {@code T}.<br>
     * See {@link Tag} for the possible types.
     *
     * @param typeClass        The class of the type to deserialize
     * @param currentValue     The last value of the option (default value on load, last value on reload)
     * @param serializedObject The object to deserialize
     * @return The deserialized object
     */
    public T deserialize(final Class<T> typeClass, final T currentValue, final Object serializedObject) {
        return this.deserialize(typeClass, serializedObject);
    }

    /**
     * Deserializes the given yaml object to the type {@code T}.<br>
     * See {@link Tag} for the possible types.
     *
     * @param typeSerializers  The list of all type serializers
     * @param typeClass        The class of the type to deserialize
     * @param currentValue     The last value of the option (default value on load, last value on reload)
     * @param serializedObject The object to deserialize
     * @return The deserialized object
     */
    public T deserialize(final TypeSerializerList<C> typeSerializers, final Class<T> typeClass, final T currentValue, final Object serializedObject) {
        return this.deserialize(typeClass, currentValue, serializedObject);
    }

    /**
     * Serializes the given object to a yaml object.<br>
     * See {@link Tag} for the possible types.
     *
     * @param object The object to serialize
     * @return The serialized object
     */
    public Object serialize(final T object) {
        throw new UnsupportedOperationException("You need to override this method or one of the other serialize methods");
    }

    /**
     * Serializes the given object to a yaml object.<br>
     * See {@link Tag} for the possible types.
     *
     * @param typeClass The class of the type to serialize
     * @param object    The object to serialize
     * @return The serialized object
     */
    public Object serialize(final Class<T> typeClass, final T object) {
        return this.serialize(object);
    }

    /**
     * Serializes the given object to a yaml object.<br>
     * See {@link Tag} for the possible types.
     *
     * @param typeSerializers The list of all type serializers
     * @param typeClass       The class of the type to serialize
     * @param object          The object to serialize
     * @return The serialized object
     */
    public Object serialize(final TypeSerializerList<C> typeSerializers, final Class<T> typeClass, final T object) {
        return this.serialize(typeClass, object);
    }

}
