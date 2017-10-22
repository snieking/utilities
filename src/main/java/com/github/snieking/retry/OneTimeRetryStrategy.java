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
public final class OneTimeRetryStrategy implements RetryStrategy {

    private static final String FAILED_TASK = "Failed with the task, performing a retry.";

    private static final Logger LOG = LoggerFactory.getLogger(OneTimeRetryStrategy.class);
    private static final String BAD_DURATION = "Duration can't be null";
    private static Map<Class, Object> ignorableExceptions;

    private Duration durationBeforeNextRetry;

    private OneTimeRetryStrategy(final Duration duration) {
        this.durationBeforeNextRetry = duration;
        ignorableExceptions = new HashMap<>();
    }

    @Override
    public OneTimeRetryStrategy nonRetryExceptions(final Class... exceptions) {
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
                LOG.info(FAILED_TASK);
                if (!ignorableExceptions.containsKey(e.getClass())) {
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
                LOG.info(FAILED_TASK);
                if (!ignorableExceptions.containsKey(e.getClass())) {
                    TimeManager.waitUntilDurationPassed(durationBeforeNextRetry);
                    return Optional.ofNullable(task.get());
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Creates a new OneTimeRetryStrategy instance with a default {@link java.time.Duration} of 0 milliseconds before retrying.
     */
    public static OneTimeRetryStrategy createRetryStrategy() {
        return new OneTimeRetryStrategy(Duration.ZERO);
    }

    /**
     * Creates a new OneTimeRetryStrategy instance with a provided {@link java.time.Duration} to wait before the next retry.
     *
     * @param durationBeforeNextRetry the wait {@link Duration} to wait before retrying.
     * @return {@link OneTimeRetryStrategy}.
     */
    public static OneTimeRetryStrategy createRetryStrategy(final Duration durationBeforeNextRetry) {
        SanityChecker.verifyNoObjectIsNull(BAD_DURATION, durationBeforeNextRetry);
        return new OneTimeRetryStrategy(durationBeforeNextRetry);
    }

}
