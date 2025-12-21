package net.lenni0451.optconfig.migrate;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfigMigratorTest {

    private static final ConfigMigrator MIGRATOR = (currentVersion, loadedValues) -> {};

    @Test
    void getSection() {
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map1_1 = new HashMap<>();
        Map<String, Object> map1_2 = new HashMap<>();
        Map<String, Object> map1_1_1 = new HashMap<>();
        map1.put("map1_1", map1_1);
        map1.put("map1_2", map1_2);
        map1_1.put("map1_1_1", map1_1_1);
        map1.put("test", "test");
        map1_1.put("test", "test");
        map1_2.put("test", "test");
        map1_1_1.put("test", "test");

        assertEquals(map1_1, MIGRATOR.getSection(map1, "map1_1").orElse(null));
        assertEquals(map1_2, MIGRATOR.getSection(map1, "map1_2").orElse(null));
        assertEquals(map1_1_1, MIGRATOR.getSection(map1, "map1_1", "map1_1_1").orElse(null));
        assertNull(MIGRATOR.getSection(map1, "map1_1", "test").orElse(null));
        assertNull(MIGRATOR.getSection(map1, "map1_2", "test").orElse(null));
        assertNull(MIGRATOR.getSection(map1, "map1_1", "map1_1_1", "test").orElse(null));
        assertNull(MIGRATOR.getSection(map1, "a").orElse(null));
    }

}
