package net.lenni0451.optconfig;

import lombok.Getter;
import net.lenni0451.optconfig.exceptions.ConfigNotAnnotatedException;
import net.lenni0451.optconfig.exceptions.EmptyConfigException;
import net.lenni0451.optconfig.index.ClassIndexer;
import net.lenni0451.optconfig.index.ConfigType;
import net.lenni0451.optconfig.index.diff.ConfigDiff;
import net.lenni0451.optconfig.index.diff.DiffMerger;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.SectionIndex;
import net.lenni0451.optconfig.provider.ConfigProvider;
import net.lenni0451.optconfig.serializer.ConfigSerializer;
import net.lenni0451.optconfig.serializer.TypeSerializerList;
import net.lenni0451.optconfig.utils.ReflectionUtils;
import net.lenni0451.optconfig.utils.YamlUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The config loader providing methods to load instanced and static configs.<br>
 * A new config loader should be created for each config class.
 *
 * @param <C> The type of the config instance
 */
@Getter
public class ConfigLoader<C> {

    private final Yaml yaml;
    private final Class<C> configClass;
    private final ConfigOptions configOptions;
    private final TypeSerializerList typeSerializers;

    public ConfigLoader(final Class<C> configClass) {
        this(configClass, loaderOptions -> {}, dumperOptions -> {});
    }

    public ConfigLoader(final Class<C> configClass, final Consumer<LoaderOptions> loaderOptionsConsumer, final Consumer<DumperOptions> dumperOptionsConsumer) {
        this(YamlUtils.createYaml(loaderOptionsConsumer, dumperOptionsConsumer), configClass);
    }

    public ConfigLoader(final Yaml yaml, final Class<C> configClass) {
        this.yaml = yaml;
        this.configClass = configClass;
        this.configOptions = new ConfigOptions();
        this.typeSerializers = new TypeSerializerList();
    }

    /**
     * Load an instanced config from the given path.<br>
     * A new instance of the config class will be created and returned.<br>
     * The config class must have an empty constructor.
     *
     * @param configProvider The config provider for loading and saving the config
     * @return The config context
     * @throws IOException If an I/O error occurs
     */
    public ConfigContext<C> load(final ConfigProvider configProvider) throws IOException {
        return this.load(ReflectionUtils.instantiate(this, this.configClass), configProvider);
    }

    /**
     * Load a static config from the given path.<br>
     * The given instance will be used to store the values of the config.
     *
     * @param config         The instance to store the values
     * @param configProvider The config provider for loading and saving the config
     * @return The config context
     * @throws IOException If an I/O error occurs
     */
    public ConfigContext<C> load(final C config, final ConfigProvider configProvider) throws IOException {
        SectionIndex index = this.loadSection(ConfigType.INSTANCED, config);
        ConfigContext<C> configContext = new ConfigContext<>(this, config, configProvider, (ConfigIndex) index);
        this.parseSection(index, configContext, config, configProvider, false);
        return configContext;
    }

    /**
     * Load a static config from the given path.
     *
     * @param configProvider The config provider for loading and saving the config
     * @return The config context
     * @throws IOException If an I/O error occurs
     */
    public ConfigContext<C> loadStatic(final ConfigProvider configProvider) throws IOException {
        SectionIndex index = this.loadSection(ConfigType.STATIC, null);
        ConfigContext<C> configContext = new ConfigContext<>(this, null, configProvider, (ConfigIndex) index);
        this.parseSection(index, configContext, null, configProvider, false);
        return configContext;
    }

    private SectionIndex loadSection(final ConfigType configType, final C config) {
        SectionIndex index = ClassIndexer.indexClass(configType, this.configClass, this.configOptions.getClassAccessFactory());
        if (!(index instanceof ConfigIndex)) throw new ConfigNotAnnotatedException(this.configClass);
        if (index.isEmpty()) throw new EmptyConfigException(this.configClass);
        switch (configType) {
            case STATIC -> {
                if (config != null) {
                    throw new IllegalArgumentException("Config instance must be null for STATIC config type");
                }
            }
            case INSTANCED -> {
                if (config == null) {
                    throw new NullPointerException("Config instance cannot be null for INSTANCED config type");
                } else {
                    index.initSubSections(this, config);
                }
            }
        }
        return index;
    }

    void parseSection(final SectionIndex sectionIndex, final ConfigContext<C> configContext, @Nullable final C instance, final ConfigProvider configProvider, final boolean reload) throws IOException {
        if (configProvider.exists()) {
            //If the file exists, load the content and deserialize it to a map
            //If differences are found, load the config again as Nodes and apply the differences, then save the config again
            String content = configProvider.load();
            Map<String, Object> values = this.yaml.load(content);
            ConfigDiff configDiff = ConfigSerializer.deserializeSection(this, instance, sectionIndex, instance, values, reload, null);
            if (!this.configOptions.isRewriteConfig() || reload) {
                //If the config should be rewritten anyway, this step is not necessary
                //On reloads also only apply differences because overwriting the config now would revert not reloadable options
                if (!configDiff.isEmpty()) {
                    MappingNode mergedNode = DiffMerger.merge(this, configContext.defaultValues, content, sectionIndex, configDiff, instance);
                    this.save(mergedNode, configProvider);
                }
                return;
            }
        }
        //If the file does not exist, simply serialize the default values
        //This also applies if ConfigOptions.isRewriteConfig() is true
        MappingNode node = ConfigSerializer.serializeSection(this, configContext.defaultValues, instance, sectionIndex, instance);
        this.save(node, configProvider);
    }

    void save(final MappingNode node, final ConfigProvider configProvider) throws IOException {
        StringWriter writer = new StringWriter();
        this.yaml.serialize(node, writer);
        configProvider.save(writer.toString());
    }

}
