package net.lenni0451.optconfig.index;

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
                Map<String, Object> subSectionValues = (Map<String, Object>) values.get(option.getName());
                ConfigDiff subSectionDiff = ConfigDiff.diff(sectionIndex.getSubSections().get(option), subSectionValues);
                if (!subSectionDiff.isEmpty()) configDiff.subSections.put(option.getName(), subSectionDiff);
            }
        }
        configDiff.removedKeys.addAll(missedKeys); //All keys that are left were removed from the config
        return configDiff;
    }


    private final List<String> addedKeys;
    private final List<String> removedKeys;
    private final Map<String, ConfigDiff> subSections;

    private ConfigDiff() {
        this.addedKeys = new ArrayList<>();
        this.removedKeys = new ArrayList<>();
        this.subSections = new HashMap<>();
    }

    public List<String> getAddedKeys() {
        return this.addedKeys;
    }

    public List<String> getRemovedKeys() {
        return this.removedKeys;
    }

    public Map<String, ConfigDiff> getSubSections() {
        return this.subSections;
    }

    public boolean isEmpty() {
        return this.addedKeys.isEmpty() && this.removedKeys.isEmpty() && this.subSections.isEmpty();
    }

}
