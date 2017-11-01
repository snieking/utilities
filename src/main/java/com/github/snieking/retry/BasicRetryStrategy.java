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

/**
 * Applies a basic retry strategy. Meaning that it will keep retrying for until a max attempts has been reached,
 * and wait a set duration between each attempt.
 */
public final class BasicRetryStrategy implements RetryStrategy {

    private static final String FAILED_TASK = "Failed with task, performing retry attempt {}. Max attempt is {}.";
    private static final String BAD_ARGUMENTS = "Provided arguments can't be null";

    private static final Logger LOG = LoggerFactory.getLogger(BasicRetryStrategy.class);

    private static final Duration DEFAULT_DURATION = Duration.ofSeconds(5);
    private static final int DEFAULT_MAX_ATTEMPTS = 10;

    private static Map<Class, Object> nonRetryableExceptions;

    private final Duration durationBeforeNextRetry;
    private final int maxAttempts;

    /**
     * Creates a BasicRetryStrategy.
     *
     * @param duration    the wait time between retries.
     * @param maxAttempts the max retry attempts that will be performed.
     */
    private BasicRetryStrategy(final Duration duration, final int maxAttempts) {
        this.durationBeforeNextRetry = duration;
        this.maxAttempts = maxAttempts;
        this.nonRetryableExceptions = new ConcurrentHashMap<>();
    }

    @Override
    public BasicRetryStrategy nonRetryExceptions(Class... exceptions) {
        this.nonRetryableExceptions = new ConcurrentHashMap<>();
        for (Class e : exceptions) {
            nonRetryableExceptions.put(e, new Object());
        }

        return this;
    }

    @Override
    public void perform(Runnable task) {
        RuntimeException exception = null;
        if (task != null) {
            int currentAttempt = 0;
            while (currentAttempt < maxAttempts) {

                try {
                    LOG.trace("Attempt {} of running task", currentAttempt+1);
                    task.run();
                    break;
                } catch (RuntimeException e) {

                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }

                    if (!nonRetryableExceptions.containsKey(e.getClass())) {
                        LOG.warn(FAILED_TASK, currentAttempt++, maxAttempts);
                        TimeManager.waitUntilDurationPassed(durationBeforeNextRetry);
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
    public <T> Optional<T> performAndGet(Supplier<T> task) {
        RuntimeException exception = null;
        boolean latestFailed = false;
        if (task != null) {
            int currentAttempt = 0;

            while (currentAttempt < maxAttempts) {
                try {
                    LOG.trace("Attempt {} of running task", currentAttempt+1);
                    return Optional.ofNullable(task.get());
                } catch (RuntimeException e) {

                    if (exception != null) {
                        exception.addSuppressed(e);
                    } else {
                        exception = e;
                    }

                    if (!nonRetryableExceptions.containsKey(e.getClass())) {
                        LOG.warn(FAILED_TASK, currentAttempt++, maxAttempts);
                        TimeManager.waitUntilDurationPassed(durationBeforeNextRetry);
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
     * Creates a BasicRetryStrategy with a default wait time between retries of 5 seconds,
     * and a default max retries attempts of 10.
     *
     * @return {@link BasicRetryStrategy}
     */
    public static BasicRetryStrategy createRetryStrategy() {
        return new BasicRetryStrategy(DEFAULT_DURATION, DEFAULT_MAX_ATTEMPTS);
    }

    /**
     * Creates a BasicRetryStrategy with a provided duration to wait between retries,
     * and a default max retries attempts of 10.
     *
     * @param duration the wait time between retries.
     * @return {@link BasicRetryStrategy}
     */
    public static BasicRetryStrategy createRetryStrategy(final Duration duration) {
        SanityChecker.verifyNoObjectIsNull(BAD_ARGUMENTS, duration);
        return new BasicRetryStrategy(duration, DEFAULT_MAX_ATTEMPTS);
    }

    /**
     * Creates a BasicRetryStrategy with a provided duration to wait between retries,
     * and a default max retries attempts of 10.
     *
     * @param maxAttempts the max retry attempts that will be performed.
     * @return {@link BasicRetryStrategy}
     */
    public static BasicRetryStrategy createRetryStrategy(final int maxAttempts) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException(BAD_ARGUMENTS);
        }

        return new BasicRetryStrategy(DEFAULT_DURATION, maxAttempts);
    }

    /**
     * Creates a BasicRetryStrategy with a provided duration to wait between retries,
     * and a provided max retries attempts.
     *
     * @param duration    the wait time between retries.
     * @param maxAttempts the max retry attempts that will be performed.
     * @return {@link BasicRetryStrategy}
     */
    public static BasicRetryStrategy createRetryStrategy(final Duration duration, final int maxAttempts) {
        SanityChecker.verifyNoObjectIsNull(BAD_ARGUMENTS, duration);
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException(BAD_ARGUMENTS);
        }

        return new BasicRetryStrategy(duration, maxAttempts);
    }
}
