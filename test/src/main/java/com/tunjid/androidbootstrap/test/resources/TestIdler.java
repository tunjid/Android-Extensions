package com.tunjid.androidbootstrap.test.resources;

import java.util.concurrent.TimeUnit;

/**
 * Idles test execution till a condition is met or the time out elapses
 * <p>
 * Created by Shemanigans on 5/5/17.
 */

public class TestIdler {

    private final long timeOut;
    private final TimeUnit timeUnit;

    public TestIdler(long timeOut, TimeUnit timeUnit) {
        this.timeOut = timeOut;
        this.timeUnit = timeUnit;
    }

    public void till(TestIdler.TestCondition condition) {
        long end = System.currentTimeMillis() + timeUnit.toMillis(timeOut);
        for (; ; ) if (condition.satified() || System.currentTimeMillis() > end) break;
    }

    public interface TestCondition {
        boolean satified();
    }
}
