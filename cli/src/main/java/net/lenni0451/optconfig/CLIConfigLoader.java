package net.lenni0451.optconfig;

import net.lenni0451.optconfig.annotations.cli.CLIAliases;
import net.lenni0451.optconfig.annotations.cli.CLIIgnore;
import net.lenni0451.optconfig.annotations.cli.CLIName;
import net.lenni0451.optconfig.cli.*;
import net.lenni0451.optconfig.exceptions.CLIIncompatibleOptionException;
import net.lenni0451.optconfig.exceptions.CLIParserException;
import net.lenni0451.optconfig.index.ClassIndexer;
import net.lenni0451.optconfig.index.ConfigType;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.serializer.CLIConfigSerializer;
import net.lenni0451.optconfig.serializer.ConfigSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

public class CLIConfigLoader<C> {

    private final ConfigContext<C> context;
    private final ConfigIndex configIndex;

    public CLIConfigLoader(final ConfigContext<C> context) {
        this.context = context;
        this.configIndex = ClassIndexer.indexClassAndInit(
                context.getConfigInstance() == null ? ConfigType.STATIC : ConfigType.INSTANCED,
                context.getConfigLoader(),
                context.getConfigInstance(),
                CLIName.class,
                CLIAliases.class,
                CLIIgnore.class
        );
    }

    /**
     * Print CLI help to the given Appendable (e.g. {@code System.out}).
     *
     * @param out     The Appendable to print the help to
     * @param options Options for the help output
     * @throws CLIIncompatibleOptionException If an option is incompatible with the CLI
     */
    public void printCLIHelp(final Appendable out, final HelpOptions options) throws CLIIncompatibleOptionException {
        try {
            out.append(this.buildCLIHelp(options));
            out.append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Build CLI help using the given formatter.
     *
     * @param options Options for the help output
     * @return The built help string
     * @throws CLIIncompatibleOptionException If an option is incompatible with the CLI
     */
    public String buildCLIHelp(final HelpOptions options) throws CLIIncompatibleOptionException {
        List<CLIOption> cliOptions = new ArrayList<>();
        //Populate CLI options
        CLIConfigSerializer.parseCLIOptions(this.context.getConfigLoader(), this.context.getConfigInstance(), this.configIndex, this.context.getConfigInstance(), new Stack<>(), cliOptions);
        return CLIHelpBuilder.build(cliOptions, options);
    }

    /**
     * Load CLI options from the given arguments.
     *
     * @param args                    The CLI arguments
     * @param setNotReloadableOptions If true, not reloadable options will also be set from the CLI arguments
     * @return A list of unknown options and their values
     * @throws CLIIncompatibleOptionException If an option is incompatible with the CLI
     * @throws CLIParserException             If the user provided invalid CLI arguments
     */
    public List<UnknownOption> loadCLIOptions(final String[] args, final boolean setNotReloadableOptions) throws CLIIncompatibleOptionException, CLIParserException {
        List<CLIOption> cliOptions = new ArrayList<>();
        //Populate CLI options
        CLIConfigSerializer.parseCLIOptions(this.context.getConfigLoader(), this.context.getConfigInstance(), this.configIndex, this.context.getConfigInstance(), new Stack<>(), cliOptions);
        Map<String, Object> values = new HashMap<>();
        List<UnknownOption> unknownOptions = CLIParser.parse(this.context.getConfigLoader().getYaml(), cliOptions, args, values);
        ConfigSerializer.deserializeSection(this.context.getConfigLoader(), this.context.getConfigInstance(), this.configIndex, this.context.getConfigInstance(), values, !setNotReloadableOptions, null);
        return unknownOptions;
    }

}
