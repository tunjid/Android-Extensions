package com.tunjid.androidbootstrap.test;

import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;

import java.util.Collection;

/**
 * Aggregation of utility test functions
 */

public class TestUtils {

    public static void unregisterAllIdlingResources() {
        Collection<IdlingResource> idlingResources = IdlingRegistry.getInstance().getResources();
        for (IdlingResource resource : idlingResources) {
            IdlingRegistry.getInstance().unregister(resource);
        }
    }
}
