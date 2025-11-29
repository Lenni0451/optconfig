package net.lenni0451.optconfig.exceptions;

import net.lenni0451.optconfig.cli.CLIOption;

import java.util.List;

public class CLIParserException extends Exception {

    public static CLIParserException valueWithoutOption(final String value) {
        return new CLIParserException("Value '" + value + "' provided without an option");
    }

    public static CLIParserException unknownOption(final String option) {
        return new CLIParserException("Unknown option: '" + option + "'");
    }

    public static CLIParserException multipleValuesForSingleValueOption(final CLIOption option, final List<String> values) {
        return new CLIParserException("Multiple values '" + String.join(", ", values) + "' provided for single-value option: '" + option.formatNameAndAliases() + "'");
    }


    private CLIParserException(final String message) {
        super(message);
    }

}
