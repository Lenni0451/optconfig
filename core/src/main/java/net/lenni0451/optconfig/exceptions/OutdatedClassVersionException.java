package net.lenni0451.optconfig.exceptions;

import lombok.Getter;

/**
 * An exception that is thrown when the version of the config file is higher than the version of the config class.<br>
 * For example this can happen when starting an outdated version of a program with a newer config file.
 */
@Getter
public class OutdatedClassVersionException extends RuntimeException {

    private final int configVersion;
    private final int internalVersion;

    public OutdatedClassVersionException(final int configVersion, final int classVersion) {
        super("The config file version (" + configVersion + ") is higher than the version of the config class (" + classVersion + ")");
        this.configVersion = configVersion;
        this.internalVersion = classVersion;
    }

}
