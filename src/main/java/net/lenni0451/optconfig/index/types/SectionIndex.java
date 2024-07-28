package net.lenni0451.optconfig.index.types;

import lombok.ToString;
import net.lenni0451.optconfig.index.ConfigType;
import net.lenni0451.optconfig.index.DependencySorter;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@ApiStatus.Internal
public class SectionIndex {

    private final ConfigType configType;
    private final Class<?> clazz;
    private final List<ConfigOption> options = new ArrayList<>();
    private final Map<ConfigOption, SectionIndex> subSections = new HashMap<>();

    public SectionIndex(final ConfigType configType, final Class<?> clazz) {
        this.configType = configType;
        this.clazz = clazz;
    }

    public ConfigType getConfigType() {
        return this.configType;
    }

    public Class<?> getClazz() {
        return this.clazz;
    }

    public List<ConfigOption> getOptions() {
        return this.options;
    }

    public void addOption(final ConfigOption option) {
        this.options.add(option);
    }

    public Map<ConfigOption, SectionIndex> getSubSections() {
        return this.subSections;
    }

    public void addSubSection(final ConfigOption option, final SectionIndex section) {
        this.subSections.put(option, section);
    }

    public void sortOptions() {
        List<ConfigOption> sortedOptions = DependencySorter.sortOptions(this.options);
        this.options.clear();
        this.options.addAll(sortedOptions);
    }

    public boolean isEmpty() {
        return this.options.isEmpty() && this.subSections.values().stream().allMatch(SectionIndex::isEmpty);
    }

}
