package net.lenni0451.optconfig;

import net.lenni0451.optconfig.annotations.OptConfig;
import net.lenni0451.optconfig.annotations.Option;
import net.lenni0451.optconfig.annotations.Section;
import net.lenni0451.optconfig.provider.ConfigProvider;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapTest {

    @Test
    void test() throws Throwable {
        ConfigLoader<Config> loader = new ConfigLoader<>(Config.class);
        ConfigContext<Config> context = loader.load(ConfigProvider.empty());
        CLIConfigLoader<Config> cliLoader = new CLIConfigLoader<>(context);
        cliLoader.loadCLIOptions(new String[]{
                "-a", "Hello, World!",

                "-b.key1", "val1",
                "-b.key2", "val2",

                "-c.key1", "val3",
                "-c.key1", "val4",
                "-c.key2", "val5",
                "-c.key2", "val6",
                "-c", "key3=val7",

                "--SubSection.d", "key4=val8",
                "--SubSection.d.key5", "val9",
        }, true);
        assertEquals("Hello, World!", context.getConfigInstance().a);
        assertEquals(Map.of("key1", "val1", "key2", "val2"), context.getConfigInstance().b);
        assertEquals(Map.of("key1", "val4", "key2", "val6", "key3", "val7"), context.getConfigInstance().c);
        assertEquals(Map.of("key4", "val8", "key5", "val9"), context.getConfigInstance().subSection.d);
    }


    @OptConfig
    public static class Config {
        @Option("a")
        public String a;

        @Option("b")
        public Map<String, String> b = new HashMap<>();

        @Option("c")
        public Map<String, String> c = new HashMap<>();

        @Option("SubSection")
        public Config.SubSection subSection;

        @Section
        public static class SubSection {
            @Option("d")
            public Map<String, String> d = Map.of("hello", "world", "bye", "bye");
        }
    }


}
