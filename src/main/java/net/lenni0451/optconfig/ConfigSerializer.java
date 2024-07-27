package net.lenni0451.optconfig;

import net.lenni0451.optconfig.annotations.OptConfig;
import net.lenni0451.optconfig.exceptions.OutdatedClassVersionException;
import net.lenni0451.optconfig.index.ConfigDiff;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.ConfigOption;
import net.lenni0451.optconfig.index.types.SectionIndex;
import net.lenni0451.optconfig.serializer.IConfigTypeSerializer;
import net.lenni0451.optconfig.utils.ReflectionUtils;
import net.lenni0451.optconfig.utils.YamlNodeUtils;
import org.jetbrains.annotations.ApiStatus;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
@SuppressWarnings({"rawtypes", "unchecked"})
class ConfigSerializer {

    static ConfigDiff deserializeSection(final ConfigLoader configLoader, final SectionIndex sectionIndex, @Nullable final Object instance, final Map<String, Object> values) throws IllegalAccessException {
        ConfigDiff configDiff = ConfigDiff.diff(sectionIndex, values);
        if (sectionIndex instanceof ConfigIndex) runMigration((ConfigIndex) sectionIndex, configDiff, values);

        for (ConfigOption option : sectionIndex.getOptions()) {
            if (!values.containsKey(option.getName())) continue;
            try {
                Object value = values.get(option.getName());
                if (sectionIndex.getSubSections().containsKey(option)) {
                    Object optionValue = option.getField().get(instance);
                    if (optionValue == null) {
                        //Config sections don't have to be instantiated by the user
                        optionValue = ReflectionUtils.instantiate(option.getField().getType());
                        option.getField().set(instance, optionValue);
                    }
                    deserializeSection(configLoader, sectionIndex.getSubSections().get(option), optionValue, (Map<String, Object>) value);
                } else {
                    IConfigTypeSerializer typeSerializer = configLoader.typeSerializers.get(option.getField().getType());
                    if (typeSerializer == null) typeSerializer = configLoader.typeSerializers.get(null);
                    option.getField().set(instance, typeSerializer.deserialize(value));
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

    private static void runMigration(final ConfigIndex configIndex, final ConfigDiff configDiff, final Map<String, Object> values) {
        int latestVersion = configIndex.getVersion();
        int currentVersion = (int) values.getOrDefault(OptConfig.CONFIG_VERSION_OPTION, OptConfig.DEFAULT_VERSION);
        boolean hasVersionField = values.containsKey(OptConfig.CONFIG_VERSION_OPTION);
        if (latestVersion != OptConfig.DEFAULT_VERSION && !hasVersionField) {
            //Add the version key if it's missing
            configDiff.getAddedKeys().add(OptConfig.CONFIG_VERSION_OPTION);
        } else if (currentVersion != latestVersion) {
            if (hasVersionField) {
                //Replace the version because it's outdated
                configDiff.getInvalidKeys().add(OptConfig.CONFIG_VERSION_OPTION);
            } else {
                //The config is outdated and the version key is missing
                configDiff.getAddedKeys().add(OptConfig.CONFIG_VERSION_OPTION);
            }
        }

        if (currentVersion > latestVersion) {
            //The config file has a newer version than the application
            //Downgrading is not supported
            throw new OutdatedClassVersionException(currentVersion, latestVersion);
        } else if (currentVersion < latestVersion) {
            ConfigIndex.Migrator migrator = configIndex.searchMigrator(currentVersion, latestVersion);
            migrator.getInstance().migrate(currentVersion, values);
        }
    }

    static MappingNode serializeSection(final ConfigLoader configLoader, final SectionIndex sectionIndex, @Nullable final Object instance) throws IllegalAccessException {
        ConfigOptions options = configLoader.getConfigOptions();
        List<NodeTuple> section = new ArrayList<>();
        for (ConfigOption option : sectionIndex.getOptions()) {
            Object optionValue = option.getField().get(instance);
            NodeTuple tuple;
            if (sectionIndex.getSubSections().containsKey(option)) {
                if (optionValue == null) {
                    //Config sections don't have to be instantiated by the user
                    optionValue = ReflectionUtils.instantiate(option.getField().getType());
                    option.getField().set(instance, optionValue);
                }
                MappingNode subSection = serializeSection(configLoader, sectionIndex.getSubSections().get(option), optionValue);
                tuple = new NodeTuple(configLoader.yaml.represent(option.getName()), subSection);
            } else {
                IConfigTypeSerializer typeSerializer = configLoader.typeSerializers.get(option.getField().getType());
                if (typeSerializer == null) typeSerializer = configLoader.typeSerializers.get(null);
                tuple = new NodeTuple(configLoader.yaml.represent(option.getName()), configLoader.yaml.represent(typeSerializer.serialize(optionValue)));
            }
            if (!section.isEmpty()) YamlNodeUtils.appendComment(tuple, options.getCommentSpacing(), "\n");
            YamlNodeUtils.appendComment(tuple, options.getCommentSpacing(), option.getDescription());
            if (!option.isReloadable()) {
                YamlNodeUtils.appendComment(tuple, options.getCommentSpacing(), "This option is not reloadable.");
                if (sectionIndex.getSubSections().containsKey(option)) {
                    YamlNodeUtils.appendComment(tuple, options.getCommentSpacing(), "This applies to all options in this section.");
                }
            }
            section.add(tuple);
        }
        if (sectionIndex instanceof ConfigIndex) {
            ConfigIndex configIndex = (ConfigIndex) sectionIndex;
            if (configIndex.getVersion() != 1) {
                NodeTuple tuple = new NodeTuple(configLoader.yaml.represent(OptConfig.CONFIG_VERSION_OPTION), configLoader.yaml.represent(configIndex.getVersion()));
                if (!section.isEmpty()) YamlNodeUtils.appendComment(tuple, options.getCommentSpacing(), "\n");
                YamlNodeUtils.appendComment(tuple, options.getCommentSpacing(), "The current version of the config file.", "DO NOT CHANGE THIS VALUE!", "CHANGING THIS VALUE CAN BREAK THE CONFIG FILE!");
                section.add(tuple);
            }
        }
        return new MappingNode(Tag.MAP, section, DumperOptions.FlowStyle.BLOCK);
    }

}
