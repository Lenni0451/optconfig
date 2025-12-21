package net.lenni0451.optconfig.provider.impl;

import net.lenni0451.optconfig.provider.ConfigProvider;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MemoryConfigProvider implements ConfigProvider {

    private final Supplier<String> contentSupplier;
    private final Consumer<String> contentConsumer;

    public MemoryConfigProvider(final Supplier<String> contentSupplier, final Consumer<String> contentConsumer) {
        this.contentSupplier = contentSupplier;
        this.contentConsumer = contentConsumer;
    }

    @Override
    public String load() {
        return this.contentSupplier.get();
    }

    @Override
    public void save(String content) {
        this.contentConsumer.accept(content);
    }

    @Override
    public boolean exists() {
        return !this.load().isEmpty();
    }

}
