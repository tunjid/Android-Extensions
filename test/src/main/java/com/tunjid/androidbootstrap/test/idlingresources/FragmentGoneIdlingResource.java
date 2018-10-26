package com.tunjid.androidbootstrap.test.idlingresources;

import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Idling resource that idles until a fragment has disappeared.
 * <p>
 * Created by tj.dahunsi on 4/29/17.
 */
public class FragmentGoneIdlingResource extends BaseFragmentIdlingResource {

    public FragmentGoneIdlingResource(AppCompatActivity activity, String fragmentTag, boolean unregisterSelf) {
        super(activity, fragmentTag, unregisterSelf);
    }

    public FragmentGoneIdlingResource(FragmentManager fragmentManager, String fragmentTag, boolean unregisterSelf) {
        super(fragmentManager, fragmentTag, unregisterSelf);
    }

    @Override
    public boolean isIdleNow() {
        // Wait until fragment is gone from fragment manager
        boolean isIdle = (findFragmentByTag() == null);

        return super.handleIsIdle(isIdle);
    }
}
