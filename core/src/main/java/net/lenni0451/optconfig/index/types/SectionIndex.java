package net.lenni0451.optconfig.index.types;

import lombok.ToString;
import net.lenni0451.optconfig.ConfigLoader;
import net.lenni0451.optconfig.exceptions.DuplicateOptionException;
import net.lenni0451.optconfig.index.ConfigType;
import net.lenni0451.optconfig.index.DependencySorter;
import net.lenni0451.optconfig.utils.ReflectionUtils;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.*;

@ToString
@ApiStatus.Internal
public class SectionIndex {

    private final ConfigType configType;
    private final Class<?> clazz;
    private final List<String> optionsOrder = new ArrayList<>();
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

    public List<String> getOptionsOrder() {
        return this.optionsOrder;
    }

    public List<ConfigOption> getOptions() {
        return this.options;
    }

    @Nullable
    public ConfigOption getOption(final String name) {
        return this.options.stream().filter(option -> option.getName().equals(name)).findFirst().orElse(null);
    }

    public void addOption(final ConfigOption option) {
        if (this.getOption(option.getName()) != null) throw new DuplicateOptionException(option.getName());
        this.optionsOrder.add(option.getName());
        this.options.add(option);
    }

    public Map<ConfigOption, SectionIndex> getSubSections() {
        return this.subSections;
    }

    public void addSubSection(final ConfigOption option, final SectionIndex section) {
        this.subSections.put(option, section);
    }

    public void sortOptions() {
        //Sort options by user given order (display order)
        this.options.stream()
                .filter(option -> option.getOrder() >= 0)
                .sorted(Comparator.comparingInt(ConfigOption::getOrder))
                .forEach(option -> {
                    this.optionsOrder.remove(option.getName());
                    this.optionsOrder.add(Math.min(option.getOrder(), this.optionsOrder.size()), option.getName());
                });

        //Sort options by dependencies (load order)
        List<ConfigOption> sortedOptions = DependencySorter.sortOptions(this.options);
        this.options.clear();
        this.options.addAll(sortedOptions);
    }

    public boolean isEmpty() {
        return this.options.isEmpty() && this.subSections.values().stream().allMatch(SectionIndex::isEmpty);
    }

    public void initSubSections(final ConfigLoader<?> configLoader, final Object parentInstance) {
        for (ConfigOption subSection : this.subSections.keySet()) {
            Object sectionInstance = subSection.getFieldAccess().getValue(parentInstance);
            if (sectionInstance == null) {
                sectionInstance = ReflectionUtils.instantiate(configLoader, subSection.getFieldAccess().getType());
                subSection.getFieldAccess().setValue(parentInstance, sectionInstance);
            }
        }
    }

    /**
     * Get the current values of all config option.<br>
     * The values are flattened into a map without any section information.<br>
     * Sections themselves are not included in the map.
     *
     * @param sectionInstance The instance of the section
     * @return The current values of all config options
     */
    public Map<ConfigOption, Object> getCurrentValues(final Object sectionInstance) {
        Map<ConfigOption, Object> values = new HashMap<>();
        for (ConfigOption option : this.options) {
            if (this.subSections.containsKey(option)) {
                Object subSectionInstance = null;
                if (sectionInstance != null) subSectionInstance = option.getFieldAccess().getValue(sectionInstance);
                values.putAll(this.subSections.get(option).getCurrentValues(subSectionInstance));
            } else {
                values.put(option, option.getFieldAccess().getValue(sectionInstance));
            }
        }
        return values;
    }

    public void merge(final SectionIndex other) {
        for (String name : other.optionsOrder) {
            ConfigOption option = other.getOption(name);
            if (option != null && !this.optionsOrder.contains(name)) {
                this.addOption(option);
                SectionIndex subSection = other.subSections.get(option);
                if (subSection != null) this.addSubSection(option, subSection);
            }
        }
    }

}
