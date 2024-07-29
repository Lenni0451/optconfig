package net.lenni0451.optconfig;

import net.lenni0451.optconfig.exceptions.ConfigNotAnnotatedException;
import net.lenni0451.optconfig.exceptions.EmptyConfigException;
import net.lenni0451.optconfig.index.ClassIndexer;
import net.lenni0451.optconfig.index.ConfigDiff;
import net.lenni0451.optconfig.index.ConfigType;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.SectionIndex;
import net.lenni0451.optconfig.provider.ConfigProvider;
import net.lenni0451.optconfig.serializer.TypeSerializerList;
import net.lenni0451.optconfig.utils.ReflectionUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.representer.Representer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * The config loader providing methods to load instanced and static configs.<br>
 * A new config loader should be created for each config class.
 *
 * @param <C> The type of the config instance
 */
public class ConfigLoader<C> {

    private final Class<C> configClass;
    final Yaml yaml;
    private final ConfigOptions configOptions = new ConfigOptions();
    public final TypeSerializerList<C> typeSerializers = new TypeSerializerList<>();

    public ConfigLoader(final Class<C> configClass) {
        this.configClass = configClass;

        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setProcessComments(true); //Enable comment parsing
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setProcessComments(true); //Enable comment writing
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); //Set the default flow style to block
        this.yaml = new Yaml(new SafeConstructor(loaderOptions), new Representer(dumperOptions), dumperOptions); //Use safe constructor to prevent code execution
    }

    /**
     * @return The config options
     */
    public ConfigOptions getConfigOptions() {
        return this.configOptions;
    }

    /**
     * @return The list of type serializers
     */
    public TypeSerializerList<C> getTypeSerializers() {
        return this.typeSerializers;
    }

    /**
     * Load an instanced config from the given path.<br>
     * A new instance of the config class will be created and returned.<br>
     * The config class must have an empty constructor.
     *
     * @param configProvider The config provider for loading and saving the config
     * @return The loaded config
     * @throws IOException            If an I/O error occurs
     * @throws IllegalAccessException If the config class or options are not accessible
     */
    public C load(final ConfigProvider configProvider) throws IOException, IllegalAccessException {
        return this.load(ReflectionUtils.instantiate(this.configClass), configProvider);
    }

    /**
     * Load a static config from the given path.<br>
     * The given instance will be used to store the values of the config.
     *
     * @param config         The instance to store the values
     * @param configProvider The config provider for loading and saving the config
     * @return The loaded config
     * @throws IOException            If an I/O error occurs
     * @throws IllegalAccessException If the config class or options are not accessible
     */
    public C load(final C config, final ConfigProvider configProvider) throws IOException, IllegalAccessException {
        SectionIndex index = ClassIndexer.indexClass(ConfigType.INSTANCED, this.configClass);
        if (!(index instanceof ConfigIndex)) throw new ConfigNotAnnotatedException(this.configClass);
        if (index.isEmpty()) throw new EmptyConfigException(this.configClass);

        this.parseSection(index, config, configProvider);
        return config;
    }

    /**
     * Load a static config from the given path.
     *
     * @param configProvider The config provider for loading and saving the config
     * @throws IOException            If an I/O error occurs
     * @throws IllegalAccessException If the config class or options are not accessible
     */
    public void loadStatic(final ConfigProvider configProvider) throws IOException, IllegalAccessException {
        SectionIndex index = ClassIndexer.indexClass(ConfigType.STATIC, this.configClass);
        if (!(index instanceof ConfigIndex)) throw new ConfigNotAnnotatedException(this.configClass);
        if (index.isEmpty()) throw new EmptyConfigException(this.configClass);
        this.parseSection(index, null, configProvider);
    }

    private void parseSection(final SectionIndex sectionIndex, @Nullable final C instance, final ConfigProvider configProvider) throws IOException, IllegalAccessException {
        if (configProvider.exists()) {
            //If the file exists, load the content and deserialize it to a map
            //If differences are found, load the config again as Nodes and apply the differences, then save the config again
            String content = configProvider.load();
            Map<String, Object> values = this.yaml.load(content);
            ConfigDiff configDiff = ConfigSerializer.deserializeSection(this, instance, sectionIndex, instance, values, null);
            if (!configDiff.isEmpty()) {
                MappingNode mergedNode = DiffMerger.merge(this, content, sectionIndex, configDiff, instance);
                this.save(mergedNode, configProvider);
            }
        } else {
            //If the file does not exist, simply serialize the default values
            MappingNode node = ConfigSerializer.serializeSection(this, instance, sectionIndex, instance);
            this.save(node, configProvider);
        }
    }

    private void save(final MappingNode node, final ConfigProvider configProvider) throws IOException {
        StringWriter writer = new StringWriter();
        this.yaml.serialize(node, writer);
        configProvider.save(writer.toString());
    }

}
