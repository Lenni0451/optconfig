package net.lenni0451.optconfig.serializer.info;

import lombok.With;
import net.lenni0451.optconfig.serializer.TypeSerializerList;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

@With
public record SerializerInfo<T>(@Nullable Object configInstance, TypeSerializerList typeSerializers, Class<T> type, @Nullable Type genericType, T value) {

    public <O> SerializerInfo<O> uncheckedDerive(final Class<?> type, @Nullable final Type genericType, final Object value) {
        return this.derive((Class<O>) type, genericType, (O) value);
    }

    public <O> SerializerInfo<O> derive(final Class<O> type, @Nullable final Type genericType, final O value) {
        return new SerializerInfo<>(this.configInstance, this.typeSerializers, type, genericType, value);
    }

}
