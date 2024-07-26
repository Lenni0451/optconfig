package net.lenni0451.optconfig.migrate;

import java.util.List;

public class MigratorChain implements IConfigMigrator {

    private final List<IConfigMigrator> migrators;

    public MigratorChain(final List<IConfigMigrator> migrators) {
        this.migrators = migrators;
    }

}
