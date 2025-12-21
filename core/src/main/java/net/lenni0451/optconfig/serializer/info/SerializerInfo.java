package net.lenni0451.optconfig.serializer.info;

import lombok.With;
import net.lenni0451.optconfig.serializer.TypeSerializerList;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

@With
public record SerializerInfo<T>(@Nullable Object configInstance, TypeSerializerList typeSerializers, Class<T> type, @Nullable Type genericType, T value) {

    /**
     * Derive a new serializer info with unchecked casts.<br>
     * <b>Use with caution!</b>
     *
     * @param type        The type
     * @param genericType The generic type
     * @param value       The value
     * @param <O>         The type of the config option
     * @return The derived serializer info
     * @see #derive(Class, Type, Object)
     */
    public <O> SerializerInfo<O> uncheckedDerive(final Class<?> type, @Nullable final Type genericType, final Object value) {
        return this.derive((Class<O>) type, genericType, (O) value);
    }

    /**
     * Derive a new serializer info.<br>
     * This creates a new serializer info with the given parameters, while keeping the config instance and type serializers the same.
     *
     * @param type        The type
     * @param genericType The generic type
     * @param value       The value
     * @param <O>         The type of the config option
     * @return The derived serializer info
     */
    public <O> SerializerInfo<O> derive(final Class<O> type, @Nullable final Type genericType, final O value) {
        return new SerializerInfo<>(this.configInstance, this.typeSerializers, type, genericType, value);
    }

}
