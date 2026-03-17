package net.lenni0451.optconfig.cli;

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
        List<HelpEntry> helpEntries = new ArrayList<>();
        for (CLIOption option : options) {
            String name = option.formatNameAndAliases();
            String[] description = option.configOption().getDescription();
            String[] dependencies = option.configOption().getDependencies();
            String defaultValue = getDefaultValueString(option, helpOptions.quoteStrings());

            Class<?> fieldType = option.configOption().getFieldAccess().getType();
            HelpEntry entry;
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                if (helpOptions.showBooleanType()) {
                    entry = new HelpEntry(name + " [" + fieldType.getSimpleName() + "]", option.required());
                } else {
                    entry = new HelpEntry(name, option.required());
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
                entry = new HelpEntry(name + " <" + entryType + ">...", option.required());
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
                entry = new HelpEntry(name + " <" + keyType + ">=<" + valueType + ">", option.required());
            } else {
                entry = new HelpEntry(name + " <" + fieldType.getSimpleName() + ">", option.required());
            }
            if (helpOptions.showDescription()) {
                Collections.addAll(entry.description, description);
            }
            if (helpOptions.showDepends() && dependencies.length > 0) {
                entry.description.add("Depends on: " + String.join(", ", dependencies));
            }
            if (helpOptions.showDefaults() && defaultValue != null && !option.required()) {
                entry.description.add("Default: " + defaultValue);
            }
            helpEntries.add(entry);
        }
        return toString(helpEntries, helpOptions);
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

    private static String toString(final List<HelpEntry> helpEntries, final HelpOptions helpOptions) {
        int longestOption = Math.max(
                helpEntries.stream()
                        .map(entry -> ((helpOptions.showRequired() && entry.required) ? " (required)" : "") + entry.name())
                        .mapToInt(String::length)
                        .max()
                        .orElse(0),
                helpOptions.optionTitle().length()
        );
        int longestDescription = helpEntries.stream()
                .flatMap(e -> e.description.stream())
                .mapToInt(String::length)
                .max()
                .orElse(0);
        if (longestDescription > 0) {
            longestDescription = Math.max(longestDescription, helpOptions.descriptionTitle().length());
        }
        String optionHeaderPadding = " ".repeat(longestOption - helpOptions.optionTitle().length() + helpOptions.columnPadding());
        StringBuilder out = new StringBuilder()
                .append(helpOptions.optionTitle());
        if (longestDescription > 0) {
            out.append(optionHeaderPadding)
                    .append(helpOptions.descriptionTitle());
        }
        out.append("\n");
        switch (helpOptions.headerSeparator()) {
            case NONE -> {
            }
            case HEADER_WIDTH -> {
                out.append(String.valueOf(helpOptions.separatorChar()).repeat(helpOptions.optionTitle().length()));
                if (longestDescription > 0) {
                    out.append(optionHeaderPadding)
                            .append(String.valueOf(helpOptions.separatorChar()).repeat(helpOptions.descriptionTitle().length()));
                }
                out.append("\n");
            }
            case COLUMN_WIDTH -> {
                out.append(String.valueOf(helpOptions.separatorChar()).repeat(longestOption));
                if (longestDescription > 0) {
                    out.append(" ".repeat(helpOptions.columnPadding()))
                            .append(String.valueOf(helpOptions.separatorChar()).repeat(longestDescription));
                }
                out.append("\n");
            }
            default -> throw new IllegalStateException("Unexpected header separator: " + helpOptions.headerSeparator());
        }
        for (HelpEntry entry : helpEntries) {
            out.append(entry.name);
            if (helpOptions.showRequired() && entry.required) {
                out.append(" (required)");
            }
            if (!entry.description.isEmpty()) {
                out.append(" ".repeat(longestOption - (entry.required ? 11 : 0) - entry.name.length() + helpOptions.columnPadding()));
                for (int i = 0; i < entry.description.size(); i++) {
                    if (i != 0) {
                        out.append("\n").append(" ".repeat(longestOption + helpOptions.columnPadding()));
                    }
                    out.append(entry.description.get(i));
                }
            }
            out.append("\n");
        }
        return out.toString();
    }


    private record HelpEntry(String name, List<String> description, boolean required) {
        public HelpEntry(final String name, final boolean required) {
            this(name, new ArrayList<>(), required);
        }
    }

}
