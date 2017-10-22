package com.github.snieking.retry;

import static com.github.snieking.retry.ExponentialRetryStrategy.createRetryStrategy;

import org.junit.Test;
import com.github.snieking.util.Stopwatch;

import static org.junit.Assert.assertTrue;

public class ExponentialRetryStrategyTest {

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
            assertTrue(time >= getSecondsFromBaseAndExponent(base, maxExponent));
        }
    }

    @Test (expected = IllegalStateException.class)
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
            assertTrue(timer.stop().getTimeInSeconds() < getSecondsFromBaseAndExponent(base, maxExponent));
            throw e;
        }
    }

    @Test (expected = IllegalStateException.class)
    public void testPermanentException() {
        createRetryStrategy(3, 10)
                .perform(() -> { throw new IllegalStateException(); });
    }

    private int getSecondsFromBaseAndExponent(final long base, final int maxExponent) {
        int seconds = (int) Math.pow(base, maxExponent) / 1000;
        return seconds;
    }
}
