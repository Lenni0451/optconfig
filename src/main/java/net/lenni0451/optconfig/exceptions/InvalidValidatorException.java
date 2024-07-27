package net.lenni0451.optconfig.exceptions;

import java.lang.reflect.Method;

public class InvalidValidatorException extends RuntimeException {

    public InvalidValidatorException(final Class<?> clazz, final Method method, final String but) {
        super("The method '" + method.getName() + "' in class '" + clazz.getName() + "' is annotated with @Validator but " + but);
    }

}
