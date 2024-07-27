package net.lenni0451.optconfig.migrate;

import net.lenni0451.optconfig.index.types.ConfigIndex;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class MigratorChain implements IConfigMigrator {

    private final List<ConfigIndex.Migrator> migrators;

    public MigratorChain(final List<ConfigIndex.Migrator> migrators) {
        this.migrators = migrators;
    }

    @Override
    public void migrate(int currentVersion, Map<String, Object> loadedValues) {
        for (ConfigIndex.Migrator migrator : this.migrators) migrator.getInstance().migrate(migrator.getFrom(), loadedValues);
    }

}
