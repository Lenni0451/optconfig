package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.info.DeserializerInfo;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;
import net.lenni0451.optconfig.utils.ClassUtils;
import net.lenni0451.optconfig.utils.generics.Generics;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A generic serializer for maps.
 *
 * @param <C> The type of the config instance
 */
@SuppressWarnings("rawtypes")
public class GenericMapSerializer<C> extends ConfigTypeSerializer<C, Map> {

    public GenericMapSerializer(final C config) {
        super(config);
    }

    @Override
    public Map deserialize(DeserializerInfo<C, Map> info) {
        if (info.serializedValue() == null) return null;
        if (!(info.serializedValue() instanceof Map)) throw new IllegalArgumentException("Serialized object is not a map");

        Type keyGenericType = Generics.getMapKeyGenericType(info.genericType());
        Type valueGenericType = Generics.getMapValueGenericType(info.genericType());
        Class<?> keyType = Generics.resolveTypeToClass(keyGenericType);
        Class<?> valueType = Generics.resolveTypeToClass(valueGenericType);
        if (keyType == null) keyType = info.currentValue() == null ? Object.class : ClassUtils.getCollectionType(info.currentValue().keySet());
        if (valueType == null) valueType = info.currentValue() == null ? Object.class : ClassUtils.getCollectionType(info.currentValue().values());
        Map map = (Map) info.serializedValue();
        Map newMap = new LinkedHashMap(map.size());
        for (Object rawEntry : map.entrySet()) {
            Map.Entry entry = (Map.Entry) rawEntry;
            Object key = entry.getKey();
            Object value = entry.getValue();
            Object defaultValue = info.currentValue() == null ? null : info.currentValue().get(key);
            Class<?> valueClass = defaultValue == null ? valueType : defaultValue.getClass();

            ConfigTypeSerializer<C, ?> keySerializer = info.typeSerializers().get(this.config, keyType);
            ConfigTypeSerializer<C, ?> valueSerializer = info.typeSerializers().get(this.config, valueClass);
            newMap.put(
                    keySerializer.deserialize(new DeserializerInfo(info.typeSerializers(), keyType, keyGenericType, defaultValue, key)),
                    valueSerializer.deserialize(new DeserializerInfo(info.typeSerializers(), valueClass, valueGenericType, defaultValue, value))
            );
        }
        return newMap;
    }

    @Override
    public Object serialize(SerializerInfo<C, Map> info) {
        if (info.value() == null) return null;
        Type keyGenericType = Generics.getMapKeyGenericType(info.genericType());
        Type valueGenericType = Generics.getMapValueGenericType(info.genericType());
        Class<?> keyType = ClassUtils.getCollectionType(info.value().keySet());
        Class<?> valueType = ClassUtils.getCollectionType(info.value().values());
        Map newMap = new LinkedHashMap<>();
        for (Object rawEntry : info.value().entrySet()) {
            Map.Entry entry = (Map.Entry) rawEntry;
            Object key = entry.getKey();
            Class<?> keyClass = key == null ? keyType : key.getClass();
            Object value = entry.getValue();
            Class<?> valueClass = value == null ? valueType : value.getClass();

            ConfigTypeSerializer<C, ?> keySerializer = info.typeSerializers().get(this.config, keyClass);
            ConfigTypeSerializer<C, ?> valueSerializer = info.typeSerializers().get(this.config, valueClass);
            newMap.put(
                    keySerializer.serialize(new SerializerInfo(info.typeSerializers(), keyClass, keyGenericType, key)),
                    valueSerializer.serialize(new SerializerInfo(info.typeSerializers(), valueClass, valueGenericType, value))
            );
        }
        return newMap;
    }

}
