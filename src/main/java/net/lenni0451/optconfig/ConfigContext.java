package net.lenni0451.optconfig;

import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.provider.ConfigProvider;
import net.lenni0451.optconfig.utils.YamlNodeUtils;
import org.yaml.snakeyaml.nodes.MappingNode;

import java.io.IOException;
import java.io.StringReader;

public class ConfigContext<C> {

    private final ConfigLoader<C> configLoader;
    private final C configInstance;
    private final ConfigProvider configProvider;
    private final ConfigIndex configIndex;

    ConfigContext(final ConfigLoader<C> configLoader, final C configInstance, final ConfigProvider configProvider, final ConfigIndex configIndex) {
        this.configLoader = configLoader;
        this.configInstance = configInstance;
        this.configProvider = configProvider;
        this.configIndex = configIndex;
    }

    /**
     * @return The config class
     */
    public Class<C> getConfigClass() {
        return this.configLoader.configClass;
    }

    /**
     * @return The config instance (null for static configs)
     */
    public C getConfigInstance() {
        return this.configInstance;
    }

    /**
     * Reload all reloadable config values.
     *
     * @throws IOException            If an I/O error occurs
     * @throws IllegalAccessException If the config class or options are not accessible
     */
    public void reload() throws IOException, IllegalAccessException {
        this.configLoader.parseSection(this.configIndex, this.configInstance, this.configProvider, true);
    }

    /**
     * Save the current config values to the config file.
     *
     * @throws IOException            If an I/O error occurs
     * @throws IllegalAccessException If the config class or options are not accessible
     */
    public void save() throws IOException, IllegalAccessException {
        MappingNode serializedSection = ConfigSerializer.serializeSection(this.configLoader, this.configInstance, this.configIndex, this.configInstance);
        if (!this.configLoader.getConfigOptions().isRewriteConfig()) {
            //If the config should not be rewritten, copy over comments and formatting
            MappingNode readNode = (MappingNode) this.configLoader.yaml.compose(new StringReader(this.configProvider.load()));
            YamlNodeUtils.copyComments(readNode, serializedSection);
        }
        this.configLoader.save(serializedSection, this.configProvider);
    }

}
