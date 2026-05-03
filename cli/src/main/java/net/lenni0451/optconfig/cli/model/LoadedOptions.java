package net.lenni0451.optconfig.cli.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public record LoadedOptions(Set<String> options, List<UnknownOption> unknownOptions) {

    public String[] unknownOptionsToArgs() {
        List<String> args = new ArrayList<>();
        for (UnknownOption unknownOption : this.unknownOptions) {
            Collections.addAll(args, unknownOption.toArgs());
        }
        return args.toArray(new String[0]);
    }

}
