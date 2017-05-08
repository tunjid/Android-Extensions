package com.tunjid.androidbootstrap.test.idlingresources;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;

import java.util.List;

/**
 * IdlingResource used to safely unregister other idling resources.
 * <p>
 * Created by tj.dahunsi on 4/29/17.
 */
public class SafeUnregisterIdlingResource extends BaseIdlingResource {

    private BaseIdlingResource resourceToUnregister;

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

        List<IdlingResource> idlingResourceList = Espresso.getIdlingResources();
        boolean hasIdleDependent = resourceToUnregister.hasIdled;

        if (hasIdleDependent) {
            Espresso.unregisterIdlingResources(resourceToUnregister);
            executeOnIdle();
        }

        return setIdled(hasIdleDependent && !idlingResourceList.contains(resourceToUnregister));
    }

    @Override
    public void setIdleCallBack(IdleCallBack idleCallBack) {
        throw new IllegalArgumentException(getClass().getSimpleName() + " cannot set idle callbacks.");
    }
}
