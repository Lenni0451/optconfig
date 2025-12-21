package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.info.DeserializerInfo;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;

/**
 * The default serializer that just returns the object without any changes.<br>
 * This is used when no other serializer is found for the object.<br>
 * The validation of the object is done by SnakeYAML itself.
 */
public class GenericTypeSerializer implements ConfigTypeSerializer<Object> {

    @Override
    public Object deserialize(DeserializerInfo<Object> info) {
        return info.serializedValue();
    }

    @Override
    public Object serialize(SerializerInfo<Object> info) {
        return info.value();
    }

}
