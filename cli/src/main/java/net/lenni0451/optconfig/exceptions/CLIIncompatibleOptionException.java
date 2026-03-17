package net.lenni0451.optconfig.exceptions;

import net.lenni0451.optconfig.annotations.cli.CLIIgnore;

import java.util.Stack;

/**
 * Exception thrown when an option is incompatible with the CLI.
 */
public class CLIIncompatibleOptionException extends RuntimeException {

    public static CLIIncompatibleOptionException invalidList(final Stack<String> path, final String name) {
        return new CLIIncompatibleOptionException("The list option '" + formatPathAndName(path, name) + "' contains elements that are incompatible with the CLI and cannot be used there");
    }

    public static CLIIncompatibleOptionException invalidMap(final Stack<String> path, final String name) {
        return new CLIIncompatibleOptionException("The map option '" + formatPathAndName(path, name) + "' contains keys or values that are incompatible with the CLI and cannot be used there");
    }

    private static String formatPathAndName(final Stack<String> path, final String name) {
        if (path.isEmpty()) return name;
        return String.join(">", path) + ">" + name;
    }


    private CLIIncompatibleOptionException(final String message) {
        super(message + ". If the option should not be used in the CLI, add the annotation @" + CLIIgnore.class.getSimpleName() + " to the option");
    }

}
