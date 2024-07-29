package net.lenni0451.optconfig.exceptions;

import java.lang.reflect.Method;

/**
 * An exception that is thrown when a method is annotated with @Validator but is invalid.<br>
 * A valid validator method must have the same return type and parameter type as the field it is validating.
 */
public class InvalidValidatorException extends RuntimeException {

    public InvalidValidatorException(final Class<?> clazz, final Method method, final String but) {
        super("The method '" + method.getName() + "' in class '" + clazz.getName() + "' is annotated with @Validator but " + but);
    }

}
