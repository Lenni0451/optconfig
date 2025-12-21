package net.lenni0451.optconfig.exceptions;

/**
 * An exception that is thrown when a method has a description generator but is invalid.<br>
 * A valid description generator method must be static, return a string array and have no parameters.
 */
public class InvalidDescriptionGeneratorException extends RuntimeException {

    public InvalidDescriptionGeneratorException(final Class<?> clazz, final String option, final String generatorName, final String but) {
        super("The option '" + option + "' in class '" + clazz.getName() + "' has a description generator '" + generatorName + "' but " + but);
    }

}
