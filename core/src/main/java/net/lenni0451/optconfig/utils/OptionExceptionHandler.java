package net.lenni0451.optconfig.utils;

import lombok.SneakyThrows;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
public interface OptionExceptionHandler {

    /**
     * Handle an exception thrown during option parsing or validation.<br>
     * The handler can choose to log the exception, ignore it, or rethrow it.
     *
     * @param option    The name of the option
     * @param exception The thrown exception
     * @throws Throwable A new/the same exception if processing should be canceled
     */
    void handle(final String option, final Throwable exception) throws Throwable;

    @SneakyThrows
    @ApiStatus.Internal
    default void tryHandle(final String option, final Throwable exception) {
        this.handle(option, exception);
    }

}
