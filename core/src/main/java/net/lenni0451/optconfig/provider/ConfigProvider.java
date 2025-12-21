package net.lenni0451.optconfig.provider;

import net.lenni0451.optconfig.provider.impl.EmptyConfigProvider;
import net.lenni0451.optconfig.provider.impl.MemoryConfigProvider;
import net.lenni0451.optconfig.provider.impl.PathConfigProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A provider to load and save the config content.
 */
public interface ConfigProvider {

    /**
     * Create a new {@link PathConfigProvider} with the given file.
     *
     * @param file The file to the config file
     * @return The created {@link PathConfigProvider}
     */
    static PathConfigProvider file(final File file) {
        return path(file.toPath());
    }

    /**
     * Create a new {@link PathConfigProvider} with the given path.
     *
     * @param path The path to the config file
     * @return The created {@link PathConfigProvider}
     */
    static PathConfigProvider path(final Path path) {
        return new PathConfigProvider(path);
    }

    /**
     * Create a new {@link MemoryConfigProvider} with the given content and consumer.
     *
     * @param content         The content of the config file
     * @param contentConsumer The consumer to set the content
     * @return The created {@link MemoryConfigProvider}
     */
    static MemoryConfigProvider memory(final String content, final Consumer<String> contentConsumer) {
        return memory(() -> content, contentConsumer);
    }

    /**
     * Create a new {@link MemoryConfigProvider} with the given content supplier and consumer.
     *
     * @param contentSupplier The supplier to get the content
     * @param contentConsumer The consumer to set the content
     * @return The created {@link MemoryConfigProvider}
     */
    static MemoryConfigProvider memory(final Supplier<String> contentSupplier, final Consumer<String> contentConsumer) {
        return new MemoryConfigProvider(contentSupplier, contentConsumer);
    }

    /**
     * Create a new {@link EmptyConfigProvider}.<br>
     * This provider always returns an empty string and does nothing on save.<br>
     * Can be used if the config is only used for CLI purposes.
     *
     * @return The created {@link EmptyConfigProvider}
     */
    static EmptyConfigProvider empty() {
        return new EmptyConfigProvider();
    }


    /**
     * Load the content of the config file.<br>
     * This method may be called multiple times.
     *
     * @return The content of the config file
     * @throws IOException If an I/O error occurs
     */
    String load() throws IOException;

    /**
     * Save the content to the config file.
     *
     * @param content The content to save
     * @throws IOException If an I/O error occurs
     */
    void save(final String content) throws IOException;

    /**
     * @return If a config already exists
     */
    boolean exists();

}
