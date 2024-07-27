package net.lenni0451.optconfig.index;

import net.lenni0451.optconfig.annotations.OptConfig;
import net.lenni0451.optconfig.index.types.ConfigOption;
import net.lenni0451.optconfig.index.types.SectionIndex;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

@ApiStatus.Internal
public class ConfigDiff {

    public static ConfigDiff diff(final SectionIndex sectionIndex, final Map<String, Object> values) {
        ConfigDiff configDiff = new ConfigDiff();
        Set<String> missedKeys = new HashSet<>(values.keySet());
        for (ConfigOption option : sectionIndex.getOptions()) {
            if (!missedKeys.remove(option.getName())) {
                //If a key is not present in the values, it was added to the config
                configDiff.addedKeys.add(option.getName());
                continue;
            }
            if (sectionIndex.getSubSections().containsKey(option)) {
                Object value = values.get(option.getName());
                if (!(value instanceof Map)) continue; //If a subsection is not a map, let the deserializer deal with it
                Map<String, Object> subSectionValues = (Map<String, Object>) value;
                ConfigDiff subSectionDiff = ConfigDiff.diff(sectionIndex.getSubSections().get(option), subSectionValues);
                if (!subSectionDiff.isEmpty()) configDiff.subSections.put(option.getName(), subSectionDiff);
            }
        }
        missedKeys.remove(OptConfig.CONFIG_VERSION_OPTION); //Ignore the version key
        configDiff.removedKeys.addAll(missedKeys); //All keys that are left were removed from the config
        return configDiff;
    }


    private final List<String> addedKeys;
    private final List<String> removedKeys;
    private final List<String> invalidKeys; //Used by the deserializer if an option has an invalid value
    private final Map<String, ConfigDiff> subSections;

    private ConfigDiff() {
        this.addedKeys = new ArrayList<>();
        this.removedKeys = new ArrayList<>();
        this.invalidKeys = new ArrayList<>();
        this.subSections = new HashMap<>();
    }

    public List<String> getAddedKeys() {
        return this.addedKeys;
    }

    public List<String> getRemovedKeys() {
        return this.removedKeys;
    }

    public List<String> getInvalidKeys() {
        return this.invalidKeys;
    }

    public Map<String, ConfigDiff> getSubSections() {
        return this.subSections;
    }

    public boolean isEmpty() {
        return this.addedKeys.isEmpty() && this.removedKeys.isEmpty() && invalidKeys.isEmpty() && this.subSections.isEmpty();
    }

}
