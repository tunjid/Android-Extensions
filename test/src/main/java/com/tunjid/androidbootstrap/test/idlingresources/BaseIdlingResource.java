package com.tunjid.androidbootstrap.test.idlingresources;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.util.Log;

/**
 * Base class for all {@link IdlingResource}.
 * <p>
 * Created by tj.dahunsi on 4/29/17.
 */
public abstract class BaseIdlingResource implements IdlingResource, IdleCallBack {

    private static final String TAG = BaseIdlingResource.class.getSimpleName();

    /**
     * Whether or not this resource is done idling.
     */
    protected boolean hasIdled;

    /**
     * The callback to notify Espresso when this resource transitions to idle.
     */
    protected ResourceCallback resourceCallback;

    /**
     * A callback used when this resource is idled.
     */
    public IdleCallBack idleCallBack;

    /**
     * Default constructor.
     *
     * @param unregisterSelf Whether or not this idling resource should unregister itself when it is done.
     */
    public BaseIdlingResource(boolean unregisterSelf) {
        if (unregisterSelf) {
            setIdleCallBack(this);
        }
    }

    public void setIdleCallBack(IdleCallBack idleCallBack) {
        this.idleCallBack = idleCallBack;
    }

    public void safelyUnregisterSelf() {
        Espresso.registerIdlingResources(new SafeUnregisterIdlingResource(this));
    }

    protected void executeOnIdle() {
        resourceCallback.onTransitionToIdle();
        if (idleCallBack != null && hasIdled) idleCallBack.onIdle();
    }

    protected boolean setIdled(boolean isIdle) {
        hasIdled = isIdle;
        return hasIdled;
    }

    /**
     * Handles the idle state of the idling resource.
     */
    protected boolean handleIsIdle(boolean isIdle) {
        if (isIdle) executeOnIdle();

        return setIdled(isIdle);
    }

    @Override
    public void onIdle() {
        Log.v(TAG, "Unregistering self: " + getName());
        safelyUnregisterSelf();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.resourceCallback = callback;
    }
}
