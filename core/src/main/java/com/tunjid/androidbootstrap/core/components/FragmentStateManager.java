package com.tunjid.androidbootstrap.core.components;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.tunjid.androidbootstrap.core.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class that keeps track of the {@link Fragment fragments} in an
 * {@link android.app.Activity activity's} {@link FragmentManager}
 * <p>
 * Created by tj.dahunsi on 4/23/17.
 */

public class FragmentStateManager {

    static final String MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK = "A fragment cannot be added to a FragmentManager managed by FragmentStateManager without adding it to the backstack";

    final FragmentManager fragmentManager;
    final Set<String> fragmentTags;

    private String currentFragmentTag;

    /**
     * A class that keeps track of the fragments in the FragmentManager
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks =
            new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentAttached(FragmentManager fm, Fragment f, Context context) {
                    fragmentTags.add(f.getTag());

                    int backstackEntryCount = fm.getBackStackEntryCount();
                    int numTrackedTags = fragmentTags.size();

                    if (backstackEntryCount != numTrackedTags) {
                        List<String> backstackEntries = new ArrayList<>(backstackEntryCount);
                        for (int i = 0; i < backstackEntryCount; i++) {
                            backstackEntries.add(fm.getBackStackEntryAt(i).getName());
                        }

                        throw new IllegalStateException(MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK
                                + "\n Fragment Attached: " + f.toString()
                                + "\n Fragment Tag: " + f.getTag()
                                + "\n Number of Tracked Fragments: " + numTrackedTags
                                + "\n Backstack Entry Count: " + backstackEntryCount
                                + "\n Tracked Fragments: " + fragmentTags
                                + "\n Back Stack Entries: " + backstackEntries
                        );
                    }
                }

                @Override
                public void onFragmentDetached(FragmentManager fm, Fragment f) {
                    fragmentTags.remove(f.getTag());
                }
            };

    /**
     * Used to keep track of the current fragment shown in the fragment manager
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final FragmentManager.OnBackStackChangedListener backStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    Fragment fragment = fragmentManager.findFragmentById(R.id.main_fragment_container);

                    if (fragment != null) {
                        if (fragment.getTag() == null) {
                            throw new IllegalArgumentException("Fragment instance "
                                    + fragment.getClass().getName()
                                    + " with no tags cannot be added to the backstack with " +
                                    "a FragmentStateManager");
                        }
                        currentFragmentTag = fragment.getTag();
                    }
                }
            };

    public FragmentStateManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        fragmentTags = new HashSet<>();

        int backStackCount = fragmentManager.getBackStackEntryCount();

        // Restore previous backstack entries in the Fragment manager
        for (int i = 0; i < backStackCount; i++) {
            fragmentTags.add(fragmentManager.getBackStackEntryAt(i).getName());
        }

        fragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
        fragmentManager.addOnBackStackChangedListener(backStackChangedListener);
    }

    /**
     * Gets the last fragment added to the {@link FragmentManager}
     */
    @Nullable
    public Fragment getCurrentFragment() {
        return currentFragmentTag == null
                ? null
                : fragmentManager.findFragmentByTag(currentFragmentTag);
    }

    /**
     * Attempts to show the fragment provided, if the fragment does not already exist in the
     * {@link FragmentManager} under the specified tag.
     *
     * @param fragment    The fragment to show.
     * @param transaction The fragment transaction to show the supplied fragment with.
     * @param tag         the value to supply to this fragment for it's backstack entry name and tag
     * @return true if the a fragment provided will be shown, false if the fragment instance already
     * exists and will be restored instead.
     */
    public final boolean showFragment(FragmentTransaction transaction, Fragment fragment, String tag) {

        boolean fragmentShown = false;

        if (currentFragmentTag == null || !currentFragmentTag.equals(tag)) {

            boolean fragmentAlreadyExists = fragmentTags.contains(tag);

            fragmentShown = !fragmentAlreadyExists;

            Fragment fragmentToShow = fragmentAlreadyExists
                    ? fragmentManager.findFragmentByTag(tag)
                    : fragment;

            transaction.addToBackStack(tag)
                    .replace(R.id.main_fragment_container, fragmentToShow, tag)
                    .commit();
        }
        return fragmentShown;
    }
}
