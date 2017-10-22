package com.github.snieking.util;

import java.time.Instant;

public class Stopwatch {
    private final Instant start;
    private Instant stop;

    private Stopwatch(final Instant start) {
        this.start = start;
    }

    public Stopwatch stop() {
        stop = Instant.now();
        return this;
    }

    public long getTimeInSeconds() {
        return stop.getEpochSecond() - start.getEpochSecond();
    }

    public static Stopwatch start() {
        return new Stopwatch(Instant.now());
    }
}
