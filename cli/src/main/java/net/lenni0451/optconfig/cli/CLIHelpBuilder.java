package net.lenni0451.optconfig.cli;

import net.lenni0451.optconfig.utils.generics.Generics;
import org.jetbrains.annotations.ApiStatus;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                    entry = new HelpEntry(name + " [" + fieldType.getSimpleName() + "]");
                } else {
                    entry = new HelpEntry(name);
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
                entry = new HelpEntry(name + " <" + entryType + ">...");
            } else {
                entry = new HelpEntry(name + " <" + fieldType.getSimpleName() + ">");
            }
            if (helpOptions.showDescription()) {
                Collections.addAll(entry.description, description);
            }
            if (helpOptions.showDepends() && dependencies.length > 0) {
                entry.description.add("Depends on: " + String.join(", ", dependencies));
            }
            if (helpOptions.showDefaults() && defaultValue != null) {
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
            case mapping -> throw new IllegalArgumentException("Mapping nodes are not supported for default value representation");
            case anchor -> null;
        };
    }

    private static String toString(final List<HelpEntry> helpEntries, final HelpOptions helpOptions) {
        int longestOption = Math.max(helpEntries.stream().map(HelpEntry::name).mapToInt(String::length).max().orElse(0), helpOptions.optionTitle().length());
        int longestDescription = helpEntries.stream().flatMap(e -> e.description.stream()).mapToInt(String::length).max().orElse(0);
        if (longestDescription > 0) longestDescription = Math.max(longestDescription, helpOptions.descriptionTitle().length());
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
            if (!entry.description.isEmpty()) {
                out.append(" ".repeat(longestOption - entry.name.length() + helpOptions.columnPadding()));
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


    private record HelpEntry(String name, List<String> description) {
        public HelpEntry(final String name) {
            this(name, new ArrayList<>());
        }
    }

}
