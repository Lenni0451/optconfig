package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.exceptions.InvalidSerializedObjectException;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.info.DeserializerInfo;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;
import net.lenni0451.optconfig.utils.generics.Generics;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A generic serializer for arrays.
 */
public class GenericArraySerializer implements ConfigTypeSerializer<Object> {

    @Override
    public Object deserialize(DeserializerInfo<Object> info) {
        if (!info.type().isArray()) throw new InvalidSerializedObjectException(Array.class, info.type());

        if (info.value().getClass().isArray()) {
            int arrayLength = Array.getLength(info.value());
            Object newArray = Array.newInstance(info.type().getComponentType(), arrayLength);
            Type componentGenericType = Generics.getArrayComponentGenericType(info.genericType());
            for (int i = 0; i < arrayLength; i++) {
                Object defaultValue = (info.configValue() == null || Array.getLength(info.configValue()) <= i) ? null : Array.get(info.configValue(), i);
                Class<?> componentType = defaultValue == null ? info.type().getComponentType() : defaultValue.getClass();
                Object value = Array.get(info.value(), i);

                ConfigTypeSerializer<?> typeSerializer = info.typeSerializers().get(componentType);
                Object deserializedValue = typeSerializer.deserialize(info.uncheckedDerive(componentType, componentGenericType, defaultValue, value));
                Array.set(newArray, i, deserializedValue);
            }
            return newArray;
        } else if (info.value() instanceof List<?> serializedList) { //YAML usually returns a list for arrays
            Object newArray = Array.newInstance(info.type().getComponentType(), serializedList.size());
            Type componentGenericType = Generics.getArrayComponentGenericType(info.genericType());
            for (int i = 0; i < serializedList.size(); i++) {
                Object defaultValue = (info.configValue() == null || Array.getLength(info.configValue()) <= i) ? null : Array.get(info.configValue(), i);
                Class<?> componentType = defaultValue == null ? info.type().getComponentType() : defaultValue.getClass();
                Object value = serializedList.get(i);

                ConfigTypeSerializer<?> typeSerializer = info.typeSerializers().get(componentType);
                Object deserializedValue = typeSerializer.deserialize(info.uncheckedDerive(componentType, componentGenericType, defaultValue, value));
                Array.set(newArray, i, deserializedValue);
            }
            return newArray;
        } else {
            throw new InvalidSerializedObjectException(List.class, info.value().getClass());
        }
    }

    @Override
    public Object serialize(SerializerInfo<Object> info) {
        int arrayLength = Array.getLength(info.value());
        List<Object> serializedList = new ArrayList<>(arrayLength); //YAML treats arrays and lists the same way
        Type componentGenericType = Generics.getArrayComponentGenericType(info.genericType());
        for (int i = 0; i < arrayLength; i++) {
            Object value = Array.get(info.value(), i);
            Class<?> componentType = value == null ? info.type().getComponentType() : value.getClass();

            ConfigTypeSerializer<?> typeSerializer = info.typeSerializers().get(componentType);
            Object serializedValue = typeSerializer.serialize(info.uncheckedDerive(componentType, componentGenericType, value));
            serializedList.add(serializedValue);
        }
        return serializedList;
    }

}
