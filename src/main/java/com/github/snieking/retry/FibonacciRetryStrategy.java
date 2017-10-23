package com.github.snieking.retry;

import java.util.Optional;
import java.util.function.Supplier;

public class FibonacciRetryStrategy implements RetryStrategy {

    private static final int DEFAULT_MAX_FIB = 10;
    private static final long DEFAULT_OFFSET = 100;

    private int maxFib;
    private long offset;

    private FibonacciRetryStrategy(int maxFib, long offset) {
        this.maxFib = maxFib;
        this.offset = offset;
    }

    @Override
    public Object nonRetryExceptions(Class... exceptions) {
        return null;
    }

    @Override
    public void perform(Runnable runnable) {

    }

    @Override
    public <T> Optional<T> performAndGet(Supplier<T> task) {
        return null;
    }

    /**
     * Creates a default {@link FibonacciRetryStrategy} with a maxFib of 10 and an offset of 100 milliseconds.
     *
     * @return {@link FibonacciRetryStrategy} instance.
     */
    public static FibonacciRetryStrategy createRetryStrategy() {
        return new FibonacciRetryStrategy(DEFAULT_MAX_FIB, DEFAULT_OFFSET);
    }

    /**
     * Creates a default {@link FibonacciRetryStrategy} with a provided maxFib and a default offset of 100 milliseconds.
     *
     * @param maxFib the maxFib that it will iterate to. Can also be looked at as maxRetries.
     * @return {@link FibonacciRetryStrategy} instance.
     */
    public static FibonacciRetryStrategy createRetryStrategy(int maxFib) {
        return new FibonacciRetryStrategy(maxFib, DEFAULT_OFFSET);
    }

    /**
     * Creates a {@link FibonacciRetryStrategy} with a default maxFib of 10 and a provided milliseconds offset.
     *
     * @param offset decides what the start of fib will be. For example 100: 100 + 100 + 200 + 400.
     * @return {@link FibonacciRetryStrategy} instance.
     */
    public static FibonacciRetryStrategy createRetryStrategy(long offset) {
        return new FibonacciRetryStrategy(DEFAULT_MAX_FIB, offset);
    }

    /**
     * Creates a {@link FibonacciRetryStrategy} with a provided maxFib and milliseconds offset.
     *
     * @param maxFib the maxFib that it will iterate to. Can also be looked at as maxRetries.
     * @param offset decides what the start of fib will be. For example 100: 100 + 100 + 200 + 400.
     * @return @return {@link FibonacciRetryStrategy} instance.
     */
    public static FibonacciRetryStrategy createRetryStrategy(int maxFib, long offset) {
        return new FibonacciRetryStrategy(maxFib, offset);
    }
}
