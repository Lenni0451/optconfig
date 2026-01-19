package net.lenni0451.optconfig.annotations.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to give a custom name to a CLI option.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CLIName {

    /**
     * @return The custom name for the CLI option
     */
    String value();

}
