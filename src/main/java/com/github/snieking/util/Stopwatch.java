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
