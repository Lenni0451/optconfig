package net.lenni0451.optconfig.exceptions;

import net.lenni0451.optconfig.annotations.CLI;

import java.util.Stack;

/**
 * Exception thrown when an option is incompatible with the CLI.
 */
public class CLIIncompatibleOptionException extends RuntimeException {

    public static CLIIncompatibleOptionException invalidList(final Stack<String> path, final String name) {
        return new CLIIncompatibleOptionException("The list option '" + String.join(">", path) + ">" + name + "' contains elements that are incompatible with the CLI and cannot be used there");
    }

    public static CLIIncompatibleOptionException invalidOption(final Stack<String> path, final String name) {
        return new CLIIncompatibleOptionException("The option '" + String.join(">", path) + ">" + name + "' is incompatible with the CLI and cannot be used there");
    }


    private CLIIncompatibleOptionException(final String message) {
        super(message + ". If the option should not be used in the CLI, add the annotation @" + CLI.class.getSimpleName() + "(ignore = true) to the option");
    }

}
