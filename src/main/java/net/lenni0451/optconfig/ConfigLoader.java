package net.lenni0451.optconfig;

import net.lenni0451.optconfig.index.ClassIndexer;
import net.lenni0451.optconfig.index.ConfigDiff;
import net.lenni0451.optconfig.index.ConfigType;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.SectionIndex;
import net.lenni0451.optconfig.serializer.IConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.impl.GenericEnumSerializer;
import net.lenni0451.optconfig.serializer.impl.PassthroughTypeSerializer;
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
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class ConfigLoader {

    final Yaml yaml;
    private final ConfigOptions configOptions = new ConfigOptions();
    private final Map<Class<?>, IConfigTypeSerializer> typeSerializers = new HashMap<>();

    public ConfigLoader() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setProcessComments(true); //Enable comment parsing
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setProcessComments(true); //Enable comment writing
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); //Set the default flow style to block
        this.yaml = new Yaml(new SafeConstructor(loaderOptions), new Representer(dumperOptions), dumperOptions); //Use safe constructor to prevent code execution

        this.addTypeSerializer(Enum.class, new GenericEnumSerializer()); //A generic enum serializer that converts strings to enum. The names are case-insensitive
        this.addTypeSerializer(Object.class, new PassthroughTypeSerializer()); //The default type serializer if no other is found
    }

    public ConfigOptions getConfigOptions() {
        return this.configOptions;
    }

    public <T> void addTypeSerializer(final Class<T> clazz, final IConfigTypeSerializer<T> typeSerializer) {
        this.typeSerializers.put(clazz, typeSerializer);
    }

    <T> IConfigTypeSerializer<T> getTypeSerializer(final Class<T> clazz) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            IConfigTypeSerializer<T> typeSerializer = (IConfigTypeSerializer<T>) this.typeSerializers.get(currentClass);
            if (typeSerializer != null) return typeSerializer;
            currentClass = currentClass.getSuperclass();
        }
        //Should never happen because every class has Object as superclass
        return (IConfigTypeSerializer<T>) this.typeSerializers.get(Object.class);
    }

    public <T> T load(final Class<T> configClass, final Path path) throws IOException, IllegalAccessException {
        return this.load(ReflectionUtils.instantiate(configClass), path);
    }

    public <T> T load(final T config, final Path path) throws IOException, IllegalAccessException {
        SectionIndex index = ClassIndexer.indexClass(ConfigType.INSTANCED, config.getClass());
        if (!(index instanceof ConfigIndex)) throw new IllegalArgumentException("The config class must be annotated with @OptConfig");

        this.parseSection(index, config, path);
        return config;
    }

    public void loadStatic(final Class<?> configClass, final Path path) throws IOException, IllegalAccessException {
        SectionIndex index = ClassIndexer.indexClass(ConfigType.INSTANCED, configClass);
        if (!(index instanceof ConfigIndex)) throw new IllegalArgumentException("The config class must be annotated with @OptConfig");
        this.parseSection(index, null, path);
    }

    private void parseSection(final SectionIndex sectionIndex, @Nullable final Object instance, final Path path) throws IOException, IllegalAccessException {
        if (Files.exists(path)) {
            //If the file exists, load the content and deserialize it to a map
            //If differences are found, load the config again as Nodes and apply the differences, then save the config again
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            Map<String, Object> values = this.yaml.load(content);
            ConfigDiff configDiff = ConfigSerializer.deserializeSection(this, sectionIndex, instance, values, null);
            if (!configDiff.isEmpty()) {
                MappingNode mergedNode = DiffMerger.merge(this, content, sectionIndex, configDiff, instance);
                this.save(mergedNode, path);
            }
        } else {
            //If the file does not exist, simply serialize the default values
            MappingNode node = ConfigSerializer.serializeSection(this, sectionIndex, instance);
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
