package com.github.snieking.retry;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Interface for the different retry strategies.
 *
 * @author Viktor Plane
 */
public interface Retryer<T, R> {

    /**
     * Skip retry if one of the provided exceptions occur.
     */
    Object nonRetryExceptions(Class<Exception>... exceptions);

    /**
     * Performs (and retries) a runnable task. Does not return anything.
     */
    void perform(final Runnable task) throws RuntimeException;

    /**
     * Performs (and retries if failed) a supplied task and returns the result.
     */
    <T> Optional<T> performAndGet(final Supplier<T> task) throws RuntimeException;

}
