package net.lenni0451.optconfig;

import lombok.Getter;

@Getter
public class ConfigOptions {

    private boolean resetInvalidOptions = false;
    private boolean removeUnknownOptions = true;
    private boolean addMissingOptions = true;
    private int commentSpacing = 0;

    public ConfigOptions setResetInvalidOptions(final boolean value) {
        this.resetInvalidOptions = value;
        return this;
    }

    public ConfigOptions setRemoveUnknownOptions(final boolean value) {
        this.removeUnknownOptions = value;
        return this;
    }

    public ConfigOptions setAddMissingOptions(final boolean value) {
        this.addMissingOptions = value;
        return this;
    }

    public ConfigOptions setCommentSpacing(final int value) {
        this.commentSpacing = value;
        return this;
    }

}
