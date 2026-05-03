package net.lenni0451.optconfig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Check the superclass of the annotated class for OptConfig annotations.<br>
 * This works on config classes ({@link OptConfig}) and section classes ({@link Section}).<br>
 * If fields/sections with the same name are found in the superclasses, they will be ignored.<br>
 * The superclass fields/sections are appended to the end.<br>
 * This annotation does <b>not</b> propagate to sections in the config class.<br>
 * The superclass annotation must be the same as the subclass annotation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckSuperclass {

    /**
     * Choose if the parent class annotations should be used.<br>
     * Migrators will be merged, migrators of the subclass will overwrite the migrators of the parent.<br>
     * The {@link OptConfig} annotation is completely overwritten.
     *
     * @return If the parent annotation should be used
     */
    boolean useParentAnnotation() default false;

}
