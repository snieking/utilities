package com.github.snieking.retry;

import com.github.snieking.time.TimeManager;
import com.github.snieking.util.SanityChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Performs one retry if the provided task returns an exception.
 *
 * @author Viktor Plane
 */
public final class OneTimeRetry implements Retryer {

    private static final Logger LOG = LoggerFactory.getLogger(OneTimeRetry.class);
    private static final String BAD_DURATION = "Duration can't be null";
    private static Map<Class<Exception>, Object> ignorableExceptions;

    private Duration durationBeforeNextRetry;

    private OneTimeRetry(final Duration duration) {
        this.durationBeforeNextRetry = duration;
        ignorableExceptions = new HashMap<>();
    }

    @Override
    public OneTimeRetry nonRetryExceptions(final Class... exceptions) {
        ignorableExceptions = new HashMap<>();
        for (Class e : exceptions) {
            ignorableExceptions.put(e, null);
        }
        return this;
    }

    @Override
    public void perform(final Runnable task) throws RuntimeException {
        Optional.ofNullable(task).ifPresent(runnable -> {
            try {
                runnable.run();
            } catch (RuntimeException e) {
                if (!ignorableExceptions.containsKey(e.getClass())) {
                    TimeManager.waitUntilDurationPassed(durationBeforeNextRetry);
                    runnable.run();
                }
            }
        });
    }

    @Override
    public Optional performAndGet(final Supplier task) throws RuntimeException {
        if (Optional.ofNullable(task).isPresent()) {
            try {
                return Optional.ofNullable(task.get());
            } catch (RuntimeException e) {
                if (!ignorableExceptions.containsKey(e.getClass())) {
                    TimeManager.waitUntilDurationPassed(durationBeforeNextRetry);
                    return Optional.ofNullable(task.get());
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Creates a new OneTimeRetry instance with a default {@link java.time.Duration} of 0 milliseconds before retrying.
     */
    public static OneTimeRetry create() {
        return new OneTimeRetry(Duration.ZERO);
    }

    /**
     * Creates a new OneTimeRetry instance with a provided {@link java.time.Duration} to wait before the next retry.
     */
    public static OneTimeRetry create(final Duration durationBeforeNextRetry) {
        SanityChecker.verifyNoObjectIsNull(BAD_DURATION, durationBeforeNextRetry);
        return new OneTimeRetry(durationBeforeNextRetry);
    }

}
