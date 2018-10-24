package com.tunjid.androidbootstrap.test.idlingresources;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
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
    @SuppressWarnings("WeakerAccess")
    protected boolean hasIdled;

    /**
     * The callback to notify Espresso when this resource transitions to idle.
     */
    @SuppressWarnings("WeakerAccess")
    protected ResourceCallback resourceCallback;

    /**
     * A callback used when this resource is idled.
     */
    @SuppressWarnings("WeakerAccess")
    public IdleCallBack idleCallBack;

    /**
     * Default constructor.
     *
     * @param unregisterSelf Whether or not this idling resource should unregister itself when it is done.
     */
    @SuppressWarnings("WeakerAccess")
    public BaseIdlingResource(boolean unregisterSelf) {
        if (unregisterSelf) {
            setIdleCallBack(this);
        }
    }

    public void setIdleCallBack(IdleCallBack idleCallBack) {
        this.idleCallBack = idleCallBack;
    }

    @SuppressWarnings("WeakerAccess")
    public void safelyUnregisterSelf() {
        getIdlingRegistry().register(new SafeUnregisterIdlingResource(this));
    }

    @SuppressWarnings("WeakerAccess")
    protected void executeOnIdle() {
        if (getIdlingRegistry().getResources().contains(this)) resourceCallback.onTransitionToIdle();
        if (idleCallBack != null && hasIdled) idleCallBack.onIdle();
    }

    @SuppressWarnings("WeakerAccess")
    protected boolean setIdled(boolean isIdle) {
        hasIdled = isIdle;
        return hasIdled;
    }

    @SuppressWarnings("WeakerAccess")
    protected IdlingRegistry getIdlingRegistry() {
        return IdlingRegistry.getInstance();
    }

    /**
     * Handles the idle state of the idling resource.
     */
    @SuppressWarnings("WeakerAccess")
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
