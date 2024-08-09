package net.lenni0451.optconfig;

import net.lenni0451.optconfig.provider.ConfigProvider;
import net.lenni0451.optconfig.utils.YamlUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A config loader for directly loading and saving maps.
 */
public class MapConfigLoader {

    private final Yaml yaml;
    private final ConfigProvider configProvider;

    public MapConfigLoader(final ConfigProvider configProvider) {
        this(configProvider, loaderOptions -> {}, dumperOptions -> {});
    }

    public MapConfigLoader(final ConfigProvider configProvider, final Consumer<LoaderOptions> loaderOptionsConsumer, final Consumer<DumperOptions> dumperOptionsConsumer) {
        this.yaml = YamlUtils.createYaml(loaderOptionsConsumer, dumperOptionsConsumer);
        this.configProvider = configProvider;
    }

    /**
     * Load the config from the config provider.
     *
     * @return The loaded config
     * @throws IOException If an I/O error occurs
     */
    public Map<String, Object> load() throws IOException {
        String content = this.configProvider.load();
        return this.yaml.load(content);
    }

    /**
     * Save the given config to the config provider.<br>
     * The user comments and order of the config will be preserved.
     *
     * @param config The config to save
     * @throws IOException If an I/O error occurs
     */
    public void save(final Map<String, Object> config) throws IOException {
        MappingNode rootNode = (MappingNode) this.yaml.compose(new StringReader(this.configProvider.load()));
        MappingNode valuesNode = (MappingNode) this.yaml.represent(config);
        YamlUtils.copyValues(valuesNode, rootNode);
        StringWriter writer = new StringWriter();
        this.yaml.serialize(rootNode, writer);
        this.configProvider.save(writer.toString());
    }

}
