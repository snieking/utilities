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

package com.github.snieking.retry;

import com.github.snieking.util.Stopwatch;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

import static com.github.snieking.retry.BasicRetryStrategy.createRetryStrategy;

public class BasicRetryStrategyTest extends BaseRetryStrategyTest {

    @Test
    public void testBasicRetryStrategy() {
        final Stopwatch timer = Stopwatch.start();
        try {
            createRetryStrategy(Duration.ofSeconds(1), 5)
                    .perform(() -> {
                        throw new IllegalStateException();
                    });
        } catch (Exception e) {
            final long time = timer.stop().getTimeInSeconds();
            Assert.assertTrue(time >= 5);
        }
    }

    @Test
    public void testBasicRetryGet() {
        final Stopwatch timer = Stopwatch.start();
        try {
            createRetryStrategy(Duration.ofSeconds(1), 5)
                    .performAndGet(this::getMessageButThrowsException);
        } catch (Exception e) {
            final long time = timer.stop().getTimeInSeconds();
            Assert.assertTrue(time >= 5);
        }
    }

}
