package com.tunjid.androidbootstrap.core.abstractclasses;

import android.app.Activity;

/**
 * An interface for objects who wish to handle {@link Activity#onBackPressed()}
 * <p>
 * Created by tj.dahunsi on 5/6/17.
 */

public interface BackPressInterceptor {
    boolean handledBackPress();
}
