package net.lenni0451.optconfig.cli;

import net.lenni0451.optconfig.cli.model.HelpOptions;
import net.lenni0451.optconfig.utils.HelpTable;
import net.lenni0451.optconfig.utils.generics.Generics;
import org.jetbrains.annotations.ApiStatus;
import org.yaml.snakeyaml.nodes.*;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class CLIHelpBuilder {

    public static String build(final List<CLIOption> options, final HelpOptions helpOptions) {
        if (helpOptions.sort()) {
            options.sort((o1, o2) -> o1.name().compareToIgnoreCase(o2.name()));
        }

        HelpTable helpTable = new HelpTable(helpOptions);
        for (CLIOption option : options) {
            List<String> names = option.getNames(option.hiddenAliases());
            String[] description = option.configOption().getDescription();
            String[] dependencies = option.configOption().getDependencies();
            String defaultValue = getDefaultValueString(option.node(), helpOptions.quoteStrings());

            Class<?> fieldType = option.configOption().getFieldAccess().getType();
            String typeStr = "";
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                if (helpOptions.showBooleanType()) {
                    typeStr = "[" + fieldType.getSimpleName() + "]";
                }
            } else if (option.node() instanceof SequenceNode sequence) {
                String entryType = fieldType.getSimpleName();
                if (List.class.isAssignableFrom(fieldType)) {
                    Type entryGenericType = Generics.getListEntryGenericType(option.configOption().getFieldAccess().getGenericType());
                    Class<?> genericType = Generics.resolveTypeToClass(entryGenericType);
                    if (genericType != null) entryType = genericType.getSimpleName();
                } else if (fieldType.isArray()) {
                    entryType = fieldType.getComponentType().getSimpleName();
                } else if (!sequence.getValue().isEmpty()) {
                    Node firstEntry = sequence.getValue().get(0);
                    if (firstEntry instanceof ScalarNode scalarEntry) {
                        entryType = scalarEntry.getTag().getClassName();
                    }
                }
                typeStr = "<" + entryType + ">...";
            } else if (option.node() instanceof MappingNode mapping) {
                String keyType = "key";
                String valueType = "value";
                if (Map.class.isAssignableFrom(fieldType)) {
                    Type fieldGenericType = option.configOption().getFieldAccess().getGenericType();

                    Type keyGenericType = Generics.getMapKeyGenericType(fieldGenericType);
                    Class<?> keyGenericClass = Generics.resolveTypeToClass(keyGenericType);
                    if (keyGenericType != null) keyType = keyGenericClass.getSimpleName();

                    Type valueGenericType = Generics.getMapValueGenericType(fieldGenericType);
                    Class<?> valueGenericClass = Generics.resolveTypeToClass(valueGenericType);
                    if (valueGenericType != null) valueType = valueGenericClass.getSimpleName();
                } else if (!mapping.getValue().isEmpty()) {
                    NodeTuple tuple = mapping.getValue().get(0);
                    if (tuple.getKeyNode() instanceof ScalarNode scalarNode) {
                        keyType = scalarNode.getTag().getClassName();
                    }
                    if (tuple.getValueNode() instanceof ScalarNode scalarNode) {
                        valueType = scalarNode.getTag().getClassName();
                    }
                }
                typeStr = "<" + keyType + ">=<" + valueType + ">";
            } else {
                typeStr = "<" + fieldType.getSimpleName() + ">";
            }

            List<String> entryDescription = new ArrayList<>();
            if (helpOptions.showDescription()) {
                Collections.addAll(entryDescription, description);
            }
            if (helpOptions.showDepends() && dependencies.length > 0) {
                entryDescription.add("Depends on: " + String.join(", ", dependencies));
            }
            if (helpOptions.showDefaults() && defaultValue != null && !option.required()) {
                entryDescription.add("Default: " + defaultValue);
            }

            helpTable.addRow(names, typeStr, option.required(), entryDescription);
        }
        return helpTable.build();
    }

    @Nullable
    private static String getDefaultValueString(final Node node, final boolean quoteStrings) {
        return switch (node.getNodeId()) {
            case scalar -> {
                ScalarNode scalarNode = (ScalarNode) node;
                if (quoteStrings && scalarNode.getTag().equals(Tag.STR)) {
                    yield "\"" + scalarNode.getValue() + "\"";
                }
                yield scalarNode.getValue();
            }
            case sequence -> {
                SequenceNode sequenceNode = (SequenceNode) node;
                List<String> values = new ArrayList<>(sequenceNode.getValue().size());
                for (Node childNode : sequenceNode.getValue()) {
                    String childValue = getDefaultValueString(childNode, quoteStrings);
                    if (childValue != null) values.add(childValue);
                }
                if (values.isEmpty()) {
                    yield null;
                } else {
                    yield String.join(", ", values);
                }
            }
            case mapping -> {
                MappingNode mappingNode = (MappingNode) node;
                List<String> values = new ArrayList<>(mappingNode.getValue().size());
                for (NodeTuple tuple : mappingNode.getValue()) {
                    String key = getDefaultValueString(tuple.getKeyNode(), false);
                    String value = getDefaultValueString(tuple.getValueNode(), false);
                    if (key != null && value != null) {
                        values.add(key + "=" + value);
                    }
                }
                if (values.isEmpty()) {
                    yield null;
                } else {
                    yield String.join(", ", values);
                }
            }
            case anchor -> null;
        };
    }

}
