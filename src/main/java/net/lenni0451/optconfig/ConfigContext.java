package net.lenni0451.optconfig;

import net.lenni0451.optconfig.index.types.ConfigIndex;
import net.lenni0451.optconfig.provider.ConfigProvider;

import java.io.IOException;

public class ConfigContext<C> {

    private final ConfigLoader<C> configLoader;
    private final C configInstance;
    private final ConfigProvider configProvider;
    private final ConfigIndex configIndex;

    ConfigContext(final ConfigLoader<C> configLoader, final C configInstance, final ConfigProvider configProvider, final ConfigIndex configIndex) {
        this.configLoader = configLoader;
        this.configInstance = configInstance;
        this.configProvider = configProvider;
        this.configIndex = configIndex;
    }

    /**
     * @return The config class
     */
    public Class<C> getConfigClass() {
        return this.configLoader.configClass;
    }

    /**
     * @return The config instance (null for static configs)
     */
    public C getConfigInstance() {
        return this.configInstance;
    }

    /**
     * Reload all reloadable config values.
     *
     * @throws IOException            If an I/O error occurs
     * @throws IllegalAccessException If the config class or options are not accessible
     */
    public void reload() throws IOException, IllegalAccessException {
        this.configLoader.parseSection(this.configIndex, this.configInstance, this.configProvider, true);
    }

    /**
     * Save the current config values to the config file.
     *
     * @throws IOException            If an I/O error occurs
     * @throws IllegalAccessException If the config class or options are not accessible
     */
    public void save() throws IOException, IllegalAccessException {
        //TODO
    }

}
