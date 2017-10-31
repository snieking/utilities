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
import com.github.snieking.util.Stopwatch;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.github.snieking.retry.ExponentialRetryStrategy.createRetryStrategy;

public class ExponentialRetryStrategyTest extends BaseRetryStrategyTest {

    @Test
    public void testDefaultExponentialRetryer() {
        long base = 10;
        int maxExponent = 3;

        final Stopwatch timer = Stopwatch.start();
        try {
            createRetryStrategy(maxExponent, base)
                    .perform(() -> {
                        throw new IllegalStateException();
                    });
        } catch (Exception e) {
            final long time = timer.stop().getTimeInSeconds();
            Assert.assertTrue(time >= getSecondsFromBaseAndExponent(base, maxExponent));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testNonRetryableExceptions() {
        final long base = 10;
        final int maxExponent = 3;

        final Stopwatch timer = Stopwatch.start();
        try {
            createRetryStrategy(maxExponent, base)
                    .nonRetryExceptions(IllegalStateException.class)
                    .perform(() -> {
                        throw new IllegalStateException();
                    });
        } catch (IllegalStateException e) {
            Assert.assertTrue(timer.stop().getTimeInSeconds() < getSecondsFromBaseAndExponent(base, maxExponent));
            throw e;
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testPermanentException() {
        createRetryStrategy(3, 10)
                .perform(() -> {
                    throw new IllegalStateException();
                });
    }

    @Test
    public void testSuccessfulPerformAndGetAsync() {
        final long sleepTime = 500;
        final String msg = "Delayed Message";

        final RetryStrategy retryStrategy = createRetryStrategy();

        final CompletableFuture<Optional<String>> future = retryStrategy.performAndGetAsync(
                () -> getMessageAfterMillis(msg, sleepTime))
                .whenComplete((optString, throwable) -> {
                    if (throwable != null) {
                        optString.ifPresent(message -> Assert.assertEquals(msg, message));
                    }
                });

        Assert.assertFalse(future.isDone());
        TimeManager.waitUntilDurationPassed(Duration.ofMillis(sleepTime + 50));
        Assert.assertTrue(future.isDone());
    }

    @Test
    public void testTemporaryErrorDuringPerformAndGetAsync() {
        final RetryStrategy retryStrategy = createRetryStrategy(10, 2);

        final CompletableFuture<Optional<String>> future = retryStrategy
                .performAndGetAsync(() -> failForCertainAttemptsThenReturnHello(5))
                .whenCompleteAsync((msg, throwable) -> {
                    Assert.assertNull(throwable);
                    Assert.assertNotNull(msg);
                });

        TimeManager.waitUntilDurationPassed(Duration.ofMillis(10));
        Assert.assertFalse(future.isDone());
        TimeManager.waitUntilDurationPassed(Duration.ofMillis(1000));
        Assert.assertTrue(future.isDone());
    }

    @Test
    public void testPermanentErrorDuringPerformAndGetAsync() {
        final RuntimeException exception = new RuntimeException();

        final RetryStrategy retryStrategy = createRetryStrategy(10, 2);

        final CompletableFuture<Optional<String>> future = retryStrategy
                .performAndGetAsync(() -> getMessageThatFailsWithException(exception))
                .whenComplete((optMsg, throwable) -> {
                    Assert.assertTrue(throwable != null);
                    Assert.assertEquals(exception.getMessage(), throwable.getMessage());
                });

        Assert.assertFalse(future.isDone());
        TimeManager.waitUntilDurationPassed(Duration.ofMillis(1500));
        Assert.assertTrue(future.isDone());
    }

    private int getSecondsFromBaseAndExponent(final long base, final int maxExponent) {
        return (int) Math.pow(base, maxExponent) / 1000;
    }

    private String getMessageAfterMillis(final String msg, final long millis) {
        TimeManager.sleep(500);
        return msg;
    }

    private String getMessageThatFailsWithException(final RuntimeException e) {
        throw e;
    }

    private String failForCertainAttemptsThenReturnHello(int numberOfTimesToFail) {
        while (numOfFails++ < numberOfTimesToFail) {
            throw new RuntimeException();
        }

        return "hello";
    }
}
