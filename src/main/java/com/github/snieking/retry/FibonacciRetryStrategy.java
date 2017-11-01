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
import com.github.snieking.util.SanityChecker;
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
    private static final String BAD_ARGUMENTS = "Provided arguments can't be null";

    private static final int DEFAULT_MAX_FIB = 10;
    private static final double DEFAULT_OFFSET = 100;

    private Map<Class, Object> nonRetryableExceptions;

    private int maxFib;
    private double offset;

    private FibonacciRetryStrategy(int maxFib, double offset) {
        this.maxFib = maxFib;
        this.offset = offset;
        this.nonRetryableExceptions = new ConcurrentHashMap<>();
    }

    @Override
    public FibonacciRetryStrategy nonRetryExceptions(Class... exceptions) {
        nonRetryableExceptions = new ConcurrentHashMap<>();
        for (Class exception : exceptions) {
            nonRetryableExceptions.put(exception, new Object());
        }

        return this;
    }

    @Override
    public void perform(final Runnable runnable) {
        if (Optional.ofNullable(runnable).isPresent()) {
            RuntimeException exception = null;
            int currentFib = 1;

            double previousOffset = offset;
            double currentOffset = offset;

            while (currentFib <= maxFib) {
                try {
                    LOG.trace("Attempt {} of running task", currentFib);
                    runnable.run();
                    return;
                } catch (RuntimeException e) {

                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }

                    if (!nonRetryableExceptions.containsKey(e.getClass())) {
                        LOG.warn(FAILED_TASK, currentFib++, maxFib);
                        TimeManager.waitUntilDurationPassed(Duration.ofMillis((int) currentOffset));

                        double temp = currentOffset;
                        currentOffset += previousOffset;
                        previousOffset = temp;
                    } else {
                        break;
                    }

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

            double previousOffset = offset;
            double currentOffset = offset;

            while (currentFib <= maxFib) {
                try {
                    LOG.trace("Attempt {} of running task", currentFib);
                    return Optional.ofNullable(task.get());
                } catch (RuntimeException e) {

                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }

                    if (!nonRetryableExceptions.containsKey(e.getClass())) {
                        LOG.warn(FAILED_TASK, currentFib++, maxFib);
                        TimeManager.waitUntilDurationPassed(Duration.ofMillis((int) currentOffset));

                        double temp = currentOffset;
                        currentOffset += previousOffset;
                        previousOffset = temp;
                    } else {
                        break;
                    }
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
    public static FibonacciRetryStrategy createRetryStrategy(final Integer maxFib) {
        SanityChecker.verifyNoObjectIsNull(BAD_ARGUMENTS, maxFib);
        return new FibonacciRetryStrategy(maxFib, DEFAULT_OFFSET);
    }

    /**
     * Creates a {@link FibonacciRetryStrategy} with a default maxFib of 10 and a provided milliseconds offset.
     *
     * @param offset decides what the start of fib will be. For example 100: 100 + 100 + 200 + 400.
     * @return {@link FibonacciRetryStrategy} instance.
     */
    public static FibonacciRetryStrategy createRetryStrategy(final double offset) {
        if (offset <= 0) {
            throw new IllegalArgumentException(BAD_ARGUMENTS);
        }

        return new FibonacciRetryStrategy(DEFAULT_MAX_FIB, offset);
    }

    /**
     * Creates a {@link FibonacciRetryStrategy} with a provided maxFib and milliseconds offset.
     *
     * @param maxFib the maxFib that it will iterate to. Can also be looked at as maxRetries.
     * @param offset decides what the start of fib will be. For example 100: 100 + 100 + 200 + 400.
     * @return {@link FibonacciRetryStrategy} instance.
     */
    public static FibonacciRetryStrategy createRetryStrategy(final int maxFib, final double offset) {
        if (maxFib <= 0 && offset <= 0) {
            throw new IllegalArgumentException(BAD_ARGUMENTS);
        }

        return new FibonacciRetryStrategy(maxFib, offset);
    }
}
