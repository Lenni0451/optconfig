package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.exceptions.InvalidSerializedObjectException;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.TypeSerializerList;
import net.lenni0451.optconfig.utils.ClassUtils;

import java.util.ArrayList;
import java.util.List;

import static net.lenni0451.optconfig.utils.ReflectionUtils.unsafeCast;

/**
 * A generic serializer for lists.
 *
 * @param <C> The type of the config instance
 */
@SuppressWarnings("rawtypes")
public class GenericListSerializer<C> extends ConfigTypeSerializer<C, List> {

    public GenericListSerializer(final C config) {
        super(config);
    }

    @Override
    public List deserialize(TypeSerializerList<C> typeSerializers, Class<List> typeClass, List currentValue, Object serializedObject) {
        if (serializedObject == null) return null;
        if (!(serializedObject instanceof List)) throw new InvalidSerializedObjectException(List.class, serializedObject.getClass());

        Class<?> listType = ClassUtils.getCollectionType(currentValue);
        List list = (List) serializedObject;
        List newList = new ArrayList(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object defaultValue = (currentValue == null || currentValue.size() <= i) ? null : currentValue.get(i);
            Class<?> componentType = defaultValue == null ? listType : defaultValue.getClass();
            Object value = list.get(i);

            ConfigTypeSerializer<C, ?> typeSerializer = typeSerializers.get(this.config, componentType);
            newList.add(typeSerializer.deserialize(typeSerializers, unsafeCast(componentType), unsafeCast(defaultValue), value));
        }
        return newList;
    }

    @Override
    public Object serialize(TypeSerializerList<C> typeSerializers, Class<List> typeClass, List object) {
        if (object == null) return null;
        Class<?> listType = ClassUtils.getCollectionType(object);
        List newList = new ArrayList(object.size());
        for (Object value : object) {
            Class<?> componentType = value == null ? listType : value.getClass();

            ConfigTypeSerializer<C, ?> typeSerializer = typeSerializers.get(this.config, componentType);
            newList.add(typeSerializer.serialize(typeSerializers, unsafeCast(componentType), unsafeCast(value)));
        }
        return newList;
    }

}
