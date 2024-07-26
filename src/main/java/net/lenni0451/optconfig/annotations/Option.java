package net.lenni0451.optconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a field as an option.<br>
 * Fields without this annotation will be ignored by OptConfig.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {

    /**
     * Get the name of the option.<br>
     * If no name is set, the field name will be used.
     * <b>Be aware that renamers/obfuscators can change the field names and therefore the option names!</b>
     *
     * @return The name of the option
     */
    String value() default "";

}
