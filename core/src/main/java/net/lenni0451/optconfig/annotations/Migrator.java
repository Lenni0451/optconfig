package net.lenni0451.optconfig.annotations;

import net.lenni0451.optconfig.annotations.internal.Migrators;
import net.lenni0451.optconfig.migrate.ConfigMigrator;

import java.lang.annotation.*;

/**
 * Register a migrator class for a config class.<br>
 * If a migrator for the current version isn't found, the default values will be used for new options and old options will be removed.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Migrators.class)
public @interface Migrator {

    /**
     * @return The version from which the migrator starts migrating
     */
    int from();

    /**
     * @return The version to which the migrator is migrating
     */
    int to();

    /**
     * @return The migrator class
     */
    Class<? extends ConfigMigrator> migrator();

}
