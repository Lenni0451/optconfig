package net.lenni0451.optconfig;

import lombok.Getter;
import net.lenni0451.optconfig.access.types.FieldAccess;
import net.lenni0451.optconfig.cli.CLIHelpBuilder;
import net.lenni0451.optconfig.cli.CLIOption;
import net.lenni0451.optconfig.cli.CLIParser;
import net.lenni0451.optconfig.cli.HelpOptions;
import net.lenni0451.optconfig.exceptions.CLIIncompatibleOptionException;
import net.lenni0451.optconfig.exceptions.CLIParserException;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.index.types.ConfigOption;
import net.lenni0451.optconfig.provider.ConfigProvider;
import net.lenni0451.optconfig.serializer.ConfigSerializer;
import net.lenni0451.optconfig.utils.HelpFormatter;
import net.lenni0451.optconfig.utils.YamlUtils;
import org.yaml.snakeyaml.nodes.MappingNode;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * The context of a loaded config instance.<br>
 * Allows reloading and saving the config as well as loading and printing CLI options.
 *
 * @param <C> The config class type
 */
public class ConfigContext<C> {

    @Getter
    private final ConfigLoader<C> configLoader;
    @Getter
    private final C configInstance;
    @Getter
    private final ConfigProvider configProvider;
    private final ConfigIndex configIndex;
    final Map<ConfigOption, Object> defaultValues;

    ConfigContext(final ConfigLoader<C> configLoader, final C configInstance, final ConfigProvider configProvider, final ConfigIndex configIndex) {
        this.configLoader = configLoader;
        this.configInstance = configInstance;
        this.configProvider = configProvider;
        this.configIndex = configIndex;
        this.defaultValues = configIndex.getCurrentValues(configInstance);

        this.setContextField();
    }

    private void setContextField() {
        try {
            FieldAccess contextField = null;
            for (FieldAccess field : this.configLoader.getConfigOptions().getClassAccessFactory().create(this.configLoader.getConfigClass()).getFields()) {
                if (!this.configIndex.getConfigType().matches(field.getModifiers())) continue;
                if (!ConfigContext.class.isAssignableFrom(field.getType())) continue;
                contextField = field;
                break;
            }
            if (contextField != null) contextField.setValue(this.configInstance, this);
        } catch (Throwable ignored) {
            //Not critical
        }
    }

    /**
     * Reload all reloadable config values.
     *
     * @throws IOException            If an I/O error occurs
     * @throws IllegalAccessException If the config class or options are not accessible
     */
    public void reload() throws IOException, IllegalAccessException {
        this.configLoader.parseSection(this.configIndex, this, this.configInstance, this.configProvider, true);
    }

    /**
     * Save the current config values to the config file.
     *
     * @throws IOException            If an I/O error occurs
     * @throws IllegalAccessException If the config class or options are not accessible
     */
    public void save() throws IOException, IllegalAccessException {
        MappingNode serializedSection = ConfigSerializer.serializeSection(this.configLoader, this.defaultValues, this.configInstance, this.configIndex, this.configInstance);
        if (this.configLoader.getConfigOptions().isRewriteConfig()) {
            //If the config should be rewritten, just save the serialized section
            this.configLoader.save(serializedSection, this.configProvider);
        } else {
            //If the config should not be rewritten, copy over comments and formatting
            MappingNode readNode = (MappingNode) this.configLoader.getYaml().compose(new StringReader(this.configProvider.load()));
            YamlUtils.copyValues(serializedSection, readNode);
            this.configLoader.save(readNode, this.configProvider);
        }
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
        ConfigSerializer.parseCLIOptions(this.configLoader, this.configInstance, this.configIndex, this.configInstance, new Stack<>(), cliOptions);
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
        ConfigSerializer.parseCLIOptions(this.configLoader, this.configInstance, this.configIndex, this.configInstance, new Stack<>(), cliOptions);
        Map<String, Object> values = new HashMap<>();
        CLIParser.parse(this.configLoader.getYaml(), cliOptions, args, values);
        ConfigSerializer.deserializeSection(this.configLoader, this.configInstance, this.configIndex, this.configInstance, values, !setNotReloadableOptions, null);
    }

}
