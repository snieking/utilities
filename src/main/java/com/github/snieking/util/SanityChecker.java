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
     * @param objects the list of objects that should be verified.
     *
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
