package net.lenni0451.optconfig.cli;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public record UnknownOption(String name, String[] values) {

    public static String[] toArgs(final Collection<UnknownOption> unknownOptions) {
        List<String> args = new ArrayList<>();
        for (UnknownOption unknownOption : unknownOptions) {
            Collections.addAll(args, unknownOption.toArgs());
        }
        return args.toArray(new String[0]);
    }


    public String[] toArgs() {
        if (this.values.length < 1) {
            return new String[]{this.name};
        } else {
            String[] args = new String[this.values.length * 2];
            for (int i = 0; i < this.values.length; i++) {
                args[i * 2] = this.name;
                args[i * 2 + 1] = this.values[i];
            }
            return args;
        }
    }

    @Override
    public @NotNull String toString() {
        return "UnknownOption{" +
                "name='" + this.name + '\'' +
                ", values=" + Arrays.toString(this.values) +
                '}';
    }

}
