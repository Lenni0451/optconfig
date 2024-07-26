package net.lenni0451.optconfig.index.types;

import lombok.ToString;
import net.lenni0451.optconfig.index.ConfigType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@ToString(callSuper = true)
public class ConfigIndex extends SectionIndex {

    private final int version;

    public ConfigIndex(final ConfigType configType, final Class<?> clazz, final int version) {
        super(configType, clazz);
        this.version = version;
    }

    public int getVersion() {
        return this.version;
    }

}
