package com.github.snieking.retry;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
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
     * Performs (and retries) a runnable task asynchronously. Does not return anything.
     *
     * @param runnable the {@link Runnable} that should be tried (and retried).
     */
    default void performAsync(final Runnable runnable) {
        new Thread(() -> perform(runnable)).start();
    }

    /**
     * Performs (and retries if failed) a supplied task and returns the result.
     *
     * @param task the {@link Supplier} that should be tried (and retried).
     * @param <T> the return type.
     * @return {@link Optional} of the result from the provided {@link Supplier}.
     */
    <T> Optional<T> performAndGet(final Supplier<T> task);

    /**
     * Performs (and retries if failed) a supplied task asynchronously and returns the result.
     *
     * @param task the {@link Supplier} that should be tried (and retried).
     * @param <T> the return type.
     * @return CompletableFuture holding a {@link Optional} of the result from the provided {@link Supplier}.
     */
    default <T> CompletableFuture<Optional<T>> performAndGetAsync(final Supplier<T> task) {
        return CompletableFuture.supplyAsync(() -> performAndGet(task));
    }
}
