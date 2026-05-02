package net.lenni0451.optconfig;

import net.lenni0451.optconfig.annotations.OptConfig;
import net.lenni0451.optconfig.annotations.Option;
import net.lenni0451.optconfig.annotations.Section;
import net.lenni0451.optconfig.provider.ConfigProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ListTest {

    @Test
    void test() throws Throwable {
        ConfigLoader<Config> loader = new ConfigLoader<>(Config.class);
        ConfigContext<Config> context = loader.load(ConfigProvider.empty());
        CLIConfigLoader<Config> cliLoader = new CLIConfigLoader<>(context);
        cliLoader.loadCLIOptions(new String[]{
                "-a", "val1",
                "-a", "val2",

                "--SubSection.b", "val3",
                "--SubSection.b", "val4",
        }, true);
        assertEquals(List.of("val1", "val2"), context.getConfigInstance().a);
        assertArrayEquals(new String[]{"val3", "val4"}, context.getConfigInstance().subSection.b);
    }


    @OptConfig
    public static class Config {
        @Option("a")
        public List<String> a = new ArrayList<>();

        @Option("SubSection")
        public SubSection subSection;

        @Section
        public static class SubSection {
            @Option("b")
            public String[] b = {};
        }
    }

}
