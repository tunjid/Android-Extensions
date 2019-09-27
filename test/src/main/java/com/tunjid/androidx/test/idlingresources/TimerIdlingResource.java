package com.tunjid.androidx.test.idlingresources;

/**
 * Idling resource that waits for a certain amount of time.
 * <p>
 * Created by tj.dahunsi on 4/29/17.
 */
public class TimerIdlingResource extends BaseIdlingResource {
    private long startTime;
    private long timeToIdle;

    public TimerIdlingResource(int timeToIdle, boolean unregisterSelf) {
        super(unregisterSelf);

        this.startTime = System.currentTimeMillis();
        this.timeToIdle = timeToIdle;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName() + hashCode();
    }

    @Override
    public boolean isIdleNow() {
        // Wait for time to pass
        long elapsed = System.currentTimeMillis() - startTime;
        boolean isIdle = (elapsed >= timeToIdle);

        return super.handleIsIdle(isIdle);
    }
}
