package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.exceptions.InvalidSerializedObjectException;
import net.lenni0451.optconfig.serializer.IConfigTypeSerializer;

import java.util.Locale;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GenericEnumSerializer implements IConfigTypeSerializer<Enum> {

    @Override
    public Enum deserialize(Class<Enum> typeClass, Object serializedObject) {
        if (!(serializedObject instanceof String)) throw new InvalidSerializedObjectException(String.class, serializedObject.getClass());
        return Enum.valueOf(typeClass, ((String) serializedObject).toUpperCase(Locale.ROOT));
    }

    @Override
    public Object serialize(Enum object) {
        return object.name();
    }

}
