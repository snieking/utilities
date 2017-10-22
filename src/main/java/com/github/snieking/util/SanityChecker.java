package com.github.snieking.util;

/**
 * Utility class for performing sanity checks on objects.
 *
 * @author Viktor Plane
 */
public class SanityChecker {

    /**
     * Verifies that no provided object is null.
     * @throws {@link java.lang.IllegalArgumentException} if a null object is found.
     */
    public static void verifyNoObjectIsNull(final String errorMsg, Object... objects) throws IllegalArgumentException {
        for (Object o : objects) {
            if (o == null) {
                throw new IllegalArgumentException(errorMsg);
            }
        }
    }
}
