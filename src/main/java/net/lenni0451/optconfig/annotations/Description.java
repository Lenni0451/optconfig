package net.lenni0451.optconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set a default description for the option.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {

    /**
     * @return The description of the option
     */
    String[] value();

    /**
     * Specify a generator method that will be called to generate the description.<br>
     * The generated output will be appended to the description array.<br>
     * The method must be static, take no parameters and return a string array.<br>
     * It must be in the same class as the field.
     *
     * @return The name of the generator method
     */
    String generator() default "";

}
