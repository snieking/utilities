package com.github.snieking.retry;

public class RetryTester {

    public void throwException (final Exception exception, boolean shouldThrow) throws Exception {
        if (shouldThrow) {
            throw exception;
        }
    }
}
