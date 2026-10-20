package net.lenni0451.optconfig.watch;

/**
 * A watcher that watches a config for changes and calls a {@link ChangeListener} when the config changes.<br>
 * The watcher can be closed to stop watching the config for changes.
 */
public interface ConfigWatcher extends AutoCloseable {

    @FunctionalInterface
    interface ChangeListener {
        void onChange() throws Throwable;
    }

    @FunctionalInterface
    interface ErrorHandler {
        void onError(final Throwable t);
    }

}
