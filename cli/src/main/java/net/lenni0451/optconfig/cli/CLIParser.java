package net.lenni0451.optconfig.cli;

import net.lenni0451.optconfig.exceptions.CLIMissingOptionException;
import net.lenni0451.optconfig.exceptions.CLIParserException;
import org.jetbrains.annotations.ApiStatus;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class CLIParser {

    public static List<UnknownOption> parse(final Yaml yaml, final List<CLIOption> cliOptions, final String[] args, final Map<String, Object> values) throws CLIParserException {
        Map<String, List<String>> passedValues = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("-")) {
                throw CLIParserException.valueWithoutOption(arg);
            } else {
                String[] split = arg.split("=", 2);
                if (split.length == 2) {
                    passedValues.computeIfAbsent(split[0], k -> new ArrayList<>()).add(split[1]);
                } else if (i < args.length - 1 && !args[i + 1].startsWith("-")) {
                    passedValues.computeIfAbsent(arg, k -> new ArrayList<>()).add(args[++i]);
                } else {
                    passedValues.computeIfAbsent(arg, k -> new ArrayList<>());
                }
            }
        }

        Map<CLIOption, List<String>> parsedOptions = new LinkedHashMap<>();
        Map<String, List<String>> unknownOptions = new LinkedHashMap<>();
        PARSE_LOOP:
        for (Map.Entry<String, List<String>> entry : passedValues.entrySet()) {
            for (CLIOption cliOption : cliOptions) {
                String name = entry.getKey();
                List<String> optionValues = new ArrayList<>(entry.getValue());
                if (name.contains(".") && cliOption.node() instanceof MappingNode && !cliOption.getNames().contains(name)) {
                    // Map value, likely not in the key=value format
                    String valueKey = name.substring(name.lastIndexOf('.') + 1);
                    name = name.substring(0, name.lastIndexOf('.'));
                    optionValues.replaceAll(s -> valueKey + "=" + s);
                }
                if (cliOption.getNames().contains(name)) {
                    parsedOptions.computeIfAbsent(cliOption, k -> new ArrayList<>()).addAll(optionValues);
                    continue PARSE_LOOP;
                }
            }
            unknownOptions.put(entry.getKey(), entry.getValue());
        }

        for (CLIOption cliOption : cliOptions) {
            if (cliOption.required() && !parsedOptions.containsKey(cliOption)) {
                throw new CLIMissingOptionException(cliOption);
            }
        }

        for (Map.Entry<CLIOption, List<String>> entry : parsedOptions.entrySet()) {
            Map<String, Object> targetMap = values;
            for (String path : entry.getKey().path()) {
                Object rawTarget = targetMap.computeIfAbsent(path, k -> new LinkedHashMap<>());
                if (!(rawTarget instanceof Map<?, ?>)) {
                    //This should theoretically never happen...
                    //Sections in the config have already been flattened during the option gathering phase
                    //So if we encounter a non-map here, something is seriously wrong
                    throw new IllegalStateException("Invalid path for CLI option '" + entry.getKey().formatNameAndAliases() + "' at '" + path + "'");
                }
                targetMap = (Map<String, Object>) rawTarget;
            }

            Object value;
            if (entry.getKey().node() instanceof SequenceNode) { //List option
                List<Object> list = new ArrayList<>();
                for (String e : entry.getValue()) {
                    list.add(yaml.load(e));
                }
                value = list;
            } else if (entry.getKey().node() instanceof MappingNode) { //Map option
                Map<?, ?> map = new LinkedHashMap<>();
                for (String e : entry.getValue()) {
                    String[] split = e.split("=", 2);
                    if (split.length != 2) {
                        throw CLIParserException.invalidMapValue(entry.getKey(), e);
                    }
                    map.put(yaml.load(split[0]), yaml.load(split[1]));
                }
                value = map;
            } else { //Single value option
                if (entry.getValue().isEmpty()) {
                    value = true;
                } else if (entry.getValue().size() == 1) {
                    value = yaml.load(entry.getValue().get(0));
                } else {
                    throw CLIParserException.multipleValuesForSingleValueOption(entry.getKey(), entry.getValue());
                }
            }
            targetMap.put(entry.getKey().configOption().getName(), value);
        }
        return unknownOptions.entrySet().stream()
                .map(e -> new UnknownOption(e.getKey(), e.getValue().toArray(new String[0])))
                .toList();
    }

}
