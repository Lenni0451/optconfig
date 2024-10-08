package net.lenni0451.optconfig.serializer;

import net.lenni0451.optconfig.serializer.impl.*;

import java.util.*;
import java.util.function.Function;

import static net.lenni0451.optconfig.utils.ReflectionUtils.unsafeCast;

/**
 * A list of all available type serializers.<br>
 * If no serializer is found for the given type, the superclass of the type is checked until a serializer is found.<br>
 * The default serializer for {@link Object} is a passthrough serializer that just returns the value without any changes.
 *
 * @param <C> The type of the config instance
 */
@SuppressWarnings("Convert2MethodRef") //For some reason the code doesn't compile if method references are used
public class TypeSerializerList<C> {

    private final Map<Class<?>, Function<C, ConfigTypeSerializer<C, ?>>> serializers;

    public TypeSerializerList() {
        this.serializers = new HashMap<>();

        this.add(String.class, config -> new StringTypeSerializer<>(config, false)); //A passthrough serializer for strings
        this.add(List.class, config -> new GenericListSerializer<>(config)); //A generic serializer for lists
        this.add(Map.class, config -> new GenericMapSerializer<>(config)); //A generic serializer for maps
        this.add(Enum.class, config -> new GenericEnumSerializer<>(config)); //A generic enum serializer that converts strings to enum. The names are case-insensitive
    }

    public <T> void add(final Class<T> type, final Function<C, ConfigTypeSerializer<C, T>> serializerSupplier) {
        this.serializers.put(type, unsafeCast(serializerSupplier));
    }

    public <T> ConfigTypeSerializer<C, T> get(final C config, final Class<T> type) {
        Class<?> currentType = type;
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        do {
            if (this.serializers.containsKey(currentType)) {
                return unsafeCast(this.serializers.get(currentType).apply(config));
            }
            Collections.addAll(interfaces, currentType.getInterfaces());
            currentType = currentType.getSuperclass();
        } while (currentType != null);
        for (Class<?> itf : interfaces) {
            if (this.serializers.containsKey(itf)) {
                return unsafeCast(this.serializers.get(itf).apply(config));
            }
        }
        return unsafeCast(new GenericTypeSerializer<>(config));
    }

}
