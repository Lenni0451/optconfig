package net.lenni0451.optconfig.cli;

import org.yaml.snakeyaml.nodes.*;

import java.util.ArrayList;
import java.util.List;

public class CLIEmitter {

    public static String[] emit(final List<CLIOption> options) {
        List<String> args = new ArrayList<>();
        for (CLIOption option : options) {
            List<String> namesAndAliases = option.getNames();
            String name = namesAndAliases.get(namesAndAliases.size() - 1);
            List<String> values = option.value() == null ? List.of() : getValues(option.node());
            for (String value : values) {
                args.add(name);
                args.add(value);
            }
        }
        return args.toArray(new String[0]);
    }

    private static List<String> getValues(final Node node) {
        return switch (node.getNodeId()) {
            case scalar -> {
                ScalarNode scalarNode = (ScalarNode) node;
                if (scalarNode.getTag().equals(Tag.STR) && scalarNode.getValue().contains(" ")) {
                    yield List.of("\"" + scalarNode.getValue() + "\"");
                } else {
                    yield List.of(scalarNode.getValue());
                }
            }
            case sequence -> {
                SequenceNode sequenceNode = (SequenceNode) node;
                List<String> values = new ArrayList<>(sequenceNode.getValue().size());
                for (Node childNode : sequenceNode.getValue()) {
                    List<String> childValues = getValues(childNode);
                    values.addAll(childValues);
                }
                yield values;
            }
            case mapping -> {
                MappingNode mappingNode = (MappingNode) node;
                List<String> values = new ArrayList<>(mappingNode.getValue().size());
                for (NodeTuple tuple : mappingNode.getValue()) {
                    String key = getValues(tuple.getKeyNode()).stream().findFirst().orElse("");
                    String value = getValues(tuple.getValueNode()).stream().findFirst().orElse("");
                    if (!key.isEmpty() && !value.isEmpty()) {
                        values.add(key + "=" + value);
                    }
                }
                yield values;
            }
            case anchor -> List.of();
        };
    }

}
