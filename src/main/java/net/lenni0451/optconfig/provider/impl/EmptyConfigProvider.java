package net.lenni0451.optconfig.provider.impl;

import net.lenni0451.optconfig.provider.ConfigProvider;

import java.io.IOException;

public class EmptyConfigProvider implements ConfigProvider {

    @Override
    public String load() throws IOException {
        return "";
    }

    @Override
    public void save(String content) {
    }

    @Override
    public boolean exists() {
        return false;
    }

}
