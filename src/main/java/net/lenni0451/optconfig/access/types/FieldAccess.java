package net.lenni0451.optconfig.access.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * A field which can be accessed by optconfig (e.g. using reflection).
 */
public interface FieldAccess {

    /**
     * @return The name of the field
     * @see Field#getName()
     */
    String getName();

    /**
     * @return The type of the field
     * @see Field#getType()
     */
    Class<?> getType();

    /**
     * @return The modifiers of the field
     * @see Field#getModifiers()
     */
    int getModifiers();

    /**
     * Get the value of the field from the given instance.
     *
     * @param instance The instance to get the value from
     * @return The value of the field
     * @see Field#get(Object)
     */
    Object getValue(final Object instance);

    /**
     * Get the value of the field from the given instance and cast it to the wanted type.
     *
     * @param instance The instance to get the value from
     * @param <T>      The type to cast the value to
     * @return The value of the field
     * @throws ClassCastException If the value cannot be casted to the wanted type
     */
    default <T> T castValue(final Object instance) {
        return (T) this.getValue(instance);
    }

    /**
     * Set the value of the field for the given instance.
     *
     * @param instance The instance to set the value for
     * @param value    The value to set
     * @see Field#set(Object, Object)
     */
    void setValue(final Object instance, final Object value);

    /**
     * Get an annotation of the field.
     *
     * @param annotationClass The class of the annotation
     * @param <T>             The type of the annotation
     * @return The annotation or null if the field does not have this annotation
     * @see Field#getDeclaredAnnotation(Class)
     */
    <T extends Annotation> T getAnnotation(final Class<T> annotationClass);

}
