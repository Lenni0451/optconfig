package net.lenni0451.optconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a class as an OptConfig class.<br>
 * This annotation is required to use the OptConfig API.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptConfig {

    int DEFAULT_VERSION = 1;
    String CONFIG_VERSION_OPTION = "config-version";

    /**
     * Get the version of the configuration.<br>
     * If a version mismatch is detected, the migrator will be called.
     * If no migrator is set, the default values will be used for new options and old options will be removed.
     *
     * @return The version of the configuration
     */
    int version() default DEFAULT_VERSION;

}
