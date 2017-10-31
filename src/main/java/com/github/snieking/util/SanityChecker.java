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

/**
 * Utility class for performing sanity checks on objects.
 *
 * @author Viktor Plane
 */
public class SanityChecker {

    /**
     * Verifies that no provided object is null.
     *
     * @param errorMsg the error message that should be included in the exception if the verification fails
     * @param objects  the list of objects that should be verified.
     * @throws IllegalArgumentException if a null object is found.
     */
    public static void verifyNoObjectIsNull(final String errorMsg, Object... objects) throws IllegalArgumentException {
        for (Object o : objects) {
            if (o == null) {
                throw new IllegalArgumentException(errorMsg);
            }
        }
    }
}
