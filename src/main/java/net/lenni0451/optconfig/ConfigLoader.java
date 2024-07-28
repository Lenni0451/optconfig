package net.lenni0451.optconfig;

import net.lenni0451.optconfig.index.ClassIndexer;
import net.lenni0451.optconfig.index.ConfigDiff;
import net.lenni0451.optconfig.index.ConfigType;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.SectionIndex;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

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

    public ConfigOptions getConfigOptions() {
        return this.configOptions;
    }

    public TypeSerializerList<C> getTypeSerializers() {
        return this.typeSerializers;
    }

    public C load(final Path path) throws IOException, IllegalAccessException {
        return this.load(ReflectionUtils.instantiate(this.configClass), path);
    }

    public C load(final C config, final Path path) throws IOException, IllegalAccessException {
        SectionIndex index = ClassIndexer.indexClass(ConfigType.INSTANCED, this.configClass);
        if (!(index instanceof ConfigIndex)) throw new IllegalArgumentException("The config class must be annotated with @OptConfig");

        this.parseSection(index, config, path);
        return config;
    }

    public void loadStatic(final Path path) throws IOException, IllegalAccessException {
        SectionIndex index = ClassIndexer.indexClass(ConfigType.STATIC, this.configClass);
        if (!(index instanceof ConfigIndex)) throw new IllegalArgumentException("The config class must be annotated with @OptConfig");
        this.parseSection(index, null, path);
    }

    private void parseSection(final SectionIndex sectionIndex, @Nullable final C instance, final Path path) throws IOException, IllegalAccessException {
        if (Files.exists(path)) {
            //If the file exists, load the content and deserialize it to a map
            //If differences are found, load the config again as Nodes and apply the differences, then save the config again
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            Map<String, Object> values = this.yaml.load(content);
            ConfigDiff configDiff = ConfigSerializer.deserializeSection(this, instance, sectionIndex, instance, values, null);
            if (!configDiff.isEmpty()) {
                MappingNode mergedNode = DiffMerger.merge(this, content, sectionIndex, configDiff, instance);
                this.save(mergedNode, path);
            }
        } else {
            //If the file does not exist, simply serialize the default values
            MappingNode node = ConfigSerializer.serializeSection(this, instance, sectionIndex, instance);
            this.save(node, path);
        }
    }

    private void save(final MappingNode node, final Path path) throws IOException {
        StringWriter writer = new StringWriter();
        this.yaml.serialize(node, writer);
        if (path.getParent() != null) Files.createDirectories(path.getParent()); //Create parent directories if they don't exist
        Files.write(path, writer.toString().getBytes(StandardCharsets.UTF_8));
    }

}
