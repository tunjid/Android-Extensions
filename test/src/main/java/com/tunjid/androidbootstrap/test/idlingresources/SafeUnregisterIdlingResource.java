package com.tunjid.androidbootstrap.test.idlingresources;

import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;

import java.util.Collection;

/**
 * IdlingResource used to safely unregister other idling resources.
 */
public class SafeUnregisterIdlingResource extends BaseIdlingResource {

    private BaseIdlingResource resourceToUnregister;

    @SuppressWarnings("WeakerAccess")
    public SafeUnregisterIdlingResource(BaseIdlingResource resourceToUnregister) {
        // Never attempt to unregister itself
        super(false);
        this.resourceToUnregister = resourceToUnregister;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName() + " with resource to unregister: " + resourceToUnregister.getName();
    }

    @Override
    public boolean isIdleNow() {
        if (hasIdled) return true;

        Collection<IdlingResource> idlingResourceList = IdlingRegistry.getInstance().getResources();
        boolean hasIdleDependent = resourceToUnregister.hasIdled;

        if (hasIdleDependent) {
            IdlingRegistry.getInstance().unregister(resourceToUnregister);
            executeOnIdle();
        }

        return setIdled(hasIdleDependent && !idlingResourceList.contains(resourceToUnregister));
    }

    @Override
    public void setIdleCallBack(IdleCallBack idleCallBack) {
        throw new IllegalArgumentException(getClass().getSimpleName() + " cannot set idle callbacks.");
    }
}
