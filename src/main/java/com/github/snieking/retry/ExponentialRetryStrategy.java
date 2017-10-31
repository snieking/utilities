/*
 * Copyright 2017 Viktor Plane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.snieking.retry;

import com.github.snieking.time.TimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Performs an exponential retry strategy.
 * 
 * For example, if base is 10ms and maxExponent is 5, it will perform retries after 10, 100, 1000 and 100000 milliseconds.
 */
public final class ExponentialRetryStrategy implements RetryStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(ExponentialRetryStrategy.class);

    private static final String FAILED_TASK = "Failed with task, performing retry attempt {}. Max attempt is {}.";

    private static final long DEFAULT_BASE = 10;
    private static final int DEFAULT_MAX_EXPONENT = 4;

    private int maxExponent;
    private long base;
    private Map<Class, Object> nonRetryableExceptions;

    private ExponentialRetryStrategy(final int maxExponent, final long base) {
        this.maxExponent = maxExponent;
        this.base = base;
        this.nonRetryableExceptions = new ConcurrentHashMap<>();
    }

    @Override
    public ExponentialRetryStrategy nonRetryExceptions(Class... exceptions) {
        this.nonRetryableExceptions = new ConcurrentHashMap<>();
        for (Class exception : exceptions) {
            nonRetryableExceptions.put(exception, Optional.empty());
        }
        return this;
    }

    @Override
    public void perform(final Runnable task) throws RuntimeException {
        RuntimeException exception = null;
        if (task != null) {
            int exponent = 0;

            while (exponent <= maxExponent) {
                try {
                    task.run();
                    return;
                } catch (RuntimeException e) {
                    LOG.warn(FAILED_TASK, exponent + 1, maxExponent);

                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }
                    exponent++;
                    if (!nonRetryableExceptions.containsKey(e.getClass())) {
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
            int exponent = 0;

            while (exponent <= maxExponent) {
                try {
                    return Optional.ofNullable(task.get());
                } catch (RuntimeException e) {
                    LOG.warn(FAILED_TASK, exponent + 1, maxExponent);

                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }
                    exponent++;
                    if (!nonRetryableExceptions.containsKey(e.getClass())) {
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
     *
     * @return {@link ExponentialRetryStrategy}
     */
    public static ExponentialRetryStrategy createRetryStrategy() {
        return new ExponentialRetryStrategy(DEFAULT_MAX_EXPONENT, DEFAULT_BASE);
    }

    /**
     * Creates a ExponentialRetryStrategy with a provided max exponent, and a default base of 10 milliseconds.
     *
     * @param maxExponent the max exponent before giving up
     * @return {@link ExponentialRetryStrategy}
     */
    public static ExponentialRetryStrategy createRetryStrategy(final int maxExponent) {
        return new ExponentialRetryStrategy(maxExponent, DEFAULT_BASE);
    }

    /**
     * Creates an ExponentialRetryStrategy with a default max exponent of 4, and a base of a provided milliseconds.
     *
     * @param base the base that should be used.
     * @return {@link ExponentialRetryStrategy}
     */
    public static ExponentialRetryStrategy createRetryStrategy(final long base) {
        return new ExponentialRetryStrategy(DEFAULT_MAX_EXPONENT, base);
    }

    /**
     * Creates an ExponentialRetryStrategy with a provided max exponent, and a base of a provided milliseconds.
     *
     * @param maxExponent the max exponent before giving up
     * @param base        the base that should be used.
     * @return {@link ExponentialRetryStrategy}
     */
    public static ExponentialRetryStrategy createRetryStrategy(final int maxExponent, long base) {
        return new ExponentialRetryStrategy(maxExponent, base);
    }

}
