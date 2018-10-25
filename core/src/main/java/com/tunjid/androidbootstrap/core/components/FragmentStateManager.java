package com.tunjid.androidbootstrap.core.components;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

    private static final String CURRENT_FRAGMENT_KEY = "com.tunjid.androidbootstrap.core.components.FragmentStateManager.currentFragmentTag";
    private static final String MSG_FRAGMENT_MISMATCH = "Fragment back stack entry name does not match a tag in the fragment manager";
    static final String MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK = "A fragment cannot be added to a FragmentManager managed by FragmentStateManager without adding it to the back stack";
    private static final String MSG_DODGY_FRAGMENT = "Tag exists in FragmentStateManager but not in FragmentManager";

    private final @IdRes
    int idResource;
    final FragmentManager fragmentManager;
    final Set<String> fragmentTags;

    private String currentFragmentTag;

    /**
     * A class that keeps track of the fragments in the FragmentManager
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentCreated(@NonNull FragmentManager fm, Fragment f, Bundle savedInstanceState) {
            // Not a fragment managed by this FragmentStateManager
            if (f.getId() != idResource) return;

            fragmentTags.add(f.getTag());

            int totalBackStackCount = fm.getBackStackEntryCount();
            int numTrackedTags = fragmentTags.size();

            Set<String> uniqueBackStackEntries = new HashSet<>();
            List<String> backStackEntries = new ArrayList<>(totalBackStackCount);

            for (int i = 0; i < totalBackStackCount; i++) {
                FragmentManager.BackStackEntry entry = fm.getBackStackEntryAt(i);
                String entryName = entry.getName();
                Fragment shownFragment = fragmentManager.findFragmentByTag(entryName);

                if (shownFragment == null) throw new IllegalStateException(MSG_FRAGMENT_MISMATCH);
                // Not a fragment managed by us, continue
                if (shownFragment.getId() != idResource) continue;

                uniqueBackStackEntries.add(entryName);
                backStackEntries.add(entryName);
            }

            // Make sure every fragment shown managed by us is added to the back stack
            if (uniqueBackStackEntries.size() != numTrackedTags && savedInstanceState == null) {
                throw new IllegalStateException(MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK
                        + "\n Fragment Attached: " + f.toString()
                        + "\n Fragment Tag: " + f.getTag()
                        + "\n Number of Tracked Fragments: " + numTrackedTags
                        + "\n Backstack Entry Count: " + totalBackStackCount
                        + "\n Tracked Fragments: " + fragmentTags
                        + "\n Back Stack Entries: " + backStackEntries
                );
            }
        }

        @Override
        public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
            if (f.getTag() == null)
                throw new IllegalArgumentException("Fragment instance "
                        + f.getClass().getName()
                        + " with no tag cannot be added to the back stack with " +
                        "a FragmentStateManager");

            currentFragmentTag = f.getTag();
        }

        @Override
        public void onFragmentDestroyed(@NonNull FragmentManager fm, Fragment f) {
            if (f.getId() == idResource) fragmentTags.remove(f.getTag());
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

        // Restore previous back stack entries in the Fragment manager
        for (int i = 0; i < backStackCount; i++) {
            fragmentTags.add(fragmentManager.getBackStackEntryAt(i).getName());
        }

        fragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
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
        boolean fragmentShown;
        if (currentFragmentTag != null && currentFragmentTag.equals(tag)) return false;

        boolean fragmentAlreadyExists = fragmentTags.contains(tag);

        fragmentShown = !fragmentAlreadyExists;

        Fragment fragmentToShow = fragmentAlreadyExists
                ? fragmentManager.findFragmentByTag(tag)
                : fragment;

        if (fragmentToShow == null) throw new NullPointerException(MSG_DODGY_FRAGMENT);

        transaction.addToBackStack(tag)
                .replace(idResource, fragmentToShow, tag)
                .commit();

        return fragmentShown;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_FRAGMENT_KEY, currentFragmentTag);
    }

    public void onRestoreInstanceState(Bundle savedState) {
        if (savedState != null) currentFragmentTag = savedState.getString(CURRENT_FRAGMENT_KEY);
    }

    /**
     * An interface to provide unique tags for {@link Fragment Fragments}
     */

    public interface FragmentTagProvider {
        String getStableTag();

        Fragment getFragment();
    }
}
