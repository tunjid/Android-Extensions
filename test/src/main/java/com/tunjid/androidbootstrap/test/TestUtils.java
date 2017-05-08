package com.tunjid.androidbootstrap.test;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;

import java.util.List;

/**
 * Aggregation of utility test functions
 * <p>
 * Created by Shemanigans on 4/29/17.
 */

public class TestUtils {

    public static void unregisterAllIdlingResources() {
        List<IdlingResource> idlingResources = Espresso.getIdlingResources();
        for (IdlingResource resource : idlingResources) {
            Espresso.unregisterIdlingResources(resource);
        }
    }
}
