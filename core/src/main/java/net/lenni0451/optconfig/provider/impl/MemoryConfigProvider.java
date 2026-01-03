package net.lenni0451.optconfig.provider.impl;

import net.lenni0451.optconfig.provider.ConfigProvider;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MemoryConfigProvider implements ConfigProvider {

    private final Supplier<byte[]> contentSupplier;
    private final Consumer<byte[]> contentConsumer;

    public MemoryConfigProvider(final Supplier<byte[]> contentSupplier, final Consumer<byte[]> contentConsumer) {
        this.contentSupplier = contentSupplier;
        this.contentConsumer = contentConsumer;
    }

    @Override
    public byte[] load() {
        return this.contentSupplier.get();
    }

    @Override
    public void save(byte[] content) {
        this.contentConsumer.accept(content);
    }

    @Override
    public boolean exists() {
        return this.load().length > 0;
    }

}
