package net.lenni0451.optconfig;

import net.lenni0451.optconfig.annotations.cli.CLIAliases;
import net.lenni0451.optconfig.annotations.cli.CLIIgnore;
import net.lenni0451.optconfig.annotations.cli.CLIName;
import net.lenni0451.optconfig.cli.CLIHelpBuilder;
import net.lenni0451.optconfig.cli.CLIOption;
import net.lenni0451.optconfig.cli.CLIParser;
import net.lenni0451.optconfig.cli.HelpOptions;
import net.lenni0451.optconfig.exceptions.CLIIncompatibleOptionException;
import net.lenni0451.optconfig.exceptions.CLIParserException;
import net.lenni0451.optconfig.index.ClassIndexer;
import net.lenni0451.optconfig.index.ConfigType;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.serializer.CLIConfigSerializer;
import net.lenni0451.optconfig.serializer.ConfigSerializer;
import net.lenni0451.optconfig.utils.HelpFormatter;

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
        HelpFormatter formatter = HelpFormatter.builder().columnCount(2).headers("Option", "Description").build();
        this.buildCLIHelp(formatter, options);
        try {
            out.append(formatter.toString());
            out.append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Build CLI help using the given formatter.
     *
     * @param formatter The help formatter
     * @param options   Options for the help output
     * @throws CLIIncompatibleOptionException If an option is incompatible with the CLI
     */
    public void buildCLIHelp(final HelpFormatter formatter, final HelpOptions options) throws CLIIncompatibleOptionException {
        if (formatter.getColumnCount() < 2) {
            throw new IllegalArgumentException("CLI help formatter must have at least 2 columns");
        }
        List<CLIOption> cliOptions = new ArrayList<>();
        //Populate CLI options
        CLIConfigSerializer.parseCLIOptions(this.context.getConfigLoader(), this.context.getConfigInstance(), this.configIndex, this.context.getConfigInstance(), new Stack<>(), cliOptions);
        CLIHelpBuilder.build(formatter, cliOptions, options);
    }

    /**
     * Load CLI options from the given arguments.
     *
     * @param args                    The CLI arguments
     * @param setNotReloadableOptions If true, not reloadable options will also be set from the CLI arguments
     * @throws CLIIncompatibleOptionException If an option is incompatible with the CLI
     * @throws CLIParserException             If the user provided invalid CLI arguments
     */
    public void loadCLIOptions(final String[] args, final boolean setNotReloadableOptions) throws CLIIncompatibleOptionException, CLIParserException {
        List<CLIOption> cliOptions = new ArrayList<>();
        //Populate CLI options
        CLIConfigSerializer.parseCLIOptions(this.context.getConfigLoader(), this.context.getConfigInstance(), this.configIndex, this.context.getConfigInstance(), new Stack<>(), cliOptions);
        Map<String, Object> values = new HashMap<>();
        CLIParser.parse(this.context.getConfigLoader().getYaml(), cliOptions, args, values);
        ConfigSerializer.deserializeSection(this.context.getConfigLoader(), this.context.getConfigInstance(), this.configIndex, this.context.getConfigInstance(), values, !setNotReloadableOptions, null);
    }

}
