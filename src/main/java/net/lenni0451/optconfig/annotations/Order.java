package net.lenni0451.optconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Give a field a custom order in the config file.<br>
 * By default, fields are ordered by their appearance in the class.<br>
 * The order applied in ascending order, lower indexes will shift the higher indexes down.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {

    /**
     * The new index of the field.<br>
     * {@code 0} makes the field the first in the config file.<br>
     * {@link Integer#MAX_VALUE} makes the field the last in the config file.<br>
     * If multiple fields have the same index, the last one will be placed first.<br>
     * Indexes larger than the amount of fields will be limited to the last index. If the index is negative, it will be set to {@code 0}.
     *
     * @return The new index of the field
     */
    int value();

}
