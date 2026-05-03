package net.lenni0451.optconfig;

import net.lenni0451.optconfig.annotations.OptConfig;
import net.lenni0451.optconfig.annotations.Option;
import net.lenni0451.optconfig.exceptions.CLIMissingOptionValueException;
import net.lenni0451.optconfig.provider.ConfigProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValueOptionWithoutValueTest {

    @Test
    void test() throws Throwable {
        ConfigLoader<Config> loader = new ConfigLoader<>(Config.class);
        ConfigContext<Config> context = loader.load(ConfigProvider.empty());
        loader.getConfigOptions().setResetInvalidOptions(false);
        CLIConfigLoader<Config> cliLoader = new CLIConfigLoader<>(context);
        Assertions.assertThrows(CLIMissingOptionValueException.class, () -> cliLoader.loadCLIOptions(new String[]{"-a"}, true));
    }


    @OptConfig
    public static class Config {
        @Option("a")
        public int a;
    }
}
