package com.github.snieking.time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.snieking.util.SanityChecker;

import java.time.Duration;
import java.time.Instant;

/**
 * Utility class for managing time. Using the Java 8 time API.
 *
 * @author Viktor Plane
 */
public class TimeManager {

    private static final Logger LOG = LoggerFactory.getLogger(TimeManager.class);
    private static final String BAD_TIME = "Times can't be null";

    /**
     * Checks if the duration has passed since the provided duration.
     */
    public static boolean isDurationPassed(final Instant timestamp, final Duration duration) {
        SanityChecker.verifyNoObjectIsNull(BAD_TIME, timestamp, duration);
        return timestamp.plus(duration).isAfter(Instant.now());
    }

    /**
     * Waits for the provided duration to pass.
     */
    public static void waitUntilDurationPassed(final Duration duration) {
        SanityChecker.verifyNoObjectIsNull(BAD_TIME, duration);
        sleep(duration.toMillis());
    }

    /**
     * Sleeps for the provided milliseconds.
     */
    public static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.warn("Received an interrupt while sleeping.");
        }
    }

}
