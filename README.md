# Utilities

## Retry
There is different retry strategies available:
* OneTimeRetryStrategy which performs one retry.
* ExponentialRetryStrategy which performs multiple retries with an exponential increase of duration between the attempts.


```java
// Build an OneTimeRetryStrategy which takes a duration as argument.
// The duration specifies how long it should wait before next retry.
RetryStrategy retryStrategy = OneTimeRetryStrategy
                                .create(Duration.ofSeconds(5));

// Perform a runnable, with no return.
retryStrategy.perform(() -> personRepository.update(person));

// Perform a supplier with return. Wraps the return in an java.util.Optional.
Optional<Person> person = retryStrategy
                            .performAndGet(() -> personRepository.findById("Viktor"));
```

```java
// Build an ExponentialRetryStrategy which takes an int maxExponent and long base.
// The maxExponent specifies the max value of n in b^n.
// The base specifies the base in milliseconds to wait before retrying.
RetryStrategy retryStrategy = ExponentialRetryStrategy.create(Duration.ofSeconds(5));

// Perform a runnable, with no return.
retryStrategy.perform(() -> personRepository.update(person));

// Perform a supplier with return. Wraps the return in an java.util.Optional.
Optional<Person> person = retryStrategy
                            .performAndGet(() -> personRepository.findById("Viktor"));
```

The interface also makes it possible to specifiy that no retry should be performed on occurance of exceptions in a list.
```java
RetryStrategy oneTimeRetryStrategy = OneTimeRetryStrategy
                                      .create(Duration.ofSeconds(5))
                                      .nonRetryExceptions(IllegalStateException.class, 
                                                          IllegalArgumentException.class);
```