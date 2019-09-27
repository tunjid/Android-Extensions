package com.tunjid.androidx.test.resources;

import androidx.test.espresso.IdlingResource;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Idles test execution till a condition is met or the time out elapses
 * <p>
 * Created by tj.dahunsi on 5/5/17.
 */

public class TestIdler {

    private final long timeOut;
    private final TimeUnit timeUnit;

    public TestIdler(long timeOut, TimeUnit timeUnit) {
        this.timeOut = timeOut;
        this.timeUnit = timeUnit;
    }

    public void till(TestIdler.TestCondition condition) throws TimeoutException {
        long end = System.currentTimeMillis() + timeUnit.toMillis(timeOut);
        for (; ; ) {
            if (condition.satisfied()) {
                break;
            }
            else if (System.currentTimeMillis() > end) {
                throw new TimeoutException("TestIdler for TestCondition " + condition + " timed out");
            }
        }
    }

    public void till(final IdlingResource idlingResource) throws TimeoutException {
        till(idlingResource::isIdleNow);
    }

    public interface TestCondition {
        boolean satisfied();
    }
}
