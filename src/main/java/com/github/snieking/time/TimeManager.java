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

package com.github.snieking.time;

import com.github.snieking.util.SanityChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     *
     * @param timestamp the timestamp to check on if the duration has passed.
     * @param duration  the duration to check if it has passed from the provided timestamp.
     * @return boolean if the duration has passed or not.
     */
    public static boolean isDurationPassed(final Instant timestamp, final Duration duration) {
        SanityChecker.verifyNoObjectIsNull(BAD_TIME, timestamp, duration);
        return timestamp.plus(duration).isAfter(Instant.now());
    }

    /**
     * Waits for the provided duration to pass.
     *
     * @param duration the time the thread should wait for to pass.
     */
    public static void waitUntilDurationPassed(final Duration duration) {
        SanityChecker.verifyNoObjectIsNull(BAD_TIME, duration);
        sleep(duration.toMillis());
    }

    /**
     * Sleeps for the provided milliseconds.
     *
     * @param millis the milliseconds the thread should sleep for.
     */
    public static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.warn("Received an interrupt while sleeping.");
        }
    }

}
