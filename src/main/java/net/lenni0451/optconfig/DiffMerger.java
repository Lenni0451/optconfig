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

    static MappingNode merge(final ConfigLoader configLoader, final String fileContent, final SectionIndex sectionIndex, final ConfigDiff configDiff, @Nullable final Object instance) throws IllegalAccessException {
        //Some values in the config have changed
        //Load the config as Nodes and apply the differences to keep comments and formatting
        MappingNode serializedNode = ConfigSerializer.serializeSection(configLoader, sectionIndex, instance); //Used for copying over nodes
        MappingNode readNode = (MappingNode) configLoader.yaml.compose(new StringReader(fileContent));
        doMerge(configDiff, readNode, serializedNode);
        return readNode;
    }

    private static void doMerge(final ConfigDiff configDiff, final MappingNode readNode, final MappingNode serializedNode) {
        for (String removedKey : configDiff.getRemovedKeys()) YamlNodeUtils.remove(readNode, removedKey);
        for (String addedKey : configDiff.getAddedKeys()) {
            NodeTuple tuple = YamlNodeUtils.get(serializedNode, addedKey);
            int index = serializedNode.getValue().indexOf(tuple);
            YamlNodeUtils.insert(readNode, tuple, index);
        }
        for (Map.Entry<String, ConfigDiff> entry : configDiff.getSubSections().entrySet()) {
            MappingNode readSubNode = (MappingNode) YamlNodeUtils.get(readNode, entry.getKey()).getValueNode();
            MappingNode serializedSubNode = (MappingNode) YamlNodeUtils.get(serializedNode, entry.getKey()).getValueNode();
            doMerge(entry.getValue(), readSubNode, serializedSubNode);
        }
    }

}
