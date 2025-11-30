package net.lenni0451.optconfig.serializer.info;

import lombok.With;
import net.lenni0451.optconfig.serializer.TypeSerializerList;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * Information about a deserialization process.
 *
 * @param configInstance  The config instance or null for static deserialization
 * @param typeSerializers The list of type serializers
 * @param type            The type of the config option
 * @param genericType     The generic type of the config option
 * @param currentValue    The current value of the config option
 * @param serializedValue The serialized value of the config option
 * @param <T>             The type of the config option
 */
@With
public record DeserializerInfo<T>(@Nullable Object configInstance, TypeSerializerList typeSerializers, Class<T> type, @Nullable Type genericType, T currentValue,
        Object serializedValue) {

    /**
     * Derive a new deserializer info with unchecked casts.<br>
     * <b>Use with caution!</b>
     *
     * @param type            The type
     * @param genericType     The generic type
     * @param currentValue    The current value
     * @param serializedValue The serialized value
     * @param <O>             The type of the config option
     * @return The derived deserializer info
     * @see #derive(Class, Type, Object, Object)
     */
    public <O> DeserializerInfo<O> uncheckedDerive(final Class<?> type, @Nullable final Type genericType, final Object currentValue, final Object serializedValue) {
        return this.derive((Class<O>) type, genericType, (O) currentValue, serializedValue);
    }

    /**
     * Derive a new deserializer info.<br>
     * This creates a new deserializer info with the given parameters, while keeping the config instance and type serializers the same.
     *
     * @param type            The type
     * @param genericType     The generic type
     * @param currentValue    The current value
     * @param serializedValue The serialized value
     * @param <O>             The type of the config option
     * @return The derived deserializer info
     */
    public <O> DeserializerInfo<O> derive(final Class<O> type, @Nullable final Type genericType, final O currentValue, final Object serializedValue) {
        return new DeserializerInfo<>(this.configInstance, this.typeSerializers, type, genericType, currentValue, serializedValue);
    }

}
