package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.exceptions.InvalidSerializedObjectException;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.info.DeserializerInfo;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;
import net.lenni0451.optconfig.utils.ClassUtils;

import java.util.ArrayList;
import java.util.List;

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
    public List deserialize(DeserializerInfo<C, List> info) {
        if (info.serializedValue() == null) return null;
        if (!(info.serializedValue() instanceof List)) throw new InvalidSerializedObjectException(List.class, info.serializedValue().getClass());

        Class<?> listType = ClassUtils.getCollectionType(info.currentValue());
        List list = (List) info.serializedValue();
        List newList = new ArrayList(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object defaultValue = (info.currentValue() == null || info.currentValue().size() <= i) ? null : info.currentValue().get(i);
            Class<?> componentType = defaultValue == null ? listType : defaultValue.getClass();
            Object value = list.get(i);

            ConfigTypeSerializer<C, ?> typeSerializer = info.typeSerializers().get(this.config, componentType);
            newList.add(typeSerializer.deserialize(new DeserializerInfo(info.typeSerializers(), componentType, defaultValue, value)));
        }
        return newList;
    }

    @Override
    public Object serialize(SerializerInfo<C, List> info) {
        if (info.value() == null) return null;
        Class<?> listType = ClassUtils.getCollectionType(info.value());
        List newList = new ArrayList(info.value().size());
        for (Object value : info.value()) {
            Class<?> componentType = value == null ? listType : value.getClass();

            ConfigTypeSerializer<C, ?> typeSerializer = info.typeSerializers().get(this.config, componentType);
            newList.add(typeSerializer.serialize(new SerializerInfo(info.typeSerializers(), componentType, value)));
        }
        return newList;
    }

}
