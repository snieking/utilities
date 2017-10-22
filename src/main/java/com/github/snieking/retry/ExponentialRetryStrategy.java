package com.github.snieking.retry;

import com.github.snieking.time.TimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Performs an exponential retry strategy.
 *
 * For example, if base is 10ms and maxExponent is 5, it will perform retries after 10, 100, 1000 & 100000 milliseconds.
 */
public final class ExponentialRetryStrategy implements RetryStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(ExponentialRetryStrategy.class);

    private int maxExponent;
    private long base;
    private int exponent;
    private Map<Class, Object> ignorableExceptions;

    private ExponentialRetryStrategy(final int maxExponent, final long base) {
        this.maxExponent = maxExponent;
        this.exponent = 0;
        this.base = base;
        this.ignorableExceptions = new HashMap<>();
    }

    @Override
    public ExponentialRetryStrategy nonRetryExceptions(Class... exceptions) {
        ignorableExceptions = new HashMap<>();
        for (Class exception : exceptions) {
            ignorableExceptions.put(exception, null);
        }
        return this;
    }

    @Override
    public void perform(final Runnable task) throws RuntimeException {
        RuntimeException exception = null;

        if (task != null) {
            while (exponent <= maxExponent) {
                try {
                    task.run();
                    return;
                } catch (RuntimeException e) {

                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }
                    exponent++;
                    if (!ignorableExceptions.containsKey(e.getClass())) {
                        TimeManager.waitUntilDurationPassed(Duration.ofMillis((long) Math.pow(base, exponent++)));
                    } else {
                        break;
                    }
                }
            }
        }

        if (exception != null) {
            throw exception;
        }

    }

    @Override
    public <T> Optional<T> performAndGet(final Supplier<T> task) throws RuntimeException {
        RuntimeException exception = null;
        boolean latestFailed = false;
        if (task != null) {
            while (exponent <= maxExponent) {
                try {
                    return Optional.ofNullable(task.get());
                } catch (RuntimeException e) {

                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }
                    exponent++;
                    if (!ignorableExceptions.containsKey(e.getClass())) {
                        TimeManager.waitUntilDurationPassed(Duration.ofMillis((long) Math.pow(base, exponent++)));
                    } else {
                        break;
                    }
                }
            }
        }

        if (latestFailed && exception != null) {
            throw exception;
        } else {
            return Optional.empty();
        }
    }

    /**
     * Creates a ExpontentialRetryer with a default max exponent of 4, and a default base of 10 milliseconds.
     */
    public static ExponentialRetryStrategy createRetryStrategy() {
        return new ExponentialRetryStrategy(4, 10);
    }

    /**
     * Creates a ExponentialRetryStrategy with a provided max exponent, and a default base of 10 milliseconds.
     */
    public static ExponentialRetryStrategy createRetryStrategy(final int maxExponent) {
        return new ExponentialRetryStrategy(maxExponent, 10);
    }

    /**
     * Creates an ExponentialRetryStrategy with a default max exponent of 4, and a base of a provided milliseconds.
     */
    public static ExponentialRetryStrategy createRetryStrategy(final long base) {
        return new ExponentialRetryStrategy(4, base);
    }

    /**
     * Creates an ExponentialRetryStrategy with a provided max exponent, and a base of a provided milliseconds.
     */
    public static ExponentialRetryStrategy createRetryStrategy(final int maxExponent, long base) {
        return new ExponentialRetryStrategy(maxExponent, base);
    }

}
