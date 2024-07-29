package net.lenni0451.optconfig.migrate;

import net.lenni0451.optconfig.annotations.Migrator;

import java.util.Map;

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

}
