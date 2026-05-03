package net.lenni0451.optconfig.exceptions;

import lombok.Getter;
import net.lenni0451.optconfig.cli.CLIOption;

import java.util.List;

/**
 * Exception thrown when an option is missing the value.
 */
@Getter
public class CLIMissingOptionValueException extends RuntimeException {

    private final String option;
    private final List<String> aliases;

    public CLIMissingOptionValueException(final CLIOption option) {
        super("Option " + String.join(", ", option.getNames()) + " is missing a value");
        List<String> names = option.getNames();
        this.option = names.get(0);
        this.aliases = names.subList(1, names.size());
    }

}
