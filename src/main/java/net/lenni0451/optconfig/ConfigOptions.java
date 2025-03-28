package net.lenni0451.optconfig;

import lombok.Getter;
import net.lenni0451.optconfig.access.ClassAccessFactory;
import net.lenni0451.optconfig.access.impl.reflection.ReflectionClassAccess;
import net.lenni0451.optconfig.access.types.ClassAccess;
import net.lenni0451.optconfig.annotations.NotReloadable;

import java.util.function.BiPredicate;

/**
 * The options to configure the behavior of the config loader.
 */
@Getter
public class ConfigOptions {

    private boolean resetInvalidOptions = false;
    private boolean removeUnknownOptions = true;
    private boolean addMissingOptions = true;
    private int commentSpacing = 1;
    private boolean rewriteConfig = false;
    private boolean spaceBetweenOptions = true;
    private boolean notReloadableComment = true;
    private ClassAccessFactory classAccessFactory = ReflectionClassAccess::new;
    private BiPredicate<Object, Object> defaultValueComparator = (o1, o2) -> {
        if (o1 instanceof Boolean && o2 instanceof Boolean) return (boolean) o1 == (boolean) o2;
        if (o1 instanceof Byte && o2 instanceof Byte) return (byte) o1 == (byte) o2;
        if (o1 instanceof Short && o2 instanceof Short) return (short) o1 == (short) o2;
        if (o1 instanceof Character && o2 instanceof Character) return (char) o1 == (char) o2;
        if (o1 instanceof Integer && o2 instanceof Integer) return (int) o1 == (int) o2;
        if (o1 instanceof Long && o2 instanceof Long) return (long) o1 == (long) o2;
        if (o1 instanceof Float && o2 instanceof Float) return (float) o1 == (float) o2;
        if (o1 instanceof Double && o2 instanceof Double) return (double) o1 == (double) o2;
        return o1 == o2;
    };

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

    /**
     * Set if there should be an empty line between each option.<br>
     * Default: {@code true}
     *
     * @param value The new value
     * @return The config options
     */
    public ConfigOptions setSpaceBetweenOptions(final boolean value) {
        this.spaceBetweenOptions = value;
        return this;
    }

    /**
     * Automatically add a comment to options that are marked with {@link NotReloadable}.<br>
     * Default: {@code true}
     *
     * @param value The new value
     * @return The config options
     */
    public ConfigOptions setNotReloadableComment(final boolean value) {
        this.notReloadableComment = value;
        return this;
    }

    /**
     * Set the factory for the {@link ClassAccess} instances.<br>
     * Default: {@link ReflectionClassAccess}
     *
     * @param classAccessFactory The new factory
     * @return The config options
     */
    public ConfigOptions setClassAccessFactory(final ClassAccessFactory classAccessFactory) {
        this.classAccessFactory = classAccessFactory;
        return this;
    }

    /**
     * Set the comparator for the default values of the options.<br>
     * This is used to determine if a default value is equal to the current value of an option.<br>
     * Default: {@code (o1, o2) -> o1 == o2}
     *
     * @param defaultValueComparator The new comparator
     * @return The config options
     */
    public ConfigOptions setDefaultValueComparator(final BiPredicate<Object, Object> defaultValueComparator) {
        this.defaultValueComparator = defaultValueComparator;
        return this;
    }

}
