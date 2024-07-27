package net.lenni0451.optconfig.index.types;

import lombok.ToString;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;

@ToString
@ApiStatus.Internal
public class ConfigOption {

    private final Field field;
    private final String name;
    private final String[] description;
    private final boolean reloadable;

    public ConfigOption(final Field field, final String name, final String[] description, final boolean reloadable) {
        this.field = field;
        this.name = name;
        this.description = description;
        this.reloadable = reloadable;
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

}
