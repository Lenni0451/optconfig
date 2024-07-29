package net.lenni0451.optconfig.annotations;

import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set a type serializer for an option.<br>
 * If this annotation is present the global type serializer will be ignored.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeSerializer {

    /**
     * Get the type serializer class.<br>
     * The class must have a public constructor only taking the config class <i>(not the section)</i> as parameter.
     *
     * @return The type serializer class
     */
    Class<? extends ConfigTypeSerializer<?, ?>> value();

}
