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
 * Performs one retry if the provided task returns an exception.
 *
 * @author Viktor Plane
 */
public final class OneTimeRetryStrategy implements RetryStrategy {

    private static final String FAILED_TASK = "Failed with the task, performing a retry.";

    private static final Logger LOG = LoggerFactory.getLogger(OneTimeRetryStrategy.class);
    private static final String BAD_DURATION = "Duration can't be null";
    private static Map<Class, Object> nonRetryableExceptions;

    private Duration durationBeforeNextRetry;

    private OneTimeRetryStrategy(final Duration duration) {
        this.durationBeforeNextRetry = duration;
        this.nonRetryableExceptions = new ConcurrentHashMap<>();
    }

    @Override
    public OneTimeRetryStrategy nonRetryExceptions(final Class... exceptions) {
        this.nonRetryableExceptions = new ConcurrentHashMap<>();
        for (Class e : exceptions) {
            nonRetryableExceptions.put(e, new Object());
        }
        return this;
    }

    @Override
    public void perform(final Runnable task) throws RuntimeException {
        Optional.ofNullable(task).ifPresent(runnable -> {
            try {
                runnable.run();
            } catch (RuntimeException e) {
                LOG.warn(FAILED_TASK);
                if (!nonRetryableExceptions.containsKey(e.getClass())) {
                    TimeManager.waitUntilDurationPassed(durationBeforeNextRetry);
                    runnable.run();
                }
            }
        });
    }

    @Override
    public <T> Optional<T> performAndGet(final Supplier<T> task) throws RuntimeException {
        if (Optional.ofNullable(task).isPresent()) {
            try {
                return Optional.ofNullable(task.get());
            } catch (RuntimeException e) {
                LOG.warn(FAILED_TASK);
                if (!nonRetryableExceptions.containsKey(e.getClass())) {
                    TimeManager.waitUntilDurationPassed(durationBeforeNextRetry);
                    return Optional.ofNullable(task.get());
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Creates a new OneTimeRetryStrategy instance with a default {@link java.time.Duration} of 0 milliseconds before retrying.
     *
     * @return {@link OneTimeRetryStrategy} instance.
     */
    public static OneTimeRetryStrategy createRetryStrategy() {
        return new OneTimeRetryStrategy(Duration.ZERO);
    }

    /**
     * Creates a new OneTimeRetryStrategy instance with a provided {@link java.time.Duration} to wait before the next retry.
     *
     * @param durationBeforeNextRetry the wait {@link Duration} to wait before retrying.
     * @return {@link OneTimeRetryStrategy} instance.
     */
    public static OneTimeRetryStrategy createRetryStrategy(final Duration durationBeforeNextRetry) {
        SanityChecker.verifyNoObjectIsNull(BAD_DURATION, durationBeforeNextRetry);
        return new OneTimeRetryStrategy(durationBeforeNextRetry);
    }

}
