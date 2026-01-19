package net.lenni0451.optconfig.cli;

import net.lenni0451.optconfig.utils.HelpFormatter;
import net.lenni0451.optconfig.utils.generics.Generics;
import org.jetbrains.annotations.ApiStatus;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public class CLIHelpBuilder {

    public static void build(final HelpFormatter formatter, final List<CLIOption> options, final HelpOptions helpOptions) {
        if (helpOptions.sort()) {
            options.sort((o1, o2) -> o1.name().compareToIgnoreCase(o2.name()));
        }
        for (CLIOption option : options) {
            String name = option.formatNameAndAliases();
            String[] description = option.configOption().getDescription();
            String[] dependencies = option.configOption().getDependencies();
            String defaultValue = getDefaultValueString(option, helpOptions.quoteStrings());

            Class<?> fieldType = option.configOption().getFieldAccess().getType();
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                if (helpOptions.booleanType()) {
                    formatter.add(0, name + " [" + fieldType.getSimpleName() + "]");
                } else {
                    formatter.add(0, name);
                }
            } else if (option.node() instanceof SequenceNode sequence) {
                String entryType = fieldType.getSimpleName();
                if (!sequence.getValue().isEmpty()) {
                    Node firstEntry = sequence.getValue().get(0);
                    if (firstEntry instanceof ScalarNode scalarEntry) {
                        entryType = scalarEntry.getTag().getClassName();
                    }
                } else if (List.class.isAssignableFrom(fieldType)) {
                    Type entryGenericType = Generics.getListEntryGenericType(option.configOption().getFieldAccess().getGenericType());
                    Class<?> genericType = Generics.resolveTypeToClass(entryGenericType);
                    if (genericType != null) entryType = genericType.getSimpleName();
                } else if (fieldType.isArray()) {
                    entryType = fieldType.getComponentType().getSimpleName();
                }
                formatter.add(0, name + " <" + entryType + ">...");
            } else {
                formatter.add(0, name + " <" + fieldType.getSimpleName() + ">");
            }
            if (helpOptions.description()) {
                for (String line : description) {
                    formatter.add(1, line);
                }
            }
            if (helpOptions.depends() && dependencies.length > 0) {
                formatter.add(1, "Depends on: " + String.join(", ", dependencies));
            }
            if (helpOptions.defaults() && defaultValue != null) {
                formatter.add(1, "Default: " + defaultValue);
            }
            formatter.pad();
        }
    }

    @Nullable
    private static String getDefaultValueString(final CLIOption option, final boolean quoteStrings) {
        Object value = option.value();
        if (value == null) return null;
        return getDefaultValueString(option.node(), quoteStrings);
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
            case mapping -> throw new IllegalArgumentException("Mapping nodes are not supported for default value representation");
            case anchor -> null;
        };
    }

}
