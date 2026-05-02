package net.lenni0451.optconfig;

import net.lenni0451.optconfig.annotations.OptConfig;
import net.lenni0451.optconfig.annotations.Option;
import net.lenni0451.optconfig.annotations.Section;
import net.lenni0451.optconfig.annotations.cli.CLIName;
import net.lenni0451.optconfig.provider.ConfigProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OmitSectionTest {

    @Test
    void test() throws Throwable {
        ConfigLoader<Config> loader = new ConfigLoader<>(Config.class);
        ConfigContext<Config> context = loader.load(ConfigProvider.empty());
        CLIConfigLoader<Config> cliLoader = new CLIConfigLoader<>(context);
        cliLoader.loadCLIOptions(new String[]{
                "-a", "val1",
                "-b", "val2",
        }, true);
        assertEquals("val1", context.getConfigInstance().a);
        assertEquals("val2", context.getConfigInstance().subSection.b);
    }


    @OptConfig
    public static class Config {
        @Option("a")
        public String a;

        @Option("SubSection")
        public SubSection subSection;

        @Section
        public static class SubSection {
            @Option("b")
            @CLIName(value = "b", omitSection = true)
            public String b;
        }
    }

}
