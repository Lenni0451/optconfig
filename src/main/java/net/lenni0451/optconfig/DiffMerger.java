package net.lenni0451.optconfig;

import net.lenni0451.optconfig.index.ConfigDiff;
import net.lenni0451.optconfig.index.types.SectionIndex;
import net.lenni0451.optconfig.utils.YamlNodeUtils;
import org.jetbrains.annotations.ApiStatus;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.NodeTuple;

import javax.annotation.Nullable;
import java.io.StringReader;
import java.util.Map;

@ApiStatus.Internal
class DiffMerger {

    static <C> MappingNode merge(final ConfigLoader<C> configLoader, final String fileContent, final SectionIndex sectionIndex, final ConfigDiff configDiff, @Nullable final C instance) throws IllegalAccessException {
        //Some values in the config have changed
        //Load the config as Nodes and apply the differences to keep comments and formatting
        MappingNode serializedNode = ConfigSerializer.serializeSection(configLoader, instance, sectionIndex, instance); //Used for copying over nodes
        MappingNode readNode = (MappingNode) configLoader.yaml.compose(new StringReader(fileContent));
        doMerge(configLoader.getConfigOptions(), configDiff, readNode, serializedNode);
        return readNode;
    }

    private static void doMerge(final ConfigOptions configOptions, final ConfigDiff configDiff, final MappingNode readNode, final MappingNode serializedNode) {
        if (configOptions.isRemoveUnknownOptions()) {
            for (String removedKey : configDiff.getRemovedKeys()) {
                YamlNodeUtils.remove(readNode, removedKey);
            }
        }
        if (configOptions.isAddMissingOptions()) {
            for (String addedKey : configDiff.getAddedKeys()) {
                NodeTuple tuple = YamlNodeUtils.get(serializedNode, addedKey);
                int index = serializedNode.getValue().indexOf(tuple);
                YamlNodeUtils.insert(readNode, tuple, index);
            }
        }
        for (String invalidKey : configDiff.getInvalidKeys()) {
            NodeTuple invalid = YamlNodeUtils.get(readNode, invalidKey);
            NodeTuple valid = YamlNodeUtils.get(serializedNode, invalidKey);
            YamlNodeUtils.replace(readNode, invalid, valid);
        }
        for (Map.Entry<String, ConfigDiff> entry : configDiff.getSubSections().entrySet()) {
            MappingNode readSubNode = (MappingNode) YamlNodeUtils.get(readNode, entry.getKey()).getValueNode();
            MappingNode serializedSubNode = (MappingNode) YamlNodeUtils.get(serializedNode, entry.getKey()).getValueNode();
            doMerge(configOptions, entry.getValue(), readSubNode, serializedSubNode);
        }
    }

}
