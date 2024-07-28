package net.lenni0451.optconfig.index.types;

import lombok.ToString;
import net.lenni0451.optconfig.annotations.Description;
import net.lenni0451.optconfig.annotations.NotReloadable;
import net.lenni0451.optconfig.annotations.Option;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@ToString
@ApiStatus.Internal
public class ConfigOption {

    private final Field field;
    private final String name;
    private final String[] description;
    private final boolean reloadable;
    @Nullable
    private final Method validator;
    private final String[] dependencies;

    public ConfigOption(final Field field, final Option option, @Nullable final Description description, @Nullable final NotReloadable notReloadable, final Map<String, Method> validatorMethods) {
        this.field = field;
        this.name = option.value();
        this.description = description == null ? new String[0] : description.value();
        this.reloadable = notReloadable == null;
        this.validator = validatorMethods.get(this.getName());
        this.dependencies = option.dependencies();
    }

    public Field getField() {
        return this.field;
    }

    public String getName() {
        if (this.name.isEmpty()) return this.field.getName();
        else return this.name;
    }

    public String[] getDescription() {
        return this.description;
    }

    public boolean isReloadable() {
        return this.reloadable;
    }

    @Nullable
    public Method getValidator() {
        return this.validator;
    }

    public String[] getDependencies() {
        return this.dependencies;
    }

}
