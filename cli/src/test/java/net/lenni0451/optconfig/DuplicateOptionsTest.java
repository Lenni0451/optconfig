package net.lenni0451.optconfig;

import net.lenni0451.optconfig.annotations.OptConfig;
import net.lenni0451.optconfig.annotations.Option;
import net.lenni0451.optconfig.annotations.cli.CLIName;
import net.lenni0451.optconfig.exceptions.CLIDuplicateOptionException;
import net.lenni0451.optconfig.provider.ConfigProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DuplicateOptionsTest {

    @Test
    void test() throws Throwable {
        ConfigLoader<Config> loader = new ConfigLoader<>(Config.class);
        ConfigContext<Config> context = loader.load(ConfigProvider.empty());
        CLIConfigLoader<Config> cliLoader = new CLIConfigLoader<>(context);
        assertThrows(CLIDuplicateOptionException.class, () -> cliLoader.loadCLIOptions(new String[]{}, true));
    }


    @OptConfig
    public static class Config {
        @Option("a")
        private String a;

        @Option("b")
        @CLIName("a")
        private String b;
    }

}
