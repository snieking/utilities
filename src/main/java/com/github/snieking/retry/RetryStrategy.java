package com.github.snieking.retry;

import java.util.Optional;
import java.util.function.Supplier;

public interface RetryStrategy {

    /**
     * Skip retry if one of the provided exceptions occur.
     */
    Object nonRetryExceptions(final Class... exceptions);

    /**
     * Performs (and retries) a runnable task. Does not return anything.
     */
    void perform(final Runnable runnable);

    /**
     * Performs (and retries if failed) a supplied task and returns the result.
     */
    <T> Optional<T> performAndGet(final Supplier<T> task);
}
