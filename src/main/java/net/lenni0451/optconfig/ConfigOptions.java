package net.lenni0451.optconfig;

import lombok.Getter;

@Getter
public class ConfigOptions {

    private boolean resetInvalidOptions = false;
    private boolean removeUnknownOptions = true;
    private boolean addMissingOptions = true;

    public ConfigOptions setResetInvalidOptions(final boolean resetInvalidOptions) {
        this.resetInvalidOptions = resetInvalidOptions;
        return this;
    }

    public ConfigOptions setRemoveUnknownOptions(final boolean removeUnknownOptions) {
        this.removeUnknownOptions = removeUnknownOptions;
        return this;
    }

    public ConfigOptions setAddMissingOptions(final boolean addMissingOptions) {
        this.addMissingOptions = addMissingOptions;
        return this;
    }

}
