package net.lenni0451.optconfig.index.types;

import lombok.ToString;
import lombok.Value;
import net.lenni0451.optconfig.annotations.OptConfig;
import net.lenni0451.optconfig.index.ConfigType;
import net.lenni0451.optconfig.migrate.IConfigMigrator;
import net.lenni0451.optconfig.migrate.MigratorChain;
import net.lenni0451.optconfig.utils.ReflectionUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

@ApiStatus.Internal
@ToString(callSuper = true)
public class ConfigIndex extends SectionIndex {

    private final int version;
    private final String[] header;
    private final Map<MigratorIndex, Class<? extends IConfigMigrator>> migrators;

    public ConfigIndex(final ConfigType configType, final Class<?> clazz, final OptConfig optConfig) {
        super(configType, clazz);
        this.version = optConfig.version();
        this.header = optConfig.header();
        this.migrators = new HashMap<>();
    }

    public int getVersion() {
        return this.version;
    }

    public String[] getHeader() {
        return this.header;
    }

    public Map<MigratorIndex, Class<? extends IConfigMigrator>> getMigrators() {
        return this.migrators;
    }

    public void addMigrator(final int from, final int to, final Class<? extends IConfigMigrator> migrator) {
        if (from == to) throw new IllegalStateException("Migrator " + migrator.getName() + " has the same from and to version");
        if (to < from) throw new IllegalStateException("Migrator " + migrator.getName() + " has a higher from version than the to version");
        this.migrators.put(new MigratorIndex(from, to), migrator);
    }

    public Migrator searchMigrator(final int from, final int to) {
        MigratorIndex index = new MigratorIndex(from, to);
        if (this.migrators.containsKey(index)) return new Migrator(from, to, ReflectionUtils.instantiate(this.migrators.get(index)));
        //No matching migrator found, try to find the best fitting ones
        //The goal is getting from the current version to the target version
        //If multiple migrators are found, they must be applied in the correct order
        //A possible chain could be: 1 -> 2, 2 -> 4, 4 -> 5
        //The chain must start with the current version. The end version can be lower than the target version, but must not exceed it

        List<Migrator> migratorChain = new ArrayList<>();
        int[] currentVersion = {from};
        while (currentVersion[0] < to) {
            //Find the next migrator. The biggest version jump is the best fitting one
            Map.Entry<MigratorIndex, Class<? extends IConfigMigrator>> migrator = this.migrators.entrySet().stream()
                    .filter(entry -> entry.getKey().from == currentVersion[0] && entry.getKey().to <= to)
                    .max(Comparator.comparingInt(e -> e.getKey().to - e.getKey().from))
                    .orElse(null);
            if (migrator == null) {
                //No fitting migrator was found
                //Let the default migrator (DiffMerger) handle the rest
                break;
            } else {
                //The next migrator was found
                //Add it to the chain and try to find the next one
                migratorChain.add(new Migrator(currentVersion[0], migrator.getKey().to, ReflectionUtils.instantiate(migrator.getValue())));
                currentVersion[0] = migrator.getKey().to;
            }
        }
        return new Migrator(from, currentVersion[0], new MigratorChain(migratorChain));
    }


    @Value
    @ApiStatus.Internal
    public static class MigratorIndex {
        int from;
        int to;
    }

    @Value
    @ApiStatus.Internal
    public static class Migrator {
        int from;
        int to;
        IConfigMigrator instance;
    }

}
