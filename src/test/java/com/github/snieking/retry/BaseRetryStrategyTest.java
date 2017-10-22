package com.github.snieking.retry;

import org.junit.Before;

public class BaseRetryStrategyTest {

    protected int numOfFails;

    @Before
    public void setup() {
        numOfFails = 0;
    }
}
