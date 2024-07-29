package net.lenni0451.optconfig;

import lombok.Getter;

/**
 * The options to configure the behavior of the config loader.
 */
@Getter
public class ConfigOptions {

    private boolean resetInvalidOptions = false;
    private boolean removeUnknownOptions = true;
    private boolean addMissingOptions = true;
    private int commentSpacing = 0;
    private boolean rewriteConfig = false;

    /**
     * Set if invalid options should be reset to their default value.<br>
     * Default: {@code false}
     *
     * @param value The new value
     * @return The config options
     */
    public ConfigOptions setResetInvalidOptions(final boolean value) {
        this.resetInvalidOptions = value;
        return this;
    }

    /**
     * Set if unknown options should be removed from the config.<br>
     * Default: {@code true}
     *
     * @param value The new value
     * @return The config options
     */
    public ConfigOptions setRemoveUnknownOptions(final boolean value) {
        this.removeUnknownOptions = value;
        return this;
    }

    /**
     * Set if missing options should be added to the config.<br>
     * The default values will be used for the missing options.<br>
     * Default: {@code true}
     *
     * @param value The new value
     * @return The config options
     */
    public ConfigOptions setAddMissingOptions(final boolean value) {
        this.addMissingOptions = value;
        return this;
    }

    /**
     * Set the spacing between the comment start and the comment text.<br>
     * Spaces will be added to the comment until the desired spacing is reached.<br>
     * Default: {@code 0}
     *
     * @param value The new value
     * @return The config options
     */
    public ConfigOptions setCommentSpacing(final int value) {
        this.commentSpacing = value;
        return this;
    }

    /**
     * Set if the config should be rewritten when loading it.<br>
     * This will remove all user comments and formatting.<br>
     * Option values are not affected by this.<br>
     * Default: {@code false}
     *
     * @param value The new value
     * @return The config options
     */
    public ConfigOptions setRewriteConfig(final boolean value) {
        this.rewriteConfig = value;
        return this;
    }

}
