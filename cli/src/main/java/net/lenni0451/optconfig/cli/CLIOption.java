package net.lenni0451.optconfig.cli;

import net.lenni0451.optconfig.index.types.ConfigOption;
import org.jetbrains.annotations.ApiStatus;
import org.yaml.snakeyaml.nodes.Node;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
public record CLIOption(String name, String[] aliases, String[] path, @Nullable Object sectionInstance, ConfigOption configOption, Object value, Node node) {

    public List<String> getNames() {
        String path = Stream.of(this.path).map(s -> s.replace('.', '-')).collect(Collectors.joining("."));
        List<String> names = new ArrayList<>();
        for (String alias : this.aliases) {
            if (alias.isEmpty()) continue;
            names.add(path + alias.replace('.', '-'));
        }
        names.add(path + this.name.replace('.', '-'));
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if (name.length() == 1) {
                names.set(i, "-" + name);
            } else {
                names.set(i, "--" + name);
            }
        }
        return names;
    }

    public String formatNameAndAliases() {
        return String.join(", ", this.getNames());
    }

}
