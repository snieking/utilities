package com.github.snieking.retry;

import org.junit.Test;

import java.time.Duration;
import java.util.Optional;

import static com.github.snieking.retry.OneTimeRetryStrategy.createRetryStrategy;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class OneTimeRetryStrategyTester {

    @Test
    public void testOneTimeRetry() throws Exception {
        createRetryStrategy(Duration.ofSeconds(5))
                .perform(this::printHello);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInvalidDuration() throws Exception {
        createRetryStrategy(null)
            .perform(this::printHello);
    }

    @Test (expected = RuntimeException.class)
    public void testIgnorableExceptions() throws Exception {
        final OneTimeRetryStrategy retryer = createRetryStrategy()
                .nonRetryExceptions(IllegalStateException.class, IllegalArgumentException.class);

        retryer.perform(() -> { throw new IllegalStateException(); });
        retryer.perform(() -> { throw new IllegalArgumentException(); });
        retryer.perform(() -> { throw new RuntimeException(); });
    }

    @Test (expected = IllegalStateException.class)
    public void testPermanentException() {
        createRetryStrategy()
                .perform(() -> { throw new IllegalStateException(); });
    }

    @Test
    public void testPerformAndGet() throws Exception {
        Optional<String> msg = createRetryStrategy()
                .performAndGet(this::getHelloMessage);

        assertTrue(msg.isPresent());
        assertEquals(getHelloMessage(), msg.get());
    }

    public void printHello() throws RuntimeException {
        if (Math.random() < 0.50) {
            System.out.println("Failed!");
            throw new RuntimeException();
        } else {
            System.out.println("Success!");
        }
    }

    public String getHelloMessage() {
        return "Hello";
    }
}
