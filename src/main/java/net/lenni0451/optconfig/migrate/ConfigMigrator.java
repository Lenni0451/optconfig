package net.lenni0451.optconfig.migrate;

import net.lenni0451.optconfig.annotations.Migrator;

import java.util.Map;
import java.util.Optional;

/**
 * A migrator that can be used to migrate the config values to a new version.
 */
public interface ConfigMigrator {

    /**
     * Migrates the config values to the new version.<br>
     * The target version is specified when registering the migrator.
     *
     * @param currentVersion The current version of the config
     * @param loadedValues   The loaded values of the config
     * @see Migrator
     */
    void migrate(final int currentVersion, final Map<String, Object> loadedValues);

    /**
     * Get a section of the values map by the given path.
     *
     * @param values The values map
     * @param path   The path to the section
     * @return The section or an empty optional if the section does not exist
     */
    default Optional<Map<String, Object>> getSection(final Map<String, Object> values, final String... path) {
        Map<String, Object> section = values;
        for (String part : path) {
            if (!section.containsKey(part)) return Optional.empty();
            try {
                section = (Map<String, Object>) section.get(part);
            } catch (ClassCastException e) {
                return Optional.empty();
            }
        }
        return Optional.of(section);
    }

}
