package net.lenni0451.optconfig;

import lombok.Getter;
import net.lenni0451.optconfig.access.types.FieldAccess;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.ConfigOption;
import net.lenni0451.optconfig.provider.ConfigProvider;
import net.lenni0451.optconfig.serializer.ConfigSerializer;
import net.lenni0451.optconfig.utils.YamlUtils;
import org.yaml.snakeyaml.nodes.MappingNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * The context of a loaded config instance.<br>
 * Allows reloading and saving the config as well as loading and printing CLI options.
 *
 * @param <C> The config class type
 */
public class ConfigContext<C> {

    @Getter
    private final ConfigLoader<C> configLoader;
    @Getter
    private final C configInstance;
    @Getter
    private final ConfigProvider configProvider;
    private final ConfigIndex configIndex;
    final Map<ConfigOption, Object> defaultValues;

    ConfigContext(final ConfigLoader<C> configLoader, final C configInstance, final ConfigProvider configProvider, final ConfigIndex configIndex) {
        this.configLoader = configLoader;
        this.configInstance = configInstance;
        this.configProvider = configProvider;
        this.configIndex = configIndex;
        this.defaultValues = configIndex.getCurrentValues(configInstance);

        this.setContextField();
    }

    private void setContextField() {
        try {
            FieldAccess contextField = null;
            for (FieldAccess field : this.configLoader.getConfigOptions().getClassAccessFactory().create(this.configLoader.getConfigClass()).getFields()) {
                if (!this.configIndex.getConfigType().matches(field.getModifiers())) continue;
                if (!ConfigContext.class.isAssignableFrom(field.getType())) continue;
                contextField = field;
                break;
            }
            if (contextField != null) contextField.setValue(this.configInstance, this);
        } catch (Throwable ignored) {
            //Not critical
        }
    }

    /**
     * Reload all reloadable config values.
     *
     * @throws IOException            If an I/O error occurs
     * @throws IllegalAccessException If the config class or options are not accessible
     */
    public void reload() throws IOException, IllegalAccessException {
        this.configLoader.parseSection(this.configIndex, this, this.configInstance, this.configProvider, true);
    }

    /**
     * Save the current config values to the config file.
     *
     * @throws IOException            If an I/O error occurs
     * @throws IllegalAccessException If the config class or options are not accessible
     */
    public void save() throws IOException, IllegalAccessException {
        MappingNode serializedSection = ConfigSerializer.serializeSection(this.configLoader, this.defaultValues, this.configInstance, this.configIndex, this.configInstance);
        if (this.configLoader.getConfigOptions().isRewriteConfig()) {
            //If the config should be rewritten, just save the serialized section
            this.configLoader.save(serializedSection, this.configProvider);
        } else {
            //If the config should not be rewritten, copy over comments and formatting
            MappingNode readNode = (MappingNode) this.configLoader.getYaml().compose(new InputStreamReader(new ByteArrayInputStream(this.configProvider.load())));
            YamlUtils.copyValues(serializedSection, readNode);
            this.configLoader.save(readNode, this.configProvider);
        }
    }

}
