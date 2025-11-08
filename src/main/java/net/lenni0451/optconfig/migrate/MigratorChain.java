package net.lenni0451.optconfig.migrate;

import net.lenni0451.optconfig.ConfigOptions;
import net.lenni0451.optconfig.index.types.ConfigIndex;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

/**
 * A list of migrators that are executed in the order they are added.<br>
 * Used when a direct migrator is not available.<br>
 * If the target version has no migrator, the default migrator is appended to the end of the list.
 * The default migrator adds missing values and removes deprecated values (if enabled in the {@link ConfigOptions}).
 */
@ApiStatus.Internal
public class MigratorChain implements ConfigMigrator {

    private final List<ConfigIndex.Migrator> migrators;

    public MigratorChain(final List<ConfigIndex.Migrator> migrators) {
        this.migrators = migrators;
    }

    @Override
    public void migrate(int currentVersion, Map<String, Object> loadedValues) {
        for (ConfigIndex.Migrator migrator : this.migrators) {
            migrator.instance().migrate(migrator.from(), loadedValues);
        }
    }

}
