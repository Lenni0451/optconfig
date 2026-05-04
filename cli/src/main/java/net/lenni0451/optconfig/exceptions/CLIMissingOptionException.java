package net.lenni0451.optconfig.exceptions;

import lombok.Getter;
import net.lenni0451.optconfig.cli.CLIOption;

import java.util.List;

/**
 * Exception thrown when a required option isn't present.
 */
@Getter
public class CLIMissingOptionException extends RuntimeException {

    private final String option;
    private final List<String> aliases;

    public CLIMissingOptionException(final CLIOption option) {
        super("Missing required option '" + option.name() + "' (" + String.join(", ", option.getNames()) + ")");
        this.option = option.name();
        this.aliases = option.getNames();
    }

}
