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
 */
@SuppressWarnings("rawtypes")
public class GenericMapSerializer implements ConfigTypeSerializer<Map> {

    @Override
    public Map deserialize(DeserializerInfo<Map> info) {
        if (info.serializedValue() == null) return null;
        if (!(info.serializedValue() instanceof Map map)) throw new IllegalArgumentException("Serialized object is not a map");

        Type keyGenericType = Generics.getMapKeyGenericType(info.genericType());
        Type valueGenericType = Generics.getMapValueGenericType(info.genericType());
        Class<?> keyType = Generics.resolveTypeToClass(keyGenericType);
        Class<?> valueType = Generics.resolveTypeToClass(valueGenericType);
        if (keyType == null) keyType = info.currentValue() == null ? Object.class : ClassUtils.getCollectionType(info.currentValue().keySet());
        if (valueType == null) valueType = info.currentValue() == null ? Object.class : ClassUtils.getCollectionType(info.currentValue().values());
        Map newMap = new LinkedHashMap(map.size());
        for (Object rawEntry : map.entrySet()) {
            Map.Entry entry = (Map.Entry) rawEntry;
            Object key = entry.getKey();
            Object value = entry.getValue();
            Object defaultValue = info.currentValue() == null ? null : info.currentValue().get(key);
            Class<?> valueClass = defaultValue == null ? valueType : defaultValue.getClass();

            ConfigTypeSerializer<?> keySerializer = info.typeSerializers().get(keyType);
            ConfigTypeSerializer<?> valueSerializer = info.typeSerializers().get(valueClass);
            newMap.put(
                    keySerializer.deserialize(info.uncheckedDerive(keyType, keyGenericType, defaultValue, key)),
                    valueSerializer.deserialize(info.uncheckedDerive(valueClass, valueGenericType, defaultValue, value))
            );
        }
        return newMap;
    }

    @Override
    public Object serialize(SerializerInfo<Map> info) {
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

            ConfigTypeSerializer<?> keySerializer = info.typeSerializers().get(keyClass);
            ConfigTypeSerializer<?> valueSerializer = info.typeSerializers().get(valueClass);
            newMap.put(
                    keySerializer.serialize(info.uncheckedDerive(keyClass, keyGenericType, key)),
                    valueSerializer.serialize(info.uncheckedDerive(valueClass, valueGenericType, value))
            );
        }
        return newMap;
    }

}
