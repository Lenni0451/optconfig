package net.lenni0451.optconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a class as a config section.<br>
 * If this annotation is missing, sections will be serialized using the default object serialization.<br>
 * <br>
 * Sections can also be used without a field in the config class (independent). {@link #name()} has to be set in this case.<br>
 * Independent sections must be an inner class of the config class and are always added to the end of the config file.<br>
 * Independent section are only supported in static config classes.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Section {

    /**
     * Get the independent name of the section.<br>
     * If this is set, the section has to be an inner class of the config class.<br>
     * <b>A field for this section must not be present in the config class.</b>
     *
     * @return The name of the section
     */
    String name() default "";

    /**
     * Get the independent description of the section.<br>
     * If this is set, {@link #name()} must be set as well.<br>
     * <b>A field for this section must not be present in the config class.</b>
     *
     * @return The description of the section
     */
    String[] description() default {};

    /**
     * Get the independent reloadable state of the section.<br>
     * If this is set, {@link #name()} must be set as well.<br>
     * <b>A field for this section must not be present in the config class.</b>
     *
     * @return The reloadable state of the section
     */
    boolean reloadable() default true;

    /**
     * Get the type of the section.<br>
     * You can use this to mix different section types in one config class.<br>
     * For example, you can have a static config class with multiple instanced sections (they can even all have the same type).
     *
     * @return The type of the section
     */
    Type type() default Type.PARENT;


    enum Type {
        /**
         * The section is loaded with the same type as the parent config class.
         */
        PARENT,
        /**
         * The section is always loaded as a static section.
         */
        STATIC,
        /**
         * The section is always loaded as an instanced section.
         */
        INSTANCED
    }

}
