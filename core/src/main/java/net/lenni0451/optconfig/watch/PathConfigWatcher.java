package net.lenni0451.optconfig.watch;

import net.lenni0451.optconfig.utils.ThreadRunner;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class PathConfigWatcher implements ConfigWatcher {

    private static final WatchEvent.Kind<?>[] WATCH_EVENTS = {
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY
    };
    private static final Duration DEBOUNCE_DURATION = Duration.of(250, ChronoUnit.MILLIS);

    private final Path path;
    private final ChangeListener changeListener;
    private final ErrorHandler errorHandler;
    private final WatchService watchService;
    private final Thread watchThread;

    public PathConfigWatcher(final Path path, final ChangeListener changeListener) throws IOException {
        this(path, changeListener, Throwable::printStackTrace);
    }

    public PathConfigWatcher(final Path path, final ChangeListener changeListener, final ErrorHandler errorHandler) throws IOException {
        Path parentPath = path.toAbsolutePath().normalize().getParent();
        if (parentPath == null) throw new NullPointerException("The path must have a parent directory");
        if (!Files.exists(parentPath)) Files.createDirectories(parentPath);
        if (!Files.isDirectory(parentPath)) throw new IllegalStateException("The parent path must be a directory");

        this.path = path;
        this.changeListener = changeListener;
        this.errorHandler = errorHandler;
        this.watchService = path.getFileSystem().newWatchService();
        try {
            parentPath.register(this.watchService, WATCH_EVENTS);
        } catch (Throwable t) {
            this.watchService.close();
            throw t;
        }
        this.watchThread = ThreadRunner.startThread(this::run, "ConfigWatcher " + path.getFileName());
    }

    private void run() {
        try {
            WatchKey watchKey;
            while ((watchKey = this.watchService.take()) != null) {
                boolean found = false;
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;
                    if (this.path.getFileName().equals(event.context())) {
                        found = true;
                    }
                }
                if (!watchKey.reset()) break;
                if (!found) continue;

                boolean valid = true;
                while ((watchKey = this.watchService.poll(DEBOUNCE_DURATION.toMillis(), TimeUnit.MILLISECONDS)) != null) {
                    watchKey.pollEvents();
                    if (!watchKey.reset()) {
                        valid = false;
                        break;
                    }
                }

                try {
                    this.changeListener.onChange();
                } catch (InterruptedException e) {
                    throw e;
                } catch (Throwable t) {
                    this.errorHandler.onError(t);
                }

                if (!valid) break;
            }
        } catch (InterruptedException | ClosedWatchServiceException ignored) {
        } catch (Throwable t) {
            this.errorHandler.onError(t);
        }
    }

    @Override
    public void close() throws IOException {
        this.watchService.close();
        this.watchThread.interrupt();
    }

}
