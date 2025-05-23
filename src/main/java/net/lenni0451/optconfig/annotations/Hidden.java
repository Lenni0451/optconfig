package net.lenni0451.optconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a field as hidden.<br>
 * Hidden fields will not be added to the config file automatically, but will be treated as normal options if added manually.<br>
 * If {@code rewriteConfig} is enabled, hidden fields will be removed from the config if they are set to their default value.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Hidden {
}
