package net.lenni0451.optconfig.serializer;

import net.lenni0451.optconfig.ConfigLoader;
import net.lenni0451.optconfig.annotations.cli.CLIAliases;
import net.lenni0451.optconfig.annotations.cli.CLIIgnore;
import net.lenni0451.optconfig.annotations.cli.CLIName;
import net.lenni0451.optconfig.cli.CLIOption;
import net.lenni0451.optconfig.exceptions.CLIIncompatibleOptionException;
import net.lenni0451.optconfig.index.types.ConfigOption;
import net.lenni0451.optconfig.index.types.SectionIndex;
import net.lenni0451.optconfig.serializer.info.SerializerInfo;
import org.jetbrains.annotations.ApiStatus;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Stack;

@ApiStatus.Internal
public class CLIConfigSerializer {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static <C> void parseCLIOptions(final ConfigLoader<C> configLoader, @Nullable final C configInstance, final SectionIndex sectionIndex, @Nullable final Object sectionInstance, final Stack<String> path, final List<CLIOption> cliOptions) {
        for (String optionName : sectionIndex.getOptionsOrder()) {
            ConfigOption option = sectionIndex.getOption(optionName);
            if (option == null) throw new IllegalStateException("Section index is desynchronized with options order");
            Object optionValue = option.getFieldAccess().getValue(sectionInstance);
            Class<?> optionType = option.getFieldAccess().getType();
            Type optionGenericType = option.getFieldAccess().getGenericType();
            if (sectionIndex.getSubSections().containsKey(option)) {
                path.push(option.getName());
                try {
                    parseCLIOptions(configLoader, configInstance, sectionIndex.getSubSections().get(option), optionValue, path, cliOptions);
                } finally {
                    path.pop();
                }
            } else {
                ConfigTypeSerializer<?> typeSerializer = option.createTypeSerializer(configLoader);
                Object deserializedValue = optionValue;
                if (option.getValidator() != null) deserializedValue = option.getValidator().invoke(sectionInstance, deserializedValue);

                Node valueNode = configLoader.getYaml().represent(typeSerializer.serialize(new SerializerInfo(configInstance, configLoader.getTypeSerializers(), optionType, optionGenericType, deserializedValue)));
                if (valueNode instanceof SequenceNode sequenceNode) {
                    for (Node node : sequenceNode.getValue()) {
                        if (!(node instanceof ScalarNode)) {
                            throw CLIIncompatibleOptionException.invalidList(path, option.getName());
                        }
                    }
                } else if (valueNode instanceof MappingNode) {
                    throw CLIIncompatibleOptionException.invalidOption(path, option.getName());
                }

                //We can assume the annotations exist because the ClassIndexer should have been informed about them
                CLIName cliName = (CLIName) option.getExtraAnnotations()[0];
                CLIAliases cliAliases = (CLIAliases) option.getExtraAnnotations()[1];
                CLIIgnore cliIgnore = (CLIIgnore) option.getExtraAnnotations()[2];
                if (cliIgnore == null) {
                    String name = (cliName == null || cliName.value().isBlank()) ? optionName : cliName.value();
                    String[] aliases = cliAliases == null ? EMPTY_STRING_ARRAY : cliAliases.value();
                    cliOptions.add(new CLIOption(name, aliases, path.toArray(EMPTY_STRING_ARRAY), sectionInstance, option, deserializedValue, valueNode));
                }
            }
        }
    }

}
