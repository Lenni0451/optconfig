package net.lenni0451.optconfig;

import net.lenni0451.optconfig.provider.ConfigProvider;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapConfigLoaderTest {

    @Test
    void nonExistingFile() {
        MapConfigLoader loader = new MapConfigLoader(ConfigProvider.empty());
        Map<String, Object> config = assertDoesNotThrow(loader::load);
        assertNotNull(config);
        assertTrue(config.isEmpty());
        assertDoesNotThrow(() -> config.put("key", "value")); // Mutability check
        assertDoesNotThrow(() -> loader.save(config));
    }

    @Test
    void emptyFile() {
        MapConfigLoader loader = new MapConfigLoader(new ConfigProvider() {
            @Override
            public byte[] load() {
                return new byte[0];
            }

            @Override
            public void save(final byte[] content) {
            }

            @Override
            public boolean exists() {
                return true;
            }
        });
        Map<String, Object> config = assertDoesNotThrow(loader::load);
        assertNotNull(config);
        assertTrue(config.isEmpty());
        assertDoesNotThrow(() -> config.put("key", "value")); // Mutability check
        assertDoesNotThrow(() -> loader.save(config));
    }

}
