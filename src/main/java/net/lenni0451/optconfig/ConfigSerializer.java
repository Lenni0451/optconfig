package net.lenni0451.optconfig;

import net.lenni0451.optconfig.annotations.OptConfig;
import net.lenni0451.optconfig.exceptions.OutdatedClassVersionException;
import net.lenni0451.optconfig.index.ConfigDiff;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.ConfigOption;
import net.lenni0451.optconfig.index.types.SectionIndex;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import net.lenni0451.optconfig.serializer.info.DeserializerInfo;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;
import net.lenni0451.optconfig.utils.YamlUtils;
import org.jetbrains.annotations.ApiStatus;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;

import static net.lenni0451.optconfig.utils.ReflectionUtils.unsafeCast;

@ApiStatus.Internal
class ConfigSerializer {

    static <C> ConfigDiff deserializeSection(final ConfigLoader<C> configLoader, @Nullable final C configInstance, final SectionIndex sectionIndex, @Nullable final Object sectionInstance, final Map<String, Object> values, final boolean reload, ConfigDiff configDiff) {
        if (sectionIndex instanceof ConfigIndex) {
            configDiff = ConfigDiff.diff(sectionIndex, values);
            runMigration(configLoader, (ConfigIndex) sectionIndex, values);
        }

        for (ConfigOption option : sectionIndex.getOptions()) {
            if (!values.containsKey(option.getName())) continue;
            if (reload && !option.isReloadable()) continue; //Skip not reloadable options, but only if it's a reload operation
            try {
                Object value = values.get(option.getName());
                Object optionValue = option.getFieldAccess().getValue(sectionInstance);
                Class<?> optionType = option.getFieldAccess().getType();
                Type optionGenericType = option.getFieldAccess().getGenericType();
                if (sectionIndex.getSubSections().containsKey(option)) {
                    deserializeSection(configLoader, configInstance, sectionIndex.getSubSections().get(option), optionValue, unsafeCast(value), reload, configDiff.getSubSections().get(option.getName()));
                } else {
                    ConfigTypeSerializer<?> typeSerializer = option.createTypeSerializer(configLoader);
                    Object deserializedValue = typeSerializer.deserialize(new DeserializerInfo(configInstance, configLoader.getTypeSerializers(), optionType, optionGenericType, optionValue, value));
                    if (option.getValidator() != null) deserializedValue = option.getValidator().invoke(sectionInstance, deserializedValue);
                    option.getFieldAccess().setValue(sectionInstance, deserializedValue);
                }
            } catch (Throwable t) {
                if (!configLoader.getConfigOptions().isResetInvalidOptions()) throw t;
                //Add the invalid key to the diff
                //The merger will replace the invalid value with the default value
                configDiff.getInvalidKeys().add(option.getName());
            }
        }
        return configDiff;
    }

    private static void runMigration(final ConfigLoader<?> configLoader, final ConfigIndex configIndex, final Map<String, Object> values) {
        int latestVersion = configIndex.getVersion();
        int currentVersion = (int) values.getOrDefault(OptConfig.CONFIG_VERSION_OPTION, OptConfig.DEFAULT_VERSION);
        if (currentVersion > latestVersion) {
            //The config file has a newer version than the application
            //Downgrading is not supported
            throw new OutdatedClassVersionException(currentVersion, latestVersion);
        } else if (currentVersion < latestVersion) {
            ConfigIndex.Migrator migrator = configIndex.searchMigrator(configLoader, currentVersion, latestVersion);
            migrator.instance().migrate(currentVersion, values);
        }
    }

    static <C> MappingNode serializeSection(final ConfigLoader<C> configLoader, final ConfigContext<C> configContext, @Nullable final C configInstance, final SectionIndex sectionIndex, @Nullable final Object sectionInstance) {
        ConfigOptions options = configLoader.getConfigOptions();
        List<NodeTuple> section = new ArrayList<>();
        MappingNode rootNode = new MappingNode(Tag.MAP, section, DumperOptions.FlowStyle.BLOCK);
        for (String optionName : sectionIndex.getOptionsOrder()) {
            ConfigOption option = sectionIndex.getOption(optionName);
            if (option == null) throw new IllegalStateException("Section index is desynchronized with options order");
            Object optionValue = option.getFieldAccess().getValue(sectionInstance);
            Class<?> optionType = option.getFieldAccess().getType();
            Type optionGenericType = option.getFieldAccess().getGenericType();
            NodeTuple tuple;
            if (sectionIndex.getSubSections().containsKey(option)) {
                MappingNode subSection = serializeSection(configLoader, configContext, configInstance, sectionIndex.getSubSections().get(option), optionValue);
                tuple = new NodeTuple(configLoader.yaml.represent(option.getName()), subSection);
            } else {
                if (option.isHidden() && options.getDefaultValueComparator().test(configContext.defaultValues.get(option), optionValue)) {
                    //Hidden options with their default value should not be saved
                    continue;
                }
                ConfigTypeSerializer<?> typeSerializer = option.createTypeSerializer(configLoader);
                Object deserializedValue = optionValue;
                if (option.getValidator() != null) deserializedValue = option.getValidator().invoke(sectionInstance, deserializedValue);
                tuple = new NodeTuple(configLoader.yaml.represent(option.getName()), configLoader.yaml.represent(typeSerializer.serialize(new SerializerInfo(configInstance, configLoader.getTypeSerializers(), optionType, optionGenericType, deserializedValue))));
            }
            if (!section.isEmpty() && options.isSpaceBetweenOptions()) YamlUtils.appendComment(tuple, options.getCommentSpacing(), "\n");
            YamlUtils.appendComment(tuple, options.getCommentSpacing(), option.getDescription());
            if (!option.isReloadable() && options.isNotReloadableComment()) {
                YamlUtils.appendComment(tuple, options.getCommentSpacing(), "This option is not reloadable.");
                if (sectionIndex.getSubSections().containsKey(option)) {
                    YamlUtils.appendComment(tuple, options.getCommentSpacing(), "This applies to all options in this section.");
                }
            }
            section.add(tuple);
        }
        if (sectionIndex instanceof ConfigIndex configIndex) {
            if (configIndex.getHeader().length > 0) {
                List<String> lines = new ArrayList<>();
                for (String line : configIndex.getHeader()) {
                    String[] parts = line.split("\n", -1);
                    int end = parts.length;
                    while (end > 0 && parts[end - 1].isEmpty()) end--;
                    if (end <= 0) end = parts.length; //If the all lines are empty assume the user wants an empty line
                    Collections.addAll(lines, Arrays.copyOfRange(parts, 0, end));
                }
                if (!lines.isEmpty()) {
                    YamlUtils.appendComment(rootNode, options.getCommentSpacing(), lines.toArray(new String[0]));
                    YamlUtils.appendComment(rootNode, options.getCommentSpacing(), "\n"); //Add an empty line after the header
                }
            }
        }
        return rootNode;
    }

}
