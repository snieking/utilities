package com.github.snieking.retry;

import org.junit.Test;
import com.github.snieking.util.Stopwatch;

import static org.junit.Assert.assertTrue;

public class ExponentialRetryerTester extends RetryTester {

    @Test
    public void testDefaultExponentialRetryer() {
        final Stopwatch timer = Stopwatch.start();
        try {
            ExponentialRetryer.create()
                    .perform(() -> {
                        throw new IllegalStateException();
                    });
        } catch (Exception e) {
            final long time = timer.stop().getTimeInSeconds();
            assertTrue(time > 10);
            e.printStackTrace();
        }
    }
}
