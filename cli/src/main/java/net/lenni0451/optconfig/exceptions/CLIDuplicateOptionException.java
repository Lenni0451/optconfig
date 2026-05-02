package net.lenni0451.optconfig.exceptions;

import lombok.Getter;

import java.util.Set;

@Getter
public class CLIDuplicateOptionException extends RuntimeException {

    private final Set<String> duplicates;

    public CLIDuplicateOptionException(final Set<String> duplicates) {
        super("Duplicate options found. Duplicates: " + String.join(", ", duplicates));
        this.duplicates = duplicates;
    }

}
