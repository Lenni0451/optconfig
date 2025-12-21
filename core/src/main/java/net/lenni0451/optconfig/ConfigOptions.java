package net.lenni0451.optconfig;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lenni0451.optconfig.access.ClassAccessFactory;
import net.lenni0451.optconfig.access.impl.reflection.ReflectionClassAccess;
import net.lenni0451.optconfig.access.types.ClassAccess;
import net.lenni0451.optconfig.annotations.NotReloadable;

import java.util.function.BiPredicate;

/**
 * The options to configure the behavior of the config loader.
 */
@Getter
@Setter
@Accessors(chain = true)
public class ConfigOptions {

    /**
     * Should invalid options be reset to their default value.<br>
     * Default: {@code false}
     */
    private boolean resetInvalidOptions = false;
    /**
     * Should unknown options be removed from the config.<br>
     * Default: {@code true}
     */
    private boolean removeUnknownOptions = true;
    /**
     * Should missing options be added to the config.<br>
     * The default values will be used for the missing options.<br>
     * Default: {@code true}
     */
    private boolean addMissingOptions = true;
    /**
     * The spacing between the comment start and the comment text.<br>
     * Spaces will be added to the comment until the desired spacing is reached.<br>
     * Default: {@code 1}
     */
    private int commentSpacing = 1;
    /**
     * Should the config be rewritten when loading it.<br>
     * This will remove all user comments and formatting.<br>
     * Option values are not affected by this.<br>
     * Default: {@code false}
     */
    private boolean rewriteConfig = false;
    /**
     * Should there be an empty line between each option.<br>
     * Default: {@code true}
     */
    private boolean spaceBetweenOptions = true;
    /**
     * Automatically add a comment to options that are marked with {@link NotReloadable}.<br>
     * Default: {@code true}
     */
    private boolean notReloadableComment = true;
    /**
     * The factory for the {@link ClassAccess} instances.<br>
     * Can be set to use a different access method than reflection.<br>
     * Default: {@link ReflectionClassAccess}
     */
    private ClassAccessFactory classAccessFactory = ReflectionClassAccess::new;
    /**
     * The comparator for the default values of the options.<br>
     * This is used to determine if a default value is equal to the current value of an option.<br>
     * The default implementation compares primitive wrapper types by their value and all other objects by reference.<br>
     * Default: {@code (o1, o2) -> o1 == o2}
     */
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

}
