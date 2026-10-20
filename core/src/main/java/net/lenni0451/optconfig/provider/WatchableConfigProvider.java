package net.lenni0451.optconfig.provider;

import net.lenni0451.optconfig.watch.ConfigWatcher;

/**
 * A {@link ConfigProvider} that can be watched for changes.
 */
public interface WatchableConfigProvider extends ConfigProvider {

    /**
     * Watch the config for changes and call the given {@link ConfigWatcher.ChangeListener} when the config changes.
     *
     * @param changeListener The listener to call when the config changes
     * @return The created {@link ConfigWatcher}. Can be closed to stop watching the config for changes
     */
    default ConfigWatcher watch(final ConfigWatcher.ChangeListener changeListener) {
        return this.watch(changeListener, Throwable::printStackTrace);
    }

    /**
     * Watch the config for changes and call the given {@link ConfigWatcher.ChangeListener} when the config changes.<br>
     * If an error occurs while watching the config, the given {@link ConfigWatcher.ErrorHandler} is called.
     *
     * @param changeListener The listener to call when the config changes
     * @param errorHandler   The error handler to call when an error occurs
     * @return The created {@link ConfigWatcher}. Can be closed to stop watching the config for changes
     */
    ConfigWatcher watch(final ConfigWatcher.ChangeListener changeListener, final ConfigWatcher.ErrorHandler errorHandler);

}
