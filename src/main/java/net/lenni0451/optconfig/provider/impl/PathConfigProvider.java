package net.lenni0451.optconfig.provider.impl;

import net.lenni0451.optconfig.provider.ConfigProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathConfigProvider implements ConfigProvider {

    private final Path path;

    public PathConfigProvider(final Path path) {
        this.path = path;
    }

    @Override
    public String load() throws IOException {
        return new String(Files.readAllBytes(this.path), StandardCharsets.UTF_8);
    }

    @Override
    public void save(String content) throws IOException {
        if (this.path.getParent() != null) Files.createDirectories(this.path.getParent()); //Create parent directories if they don't exist
        Files.write(this.path, content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean exists() {
        return Files.exists(this.path);
    }

}