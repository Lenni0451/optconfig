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
 * The default serializer that just returns the object without any changes.<br>
 * This is used when no other serializer is found for the object.<br>
 * The validation of the object is done by SnakeYAML itself.
 *
 * @param <C> The type of the config instance
 */
public class GenericTypeSerializer<C> extends ConfigTypeSerializer<C, Object> {

    public GenericTypeSerializer(final C config) {
        super(config);
    }

    @Override
    public Object deserialize(DeserializerInfo<C, Object> info) {
        if (info.serializedValue() == null) return null;
        if (info.type().isArray()) {
            if (info.serializedValue().getClass().isArray()) {
                int arrayLength = Array.getLength(info.serializedValue());
                Object newArray = Array.newInstance(info.type().getComponentType(), arrayLength);
                Type componentGenericType = Generics.getArrayComponentGenericType(info.genericType());
                for (int i = 0; i < arrayLength; i++) {
                    Object defaultValue = (info.currentValue() == null || Array.getLength(info.currentValue()) <= i) ? null : Array.get(info.currentValue(), i);
                    Class<?> componentType = defaultValue == null ? info.type().getComponentType() : defaultValue.getClass();
                    Object value = Array.get(info.serializedValue(), i);

                    ConfigTypeSerializer<C, ?> typeSerializer = info.typeSerializers().get(this.config, componentType);
                    Object deserializedValue = typeSerializer.deserialize(new DeserializerInfo(info.typeSerializers(), componentType, componentGenericType, defaultValue, value));
                    Array.set(newArray, i, deserializedValue);
                }
                return newArray;
            } else if (info.serializedValue() instanceof List<?>) { //YAML usually returns a list for arrays
                List<?> serializedList = (List<?>) info.serializedValue();
                Object newArray = Array.newInstance(info.type().getComponentType(), serializedList.size());
                Type componentGenericType = Generics.getArrayComponentGenericType(info.genericType());
                for (int i = 0; i < serializedList.size(); i++) {
                    Object defaultValue = (info.currentValue() == null || Array.getLength(info.currentValue()) <= i) ? null : Array.get(info.currentValue(), i);
                    Class<?> componentType = defaultValue == null ? info.type().getComponentType() : defaultValue.getClass();
                    Object value = serializedList.get(i);

                    ConfigTypeSerializer<C, ?> typeSerializer = info.typeSerializers().get(this.config, componentType);
                    Object deserializedValue = typeSerializer.deserialize(new DeserializerInfo(info.typeSerializers(), componentType, componentGenericType, defaultValue, value));
                    Array.set(newArray, i, deserializedValue);
                }
                return newArray;
            } else {
                throw new InvalidSerializedObjectException(List.class, info.serializedValue().getClass());
            }
        } else {
            return info.serializedValue();
        }
    }

    @Override
    public Object serialize(SerializerInfo<C, Object> info) {
        if (info.value() == null) return null;
        if (info.type().isArray()) {
            int arrayLength = Array.getLength(info.value());
            List<Object> serializedList = new ArrayList<>(arrayLength); //YAML treats arrays and lists the same way
            Type componentGenericType = Generics.getArrayComponentGenericType(info.genericType());
            for (int i = 0; i < arrayLength; i++) {
                Object value = Array.get(info.value(), i);
                Class<?> componentType = value == null ? info.type().getComponentType() : value.getClass();

                ConfigTypeSerializer<C, ?> typeSerializer = info.typeSerializers().get(this.config, componentType);
                Object serializedValue = typeSerializer.serialize(new SerializerInfo(info.typeSerializers(), componentType, componentGenericType, value));
                serializedList.add(serializedValue);
            }
            return serializedList;
        } else {
            return info.value();
        }
    }

}
