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
        super("Missing required option " + String.join(", ", option.getNames()));
        List<String> names = option.getNames();
        this.option = names.get(0);
        this.aliases = names.subList(1, names.size());
    }

}
