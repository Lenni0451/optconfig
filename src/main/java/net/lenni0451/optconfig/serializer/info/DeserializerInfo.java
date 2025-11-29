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

    public <O> DeserializerInfo<O> uncheckedDerive(final Class<?> type, @Nullable final Type genericType, final Object currentValue, final Object serializedValue) {
        return this.derive((Class<O>) type, genericType, (O) currentValue, serializedValue);
    }

    public <O> DeserializerInfo<O> derive(final Class<O> type, @Nullable final Type genericType, final O currentValue, final Object serializedValue) {
        return new DeserializerInfo<>(this.configInstance, this.typeSerializers, type, genericType, currentValue, serializedValue);
    }

}
