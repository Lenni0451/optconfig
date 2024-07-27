package net.lenni0451.optconfig.exceptions;

public class OutdatedClassVersionException extends RuntimeException {

    private final int configVersion;
    private final int internalVersion;

    public OutdatedClassVersionException(final int configVersion, final int classVersion) {
        super("The config file version (" + configVersion + ") is higher than the version of the config class (" + classVersion + ")");
        this.configVersion = configVersion;
        this.internalVersion = classVersion;
    }

    public int getConfigVersion() {
        return this.configVersion;
    }

    public int getInternalVersion() {
        return this.internalVersion;
    }

}
