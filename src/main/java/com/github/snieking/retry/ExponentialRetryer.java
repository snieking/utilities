package com.github.snieking.retry;

import com.github.snieking.time.TimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class ExponentialRetryer implements Retryer {

    private static final Logger LOG = LoggerFactory.getLogger(ExponentialRetryer.class);

    private int maxExponent;
    private long base;
    private int exponent;
    private Map<Class<Exception>, Object> ignorableExceptions;

    private ExponentialRetryer(final int maxExponent, final long base) {
        this.maxExponent = maxExponent;
        this.exponent = 1;
        this.base = base;
        this.ignorableExceptions = new HashMap<>();
    }

    @Override
    public ExponentialRetryer ignoreExceptions(Class[] exceptions) {
        for (Class<Exception> exception : exceptions) {
            ignorableExceptions.put(exception, null);
        }
        return this;
    }

    @Override
    public void perform(final Runnable task) throws Exception {
        Exception exception = null;

        if (task != null) {
            while (exponent < maxExponent) {
                try {
                    task.run();
                    return;
                } catch (Exception e) {

                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }

                    if (!ignorableExceptions.containsKey(e.getClass())) {
                        TimeManager.waitUntilDurationPassed(Duration.ofMillis((long) Math.pow(base, exponent++)));
                    }
                }
            }
        }

        if (exception != null) {
            throw exception;
        }

    }

    @Override
    public Optional performAndGet(final Supplier task) throws Exception {
        Exception exception = null;
        boolean latestFailed = false;
        if (task != null) {
            while (exponent < maxExponent) {
                try {
                    return Optional.ofNullable(task.get());
                } catch (Exception e) {

                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }

                    if (!ignorableExceptions.containsKey(e.getClass())) {
                        TimeManager.waitUntilDurationPassed(Duration.ofMillis((long) Math.pow(base, exponent++)));
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
     * Creates a ExpontentialRetryer with a default max exponent of 5, and a default base of 10 milliseconds.
     */
    public static ExponentialRetryer create() {
        return new ExponentialRetryer(5, 10);
    }

    /**
     * Creates a ExponentialRetryer with a provided max exponent, and a default base of 10 milliseconds.
     */
    public static ExponentialRetryer create(int maxExponent) {
        return new ExponentialRetryer(maxExponent, 10);
    }

    /**
     * Creates an ExponentialRetryer with a default max exponent of 5, and a base of a provided milliseconds.
     */
    public static ExponentialRetryer create(long base) {
        return new ExponentialRetryer(5, base);
    }

}
