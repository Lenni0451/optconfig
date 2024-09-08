package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.TypeSerializerList;
import net.lenni0451.optconfig.utils.ClassUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.lenni0451.optconfig.utils.ReflectionUtils.unsafeCast;

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
    public Map deserialize(TypeSerializerList<C> typeSerializers, Class<Map> typeClass, Map currentValue, Object serializedObject) {
        if (serializedObject == null) return null;
        if (!(serializedObject instanceof Map)) throw new IllegalArgumentException("Serialized object is not a map");

        Class<?> keyType = currentValue == null ? Object.class : ClassUtils.getCollectionType(currentValue.keySet());
        Class<?> valueType = currentValue == null ? Object.class : ClassUtils.getCollectionType(currentValue.values());
        Map map = (Map) serializedObject;
        Map newMap = new LinkedHashMap(map.size());
        for (Object rawEntry : map.entrySet()) {
            Map.Entry entry = (Map.Entry) rawEntry;
            Object key = entry.getKey();
            Object value = entry.getValue();
            Object defaultValue = currentValue == null ? null : currentValue.get(key);
            Class<?> valueClass = defaultValue == null ? valueType : defaultValue.getClass();

            ConfigTypeSerializer<C, ?> keySerializer = typeSerializers.get(this.config, keyType);
            ConfigTypeSerializer<C, ?> valueSerializer = typeSerializers.get(this.config, valueClass);
            newMap.put(
                    keySerializer.deserialize(typeSerializers, unsafeCast(keyType), unsafeCast(defaultValue), key),
                    valueSerializer.deserialize(typeSerializers, unsafeCast(valueClass), unsafeCast(defaultValue), value)
            );
        }
        return newMap;
    }

    @Override
    public Object serialize(TypeSerializerList<C> typeSerializers, Class<Map> typeClass, Map object) {
        if (object == null) return null;
        Class<?> keyType = ClassUtils.getCollectionType(object.keySet());
        Class<?> valueType = ClassUtils.getCollectionType(object.values());
        Map newMap = new LinkedHashMap<>();
        for (Object rawEntry : object.entrySet()) {
            Map.Entry entry = (Map.Entry) rawEntry;
            Object key = entry.getKey();
            Class<?> keyClass = key == null ? keyType : key.getClass();
            Object value = entry.getValue();
            Class<?> valueClass = value == null ? valueType : value.getClass();

            ConfigTypeSerializer<C, ?> keySerializer = typeSerializers.get(this.config, keyClass);
            ConfigTypeSerializer<C, ?> valueSerializer = typeSerializers.get(this.config, valueClass);
            newMap.put(
                    keySerializer.serialize(typeSerializers, unsafeCast(keyClass), unsafeCast(key)),
                    valueSerializer.serialize(typeSerializers, unsafeCast(valueClass), unsafeCast(value))
            );
        }
        return newMap;
    }

}
