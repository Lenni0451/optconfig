package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.exceptions.InvalidSerializedObjectException;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.TypeSerializerList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static net.lenni0451.optconfig.utils.ReflectionUtils.unsafeCast;

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
    public Object deserialize(TypeSerializerList<C> typeSerializers, Class<Object> typeClass, Object currentValue, Object serializedObject) {
        if (serializedObject == null) return null;
        if (typeClass.isArray()) {
            if (serializedObject.getClass().isArray()) {
                int arrayLength = Array.getLength(serializedObject);
                Object newArray = Array.newInstance(typeClass.getComponentType(), arrayLength);
                for (int i = 0; i < arrayLength; i++) {
                    Object defaultValue = (currentValue == null || Array.getLength(currentValue) <= i) ? null : Array.get(currentValue, i);
                    Class<?> componentType = defaultValue == null ? typeClass.getComponentType() : defaultValue.getClass();
                    Object value = Array.get(serializedObject, i);

                    ConfigTypeSerializer<C, ?> typeSerializer = typeSerializers.get(this.config, componentType);
                    Object deserializedValue = typeSerializer.deserialize(typeSerializers, unsafeCast(componentType), unsafeCast(defaultValue), value);
                    Array.set(newArray, i, deserializedValue);
                }
                return newArray;
            } else if (serializedObject instanceof List<?>) { //YAML usually returns a list for arrays
                List<?> serializedList = (List<?>) serializedObject;
                Object newArray = Array.newInstance(typeClass.getComponentType(), serializedList.size());
                for (int i = 0; i < serializedList.size(); i++) {
                    Object defaultValue = (currentValue == null || Array.getLength(currentValue) <= i) ? null : Array.get(currentValue, i);
                    Class<?> componentType = defaultValue == null ? typeClass.getComponentType() : defaultValue.getClass();
                    Object value = serializedList.get(i);

                    ConfigTypeSerializer<C, ?> typeSerializer = typeSerializers.get(this.config, componentType);
                    Object deserializedValue = typeSerializer.deserialize(typeSerializers, unsafeCast(componentType), unsafeCast(defaultValue), value);
                    Array.set(newArray, i, deserializedValue);
                }
                return newArray;
            } else {
                throw new InvalidSerializedObjectException(List.class, serializedObject.getClass());
            }
        } else {
            return serializedObject;
        }
    }

    @Override
    public Object serialize(TypeSerializerList<C> typeSerializers, Class<Object> typeClass, Object object) {
        if (object == null) return null;
        if (typeClass.isArray()) {
            int arrayLength = Array.getLength(object);
            List<Object> serializedList = new ArrayList<>(arrayLength); //YAML treats arrays and lists the same way
            for (int i = 0; i < arrayLength; i++) {
                Object value = Array.get(object, i);
                Class<?> componentType = value == null ? typeClass.getComponentType() : value.getClass();

                ConfigTypeSerializer<C, ?> typeSerializer = typeSerializers.get(this.config, componentType);
                Object serializedValue = typeSerializer.serialize(typeSerializers, unsafeCast(componentType), unsafeCast(value));
                serializedList.add(serializedValue);
            }
            return serializedList;
        } else {
            return object;
        }
    }

}
