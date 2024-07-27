package net.lenni0451.optconfig;

public class ConfigOptions {

    private boolean resetInvalidOptions = false;

    public boolean isResetInvalidOptions() {
        return this.resetInvalidOptions;
    }

    public ConfigOptions resetInvalidOptions(final boolean state) {
        this.resetInvalidOptions = state;
        return this;
    }

}
