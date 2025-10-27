package net.lenni0451.optconfig.serializer;

import net.lenni0451.optconfig.serializer.info.DeserializerInfo;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;
import org.jetbrains.annotations.ApiStatus;
import org.yaml.snakeyaml.nodes.Tag;

import javax.annotation.Nullable;

/**
 * A serializer translating between yaml objects and the correct type.<br>
 * Make sure to override <u>one</u> of the deserialize methods and <u>one</u> of the serialize methods.<br>
 * <br>
 * When implementing this class, and you don't need access to the config instance pass {@code null} to the super constructor.
 *
 * @param <C> The type of the config instance
 * @param <T> The type to serialize
 */
public abstract class ConfigTypeSerializer<C, T> {

    /**
     * The instance of the config class.<br>
     * Can be {@code null} if the config class is static or the serializer doesn't need access to the config instance.
     */
    @Nullable
    protected final C config;

    public ConfigTypeSerializer(@Nullable final C config) {
        this.config = config;
    }

    /**
     * Deserializes the given yaml object to the type {@code T}.<br>
     * See {@link Tag} for the possible types.
     *
     * @param serializedObject The object to deserialize
     * @return The deserialized object
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
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
    @Deprecated
    @ApiStatus.ScheduledForRemoval
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
    @Deprecated
    @ApiStatus.ScheduledForRemoval
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
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public T deserialize(final TypeSerializerList<C> typeSerializers, final Class<T> typeClass, final T currentValue, final Object serializedObject) {
        return this.deserialize(typeClass, currentValue, serializedObject);
    }

    /**
     * Deserializes the given yaml object to the type {@code T}.
     *
     * @param info The deserializer info
     * @return The deserialized object
     * @see Tag YAML Tag for the possible types
     */
    public T deserialize(final DeserializerInfo<C, T> info) {
        return this.deserialize(info.typeSerializers(), info.type(), info.currentValue(), info.serializedValue());
    }

    /**
     * Serializes the given object to a yaml object.<br>
     * See {@link Tag} for the possible types.
     *
     * @param object The object to serialize
     * @return The serialized object
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
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
    @Deprecated
    @ApiStatus.ScheduledForRemoval
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
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Object serialize(final TypeSerializerList<C> typeSerializers, final Class<T> typeClass, final T object) {
        return this.serialize(typeClass, object);
    }

    /**
     * Serializes the given object to a yaml object.
     *
     * @param info The serializer info
     * @return The serialized object
     * @see Tag YAML Tag for the possible types
     */
    public Object serialize(final SerializerInfo<C, T> info) {
        return this.serialize(info.typeSerializers(), info.type(), info.value());
    }

}
