package com.github.snieking.retry;

import java.util.Optional;
import java.util.function.Supplier;

public interface RetryStrategy {

    /**
     * Skip retry if one of the provided exceptions occur.
     *
     * @param exceptions list of exceptions to not perform retry if one of them occurs.
     * @return the retry strategy instance.
     */
    Object nonRetryExceptions(final Class... exceptions);

    /**
     * Performs (and retries) a runnable task. Does not return anything.
     *
     * @param runnable the {@link Runnable} that should be tried (and retried).
     */
    void perform(final Runnable runnable);

    /**
     * Performs (and retries if failed) a supplied task and returns the result.
     *
     * @param task the {@link Supplier} that should be tried (and retried).
     * @return {@link Optional} of the result from the provided {@link Supplier}.
     */
    <T> Optional<T> performAndGet(final Supplier<T> task);
}
