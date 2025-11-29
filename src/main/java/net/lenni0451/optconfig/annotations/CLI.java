package net.lenni0451.optconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to give hints to the CLI generator about handling this option in the CLI.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CLI {

    /**
     * Give the option a custom name in the CLI.<br>
     * If not set or empty, the regular option name will be used.
     *
     * @return The custom name for the CLI option
     */
    String name() default "";

    /**
     * Give the option one or more aliases in the CLI.
     *
     * @return The aliases for the CLI option
     */
    String[] aliases() default {};

    /**
     * @return Whether to ignore this option in the CLI
     */
    boolean ignore() default false;

}
