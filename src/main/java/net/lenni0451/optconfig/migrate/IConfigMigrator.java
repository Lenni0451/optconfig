package net.lenni0451.optconfig.migrate;

import java.util.Map;

public interface IConfigMigrator {

    void migrate(final int currentVersion, final Map<String, Object> loadedValues);

}
