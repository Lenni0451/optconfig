package net.lenni0451.optconfig.provider.impl;

import net.lenni0451.optconfig.provider.ConfigProvider;

import java.io.IOException;

/**
 * A config provider that does nothing.<br>
 * Can be used if the config is only used for CLI purposes.
 */
public class EmptyConfigProvider implements ConfigProvider {

    @Override
    public byte[] load() throws IOException {
        return new byte[0];
    }

    @Override
    public void save(byte[] content) {
    }

    @Override
    public boolean exists() {
        return false;
    }

}
