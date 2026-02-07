package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.exceptions.InvalidSerializedObjectException;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.info.DeserializerInfo;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;
import net.lenni0451.optconfig.utils.ClassUtils;
import net.lenni0451.optconfig.utils.generics.Generics;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A generic serializer for lists.
 */
@SuppressWarnings("rawtypes")
public class GenericListSerializer implements ConfigTypeSerializer<List> {

    @Override
    public List deserialize(DeserializerInfo<List> info) {
        if (info.value() == null) return null;
        if (!(info.value() instanceof List list)) throw new InvalidSerializedObjectException(List.class, info.value().getClass());

        Type entryGenericType = Generics.getListEntryGenericType(info.genericType());
        Class<?> entryType = Generics.resolveTypeToClass(entryGenericType);
        if (entryType == null) entryType = ClassUtils.getCollectionType(info.configValue());
        List newList = new ArrayList(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object defaultValue = (info.configValue() == null || info.configValue().size() <= i) ? null : info.configValue().get(i);
            Class<?> componentType = defaultValue == null ? entryType : defaultValue.getClass();
            Object value = list.get(i);

            ConfigTypeSerializer<?> typeSerializer = info.typeSerializers().get(componentType);
            newList.add(typeSerializer.deserialize(info.uncheckedDerive(componentType, entryGenericType, defaultValue, value)));
        }
        return newList;
    }

    @Override
    public Object serialize(SerializerInfo<List> info) {
        if (info.value() == null) return null;
        Type entryGenericType = Generics.getListEntryGenericType(info.genericType());
        Class<?> entryType = Generics.resolveTypeToClass(entryGenericType);
        if (entryType == null) entryType = ClassUtils.getCollectionType(info.value());
        List newList = new ArrayList(info.value().size());
        for (Object value : info.value()) {
            Class<?> componentType = value == null ? entryType : value.getClass();

            ConfigTypeSerializer<?> typeSerializer = info.typeSerializers().get(componentType);
            newList.add(typeSerializer.serialize(info.uncheckedDerive(componentType, entryGenericType, value)));
        }
        return newList;
    }

}
