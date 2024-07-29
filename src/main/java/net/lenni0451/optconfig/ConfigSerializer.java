package net.lenni0451.optconfig;

import net.lenni0451.optconfig.annotations.OptConfig;
import net.lenni0451.optconfig.exceptions.OutdatedClassVersionException;
import net.lenni0451.optconfig.index.ConfigDiff;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.ConfigOption;
import net.lenni0451.optconfig.index.types.SectionIndex;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
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

import static net.lenni0451.optconfig.utils.ReflectionUtils.unsafeCast;

@ApiStatus.Internal
class ConfigSerializer {

    static <C> ConfigDiff deserializeSection(final ConfigLoader<C> configLoader, @Nullable final C configInstance, final SectionIndex sectionIndex, @Nullable final Object sectionInstance, final Map<String, Object> values, final boolean reload, ConfigDiff configDiff) throws IllegalAccessException {
        if (sectionIndex instanceof ConfigIndex) {
            configDiff = ConfigDiff.diff(sectionIndex, values);
            runMigration((ConfigIndex) sectionIndex, configDiff, values);
        }

        for (ConfigOption option : sectionIndex.getOptions()) {
            if (!values.containsKey(option.getName())) continue;
            if (reload && !option.isReloadable()) continue; //Skip not reloadable options, but only if it's a reload operation
            try {
                Object value = values.get(option.getName());
                if (sectionIndex.getSubSections().containsKey(option)) {
                    Object optionValue = option.getField().get(sectionInstance);
                    if (optionValue == null) {
                        //Config sections don't have to be instantiated by the user
                        optionValue = ReflectionUtils.instantiate(option.getField().getType());
                        option.getField().set(sectionInstance, optionValue);
                    }
                    deserializeSection(configLoader, configInstance, sectionIndex.getSubSections().get(option), optionValue, unsafeCast(value), reload, configDiff.getSubSections().get(option.getName()));
                } else {
                    ConfigTypeSerializer<C, ?> typeSerializer;
                    if (option.getTypeSerializer() == null) {
                        typeSerializer = configLoader.getTypeSerializers().get(configInstance, option.getField().getType());
                    } else {
                        typeSerializer = unsafeCast(ReflectionUtils.instantiate(option.getTypeSerializer(), new Class[]{configLoader.configClass}, new Object[]{configInstance}));
                    }
                    Object deserializedValue = typeSerializer.deserialize(unsafeCast(option.getField().getType()), value);
                    if (option.getValidator() != null) deserializedValue = ReflectionUtils.invoke(option.getValidator(), sectionInstance, deserializedValue);
                    option.getField().set(sectionInstance, deserializedValue);
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

    static <C> MappingNode serializeSection(final ConfigLoader<C> configLoader, @Nullable final C configInstance, final SectionIndex sectionIndex, @Nullable final Object sectionInstance) throws IllegalAccessException {
        ConfigOptions options = configLoader.getConfigOptions();
        List<NodeTuple> section = new ArrayList<>();
        MappingNode rootNode = new MappingNode(Tag.MAP, section, DumperOptions.FlowStyle.BLOCK);
        for (String optionName : sectionIndex.getOptionsOrder()) {
            ConfigOption option = sectionIndex.getOption(optionName);
            if (option == null) throw new IllegalStateException("Section index is desynchronized with options order");
            Object optionValue = option.getField().get(sectionInstance);
            NodeTuple tuple;
            if (sectionIndex.getSubSections().containsKey(option)) {
                if (optionValue == null) {
                    //Config sections don't have to be instantiated by the user
                    optionValue = ReflectionUtils.instantiate(option.getField().getType());
                    option.getField().set(sectionInstance, optionValue);
                }
                MappingNode subSection = serializeSection(configLoader, configInstance, sectionIndex.getSubSections().get(option), optionValue);
                tuple = new NodeTuple(configLoader.yaml.represent(option.getName()), subSection);
            } else {
                ConfigTypeSerializer<C, ?> typeSerializer;
                if (option.getTypeSerializer() == null) {
                    typeSerializer = configLoader.getTypeSerializers().get(configInstance, option.getField().getType());
                } else {
                    typeSerializer = unsafeCast(ReflectionUtils.instantiate(option.getTypeSerializer(), new Class[]{configLoader.configClass}, new Object[]{configInstance}));
                }
                Object deserializedValue = optionValue;
                if (option.getValidator() != null) deserializedValue = ReflectionUtils.invoke(option.getValidator(), sectionInstance, deserializedValue);
                tuple = new NodeTuple(configLoader.yaml.represent(option.getName()), configLoader.yaml.represent(typeSerializer.serialize(unsafeCast(deserializedValue))));
            }
            if (!section.isEmpty() && configLoader.getConfigOptions().isSpaceBetweenOptions()) YamlNodeUtils.appendComment(tuple, options.getCommentSpacing(), "\n");
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
            if (configIndex.getVersion() != OptConfig.DEFAULT_VERSION) {
                NodeTuple tuple = new NodeTuple(configLoader.yaml.represent(OptConfig.CONFIG_VERSION_OPTION), configLoader.yaml.represent(configIndex.getVersion()));
                if (!section.isEmpty() && configLoader.getConfigOptions().isSpaceBetweenOptions()) YamlNodeUtils.appendComment(tuple, options.getCommentSpacing(), "\n");
                YamlNodeUtils.appendComment(tuple, options.getCommentSpacing(), "The current version of the config file.", "DO NOT CHANGE THIS VALUE!", "CHANGING THIS VALUE CAN BREAK THE CONFIG FILE!");
                section.add(tuple);
            }
            if (configIndex.getHeader().length > 0) {
                YamlNodeUtils.appendComment(rootNode, options.getCommentSpacing(), configIndex.getHeader());
                YamlNodeUtils.appendComment(rootNode, options.getCommentSpacing(), "\n"); //Add an empty line after the header
            }
        }
        return rootNode;
    }

}
