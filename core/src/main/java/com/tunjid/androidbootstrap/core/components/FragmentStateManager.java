package com.tunjid.androidbootstrap.core.components;

import android.os.Bundle;
import android.support.annotation.IdRes;
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

    private final @IdRes int idResource;
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
                public void onFragmentCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
                    // Not a fragment managed by this FragmentStateManager
                    if (f.getId() != idResource) return;

                    fragmentTags.add(f.getTag());

                    int totalBackStackCount = fm.getBackStackEntryCount();
                    int numTrackedTags = fragmentTags.size();

                    Set<String> uniqueBackstackEntries = new HashSet<>();
                    List<String> backstackEntries = new ArrayList<>(totalBackStackCount);

                    for (int i = 0; i < totalBackStackCount; i++) {
                        FragmentManager.BackStackEntry entry = fm.getBackStackEntryAt(i);
                        String entryName = entry.getName();
                        Fragment shownFragment = fragmentManager.findFragmentByTag(entryName);

                        if (shownFragment == null) {
                            throw new IllegalStateException("Fragment backstack entry name does " +
                                    "not match a tag in the fragment manager");
                        }
                        if (shownFragment.getId() != idResource) {
                            // Not a fragment managed by us, continue
                            continue;
                        }

                        uniqueBackstackEntries.add(entryName);
                        backstackEntries.add(entryName);
                    }

                    // Maje sure every fragment shown managed by us is added to the backstack
                    if (uniqueBackstackEntries.size() != numTrackedTags && savedInstanceState == null) {
                        throw new IllegalStateException(MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK
                                + "\n Fragment Attached: " + f.toString()
                                + "\n Fragment Tag: " + f.getTag()
                                + "\n Number of Tracked Fragments: " + numTrackedTags
                                + "\n Backstack Entry Count: " + totalBackStackCount
                                + "\n Tracked Fragments: " + fragmentTags
                                + "\n Back Stack Entries: " + backstackEntries
                        );
                    }
                }

                @Override
                public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
                    // Not a fragment managed by this FragmentStateManager
                    if (f.getId() != idResource) return;

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
        this(fragmentManager, R.id.main_fragment_container);
    }

    @SuppressWarnings("WeakerAccess")
    public FragmentStateManager(FragmentManager fragmentManager, @IdRes int idResource) {
        this.fragmentManager = fragmentManager;
        this.idResource = idResource;
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

    @IdRes
    public int getIdResource() {
        return idResource;
    }

    /**
     * Attempts to show the fragment provided, if the fragment does not already exist in the
     * {@link FragmentManager} under the specified tag.
     *
     * @param transaction         The fragment transaction to show the supplied fragment with.
     * @param fragmentTagProvider A fragmentTagProvider provider specifying the
     *                            fragment and tag
     * @return true if the a fragment provided will be shown, false if the fragment instance already
     * exists and will be restored instead.
     */
    public final boolean showFragment(FragmentTransaction transaction, FragmentTagProvider fragmentTagProvider) {
        return showFragment(transaction, fragmentTagProvider.getFragment(), fragmentTagProvider.getStableTag());
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
                    .replace(idResource, fragmentToShow, tag)
                    .commit();
        }
        return fragmentShown;
    }

    /**
     * An interface to provide unique tags for {@link Fragment Fragments}
     */

    public interface FragmentTagProvider {
        String getStableTag();

        Fragment getFragment();
    }
}
