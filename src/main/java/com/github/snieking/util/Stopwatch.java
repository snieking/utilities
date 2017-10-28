package com.github.snieking.util;

import java.time.Instant;

/**
 * Utility class for making it easier to track how long stuff takes.
 *
 * @author Viktor Plane
 */
public class Stopwatch {
    private final Instant start;
    private Instant stop;

    private Stopwatch(final Instant start) {
        this.start = start;
    }

    /**
     * Stops the stopwatch.
     *
     * @return the {@link Stopwatch} instance.
     */
    public Stopwatch stop() {
        stop = Instant.now();
        return this;
    }

    /**
     * Gets the time that passed during the start and stop of the stopwatch.
     *
     * @return time in seconds.
     */
    public long getTimeInSeconds() {
        return stop.getEpochSecond() - start.getEpochSecond();
    }

    /**
     * Gets the time that passed during the start and stop of the stopwatch.
     *
     * @return time in milliseconds.
     */
    public long getTimeInMilliSeconds() {
        return stop.toEpochMilli() - start.toEpochMilli();
    }

    /**
     * Starts a new stopwatch.
     *
     * @return the {@link Stopwatch} instance.
     */
    public static Stopwatch start() {
        return new Stopwatch(Instant.now());
    }
}
