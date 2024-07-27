package net.lenni0451.optconfig.index.types;

import lombok.ToString;
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

    public ConfigOption(final Field field, final String name, final String[] description, final boolean reloadable, final Map<String, Method> validatorMethods) {
        this.field = field;
        this.name = name;
        this.description = description;
        this.reloadable = reloadable;
        this.validator = validatorMethods.get(this.getName());
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

}
