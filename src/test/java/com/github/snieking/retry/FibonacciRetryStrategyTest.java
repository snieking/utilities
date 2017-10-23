package com.github.snieking.retry;

import com.github.snieking.time.TimeManager;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FibonacciRetryStrategyTest extends BaseRetryStrategyTest {

    @Test (expected = IllegalStateException.class)
    public void testFibonacciRetryStrategy() {
        FibonacciRetryStrategy
                .createRetryStrategy(4, 100)
                .perform(() -> { throw new IllegalStateException(); });
    }

    @Test
    public void testNonRetryableExceptions() {
        CompletableFuture<Optional<String>> future = FibonacciRetryStrategy
                .createRetryStrategy(4, 100)
                .nonRetryExceptions(IllegalStateException.class)
                .performAndGetAsync(() -> getMessageButThrowsException())
                .whenComplete((msg, throwable) -> {
                    Assert.assertNull(msg);
                    Assert.assertNotNull(throwable);
                });

        TimeManager.waitUntilDurationPassed(Duration.ofMillis(100));
        Assert.assertTrue(future.isDone());
    }

    @Test
    public void testRetryableExceptions() {
        CompletableFuture<Optional<String>> future = FibonacciRetryStrategy
                .createRetryStrategy(4, 100)
                .performAndGetAsync(() -> getMessageButThrowsException())
                .whenComplete((msg, throwable) -> {
                    Assert.assertNotNull(throwable);
                    Assert.assertNull(msg);
                });

        TimeManager.waitUntilDurationPassed(Duration.ofMillis(400));
        Assert.assertFalse(future.isDone());
        TimeManager.waitUntilDurationPassed(Duration.ofMillis(1250));
        Assert.assertTrue(future.isDone());
    }
}
