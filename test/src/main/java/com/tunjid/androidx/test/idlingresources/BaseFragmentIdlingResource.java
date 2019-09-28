package com.tunjid.androidx.test.idlingresources;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

/**
 * {@link BaseIdlingResource} for fragments with a tag.
 *
 * Created by tj.dahunsi on 4/29/17.
 */
public abstract class BaseFragmentIdlingResource extends BaseIdlingResource {
    protected FragmentManager fragmentManager;

    /**
     * The tag used to identify the fragment.
     */
    protected String fragmentTag;

    public BaseFragmentIdlingResource(AppCompatActivity activity, String fragmentTag, boolean unregisterSelf) {
        this(activity.getSupportFragmentManager(), fragmentTag, unregisterSelf);
    }

    public BaseFragmentIdlingResource(FragmentManager fragmentManager, String fragmentTag, boolean unregisterSelf) {
        super(unregisterSelf);

        this.fragmentManager = fragmentManager;
        this.fragmentTag = fragmentTag;
    }

    /**
     * Finds the Fragment that this Idling Resource is responsible for.
     */
    protected Fragment findFragmentByTag() {
        return fragmentManager.findFragmentByTag(fragmentTag);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName() + ": " + fragmentTag;
    }
}
