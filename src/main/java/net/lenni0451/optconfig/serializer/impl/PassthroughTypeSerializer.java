package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.serializer.IConfigTypeSerializer;

/**
 * The default serializer that just returns the object without any changes.<br>
 * This is used when no other serializer is found for the object.<br>
 * The validation of the object is done by SnakeYAML itself.
 */
public class PassthroughTypeSerializer implements IConfigTypeSerializer<Object> {

    @Override
    public Object deserialize(Object serializedObject) {
        return serializedObject;
    }

    @Override
    public Object serialize(Object object) {
        return object;
    }

}
