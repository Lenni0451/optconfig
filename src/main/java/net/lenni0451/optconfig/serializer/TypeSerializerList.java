package net.lenni0451.optconfig.serializer;

import net.lenni0451.optconfig.serializer.impl.*;

import java.util.*;

/**
 * A list of all available type serializers.<br>
 * If no serializer is found for the given type, the superclass of the type is checked until a serializer is found.<br>
 * The default serializer for {@link Object} is a passthrough serializer that just returns the value without any changes.
 */
public class TypeSerializerList {

    private final Map<Class<?>, ConfigTypeSerializer<?>> typeSerializers = new HashMap<>();
    private final GenericArraySerializer genericArraySerializer;
    private final GenericTypeSerializer genericTypeSerializer;

    public TypeSerializerList() {
        this.add(String.class, new StringTypeSerializer(false)); //A passthrough serializer for strings
        this.add(List.class, new GenericListSerializer()); //A generic serializer for lists
        this.add(Map.class, new GenericMapSerializer()); //A generic serializer for maps
        this.add(Enum.class, new GenericEnumSerializer()); //A generic enum serializer that converts strings to enum. The names are case-insensitive
        this.genericArraySerializer = new GenericArraySerializer();
        this.genericTypeSerializer = new GenericTypeSerializer();
    }

    /**
     * Add a new type serializer to the list.
     *
     * @param type       The type to add the serializer for
     * @param serializer The type serializer
     * @param <T>        The type of the serializer
     * @return The type serializer list
     */
    public <T> TypeSerializerList add(final Class<T> type, final ConfigTypeSerializer<T> serializer) {
        this.typeSerializers.put(type, serializer);
        return this;
    }

    /**
     * Get a type serializer for the given type.
     * The serializer may also be for a superclass or interface of the given type.<br>
     * If no serializer is found, the generic type serializer is returned.
     *
     * @param type The type to get the serializer for
     * @return The type serializer
     */
    public ConfigTypeSerializer<?> get(final Class<?> type) {
        Class<?> currentType = type;
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        do {
            if (this.typeSerializers.containsKey(currentType)) {
                return this.typeSerializers.get(currentType);
            }
            Collections.addAll(interfaces, currentType.getInterfaces());
            currentType = currentType.getSuperclass();
        } while (currentType != null);
        for (Class<?> itf : interfaces) {
            if (this.typeSerializers.containsKey(itf)) {
                return this.typeSerializers.get(itf);
            }
        }
        if (type.isArray()) {
            return this.genericArraySerializer;
        } else {
            return this.genericTypeSerializer;
        }
    }

}
