package net.lenni0451.optconfig.serializer;

import net.lenni0451.optconfig.serializer.impl.GenericEnumSerializer;
import net.lenni0451.optconfig.serializer.impl.PassthroughTypeSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static net.lenni0451.optconfig.utils.ReflectionUtils.unsafeCast;

@SuppressWarnings("Convert2MethodRef") //For some reason the code doesn't compile if method references are used
public class TypeSerializerList<C> {

    private final Map<Class<?>, Function<C, ConfigTypeSerializer<C, ?>>> serializers;

    public TypeSerializerList() {
        this.serializers = new HashMap<>();

        this.addTypeSerializer(Enum.class, config -> new GenericEnumSerializer<>(config)); //A generic enum serializer that converts strings to enum. The names are case-insensitive
        this.addTypeSerializer(Object.class, config -> new PassthroughTypeSerializer<C>(config)); //The default type serializer if no other is found
    }

    public <T> void addTypeSerializer(final Class<T> type, final Function<C, ConfigTypeSerializer<C, T>> serializerSupplier) {
        this.serializers.put(type, unsafeCast(serializerSupplier));
    }

    public <T> ConfigTypeSerializer<C, T> getTypeSerializer(final C config, final Class<T> type) {
        Class<?> currentType = type;
        do {
            if (this.serializers.containsKey(currentType)) {
                return unsafeCast(this.serializers.get(currentType).apply(config));
            }
            currentType = currentType.getSuperclass();
        } while (currentType != null);
        return unsafeCast(this.serializers.get(Object.class).apply(config));
    }

}
