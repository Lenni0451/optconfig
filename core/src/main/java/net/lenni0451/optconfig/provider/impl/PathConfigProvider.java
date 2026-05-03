package net.lenni0451.optconfig.provider.impl;

import net.lenni0451.optconfig.provider.ConfigProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathConfigProvider implements ConfigProvider {

    private final Path path;
    private final Path tempPath;
    private final boolean readOnly;

    public PathConfigProvider(final Path path, final boolean readOnly) {
        this.path = path;
        this.tempPath = path.resolveSibling(path.getFileName() + ".tmp");
        this.readOnly = readOnly;
    }

    @Override
    public byte[] load() throws IOException {
        return Files.readAllBytes(this.path);
    }

    @Override
    public void save(byte[] content) throws IOException {
        if (this.readOnly) return;
        if (this.path.getParent() != null) Files.createDirectories(this.path.getParent()); //Create parent directories if they don't exist
        Files.write(this.tempPath, content);
        Files.move(this.tempPath, this.path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public boolean exists() {
        return Files.exists(this.path);
    }

}
