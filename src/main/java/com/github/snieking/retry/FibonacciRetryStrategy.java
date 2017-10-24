package com.github.snieking.retry;

import com.github.snieking.time.TimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class FibonacciRetryStrategy implements RetryStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(FibonacciRetryStrategy.class);

    private static final String FAILED_TASK = "Failed with task, performing retry attempt {}. Max attempt is {}.";

    private static final int DEFAULT_MAX_FIB = 10;
    private static final long DEFAULT_OFFSET = 100;

    private Map<Class, Object> nonRetryableExceptions;

    private int maxFib;
    private long offset;

    private FibonacciRetryStrategy(int maxFib, long offset) {
        this.maxFib = maxFib;
        this.offset = offset;
        this.nonRetryableExceptions = new ConcurrentHashMap<>();
    }

    @Override
    public FibonacciRetryStrategy nonRetryExceptions(Class... exceptions) {
        nonRetryableExceptions = new ConcurrentHashMap<>();
        for (Class exception : exceptions) {
            nonRetryableExceptions.put(exception, Optional.empty());
        }

        return this;
    }

    @Override
    public void perform(final Runnable runnable) {
        if (Optional.ofNullable(runnable).isPresent()) {
            RuntimeException exception = null;
            int currentFib = 1;

            long previousOffset = offset;
            long currentOffset = offset;

            while (currentFib <= maxFib) {
                try {
                    runnable.run();
                    return;
                } catch (RuntimeException e) {
                    LOG.warn(FAILED_TASK, currentFib, maxFib);

                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }

                    if (!nonRetryableExceptions.containsKey(e.getClass())) {
                        TimeManager.waitUntilDurationPassed(Duration.ofMillis(currentOffset));

                        long temp = currentOffset;
                        currentOffset += previousOffset;
                        previousOffset = temp;
                    } else {
                        break;
                    }

                    currentFib++;
                }
            }

            if (exception != null) {
                throw exception;
            }
        }
    }

    @Override
    public <T> Optional<T> performAndGet(final Supplier<T> task) {
        if (Optional.ofNullable(task).isPresent()) {
            RuntimeException exception = null;
            int currentFib = 1;

            long previousOffset = offset;
            long currentOffset = offset;

            while (currentFib <= maxFib) {
                try {
                    return Optional.ofNullable(task.get());
                } catch (RuntimeException e) {
                    LOG.warn(FAILED_TASK, currentFib, maxFib);
                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }

                    if (!nonRetryableExceptions.containsKey(e.getClass())) {
                        TimeManager.waitUntilDurationPassed(Duration.ofMillis(currentOffset));

                        long temp = currentOffset;
                        currentOffset += previousOffset;
                        previousOffset = temp;
                    } else {
                        break;
                    }

                    currentFib++;
                }
            }

            if (exception != null) {
                throw exception;
            }
        }

        return Optional.empty();
    }

    /**
     * Creates a default {@link FibonacciRetryStrategy} with a maxFib of 10 and an offset of 100 milliseconds.
     *
     * @return {@link FibonacciRetryStrategy} instance.
     */
    public static FibonacciRetryStrategy createRetryStrategy() {
        return new FibonacciRetryStrategy(DEFAULT_MAX_FIB, DEFAULT_OFFSET);
    }

    /**
     * Creates a default {@link FibonacciRetryStrategy} with a provided maxFib and a default offset of 100 milliseconds.
     *
     * @param maxFib the maxFib that it will iterate to. Can also be looked at as maxRetries.
     * @return {@link FibonacciRetryStrategy} instance.
     */
    public static FibonacciRetryStrategy createRetryStrategy(int maxFib) {
        return new FibonacciRetryStrategy(maxFib, DEFAULT_OFFSET);
    }

    /**
     * Creates a {@link FibonacciRetryStrategy} with a default maxFib of 10 and a provided milliseconds offset.
     *
     * @param offset decides what the start of fib will be. For example 100: 100 + 100 + 200 + 400.
     * @return {@link FibonacciRetryStrategy} instance.
     */
    public static FibonacciRetryStrategy createRetryStrategy(long offset) {
        return new FibonacciRetryStrategy(DEFAULT_MAX_FIB, offset);
    }

    /**
     * Creates a {@link FibonacciRetryStrategy} with a provided maxFib and milliseconds offset.
     *
     * @param maxFib the maxFib that it will iterate to. Can also be looked at as maxRetries.
     * @param offset decides what the start of fib will be. For example 100: 100 + 100 + 200 + 400.
     * @return {@link FibonacciRetryStrategy} instance.
     */
    public static FibonacciRetryStrategy createRetryStrategy(int maxFib, long offset) {
        return new FibonacciRetryStrategy(maxFib, offset);
    }
}
