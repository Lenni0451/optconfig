package net.lenni0451.optconfig.serializer.impl;

import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;

/**
 * The default serializer that just returns the object without any changes.<br>
 * This is used when no other serializer is found for the object.<br>
 * The validation of the object is done by SnakeYAML itself.
 *
 * @param <C> The type of the config instance
 */
public class PassthroughTypeSerializer<C> extends ConfigTypeSerializer<C, Object> {

    public PassthroughTypeSerializer(final C config) {
        super(config);
    }

    @Override
    public Object deserialize(Object serializedObject) {
        return serializedObject;
    }

    @Override
    public Object serialize(Object object) {
        return object;
    }

}
