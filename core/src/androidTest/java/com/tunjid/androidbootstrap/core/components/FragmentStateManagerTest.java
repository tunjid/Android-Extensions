package com.tunjid.androidbootstrap.core.components;

import android.support.test.annotation.UiThreadTest;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.tunjid.androidbootstrap.core.R;
import com.tunjid.androidbootstrap.core.testclasses.TestActivity;
import com.tunjid.androidbootstrap.core.testclasses.TestFragment;
import com.tunjid.androidbootstrap.test.TestUtils;
import com.tunjid.androidbootstrap.test.idlingresources.FragmentVisibleIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Tests the {@link FragmentStateManager}
 * <p>
 * Created by tj.dahunsi on 4/29/17.
 */

@RunWith(AndroidJUnit4.class)
public class FragmentStateManagerTest {

    private static final String TAG_A = "A";
    private static final String TAG_B = "B";

    private AppCompatActivity activity;
    private FragmentStateManager fragmentStateManager;

    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<>(TestActivity.class);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        activity = (AppCompatActivity) activityRule.getActivity();
        fragmentStateManager = new FragmentStateManager(activity.getSupportFragmentManager());
    }

    @After
    public void tearDown() {
        activity.finish();
        activity = null;

        // Unregister all idling resources before new tests start
        TestUtils.unregisterAllIdlingResources();
    }

    @Test
    public void testFragmentTagsAdded() throws Throwable {
        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
        TestFragment testFragment = TestFragment.newInstance(TAG_A);

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragment, TAG_A)
                .addToBackStack(TAG_A)
                .commit();

        FragmentVisibleIdlingResource resource = new FragmentVisibleIdlingResource(fragmentManager, TAG_A, true);
        Espresso.registerIdlingResources(resource);

        activityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assertTrue(fragmentStateManager.fragmentTags.contains(TAG_A));
                assertTrue(fragmentStateManager.fragmentTags.size() == 1);
            }
        });
    }

    @Test
    public void testFragmentTagsRestored() throws Throwable {
        FragmentManager fragmentManager = fragmentStateManager.fragmentManager;
        TestFragment testFragment = TestFragment.newInstance(TAG_A);

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragment, TAG_A)
                .addToBackStack(TAG_A)
                .commit();

        FragmentVisibleIdlingResource resource = new FragmentVisibleIdlingResource(fragmentManager, TAG_A, true);
        Espresso.registerIdlingResources(resource);

        activityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // create new instance of fragentStateManager and confirm all
                // the old tags are restored
                fragmentStateManager = new FragmentStateManager(activity.getSupportFragmentManager());

                assertTrue(fragmentStateManager.fragmentTags.contains(TAG_A));
                assertTrue(fragmentStateManager.fragmentTags.size() == 1);
            }
        });
    }

    @Test//(expected = IllegalStateException.class)
    @UiThreadTest
    public void testExceptionNotAddedToBackStack() {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        TestFragment testFragment = TestFragment.newInstance(TAG_A);

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(FragmentStateManager.MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK);

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragment, TAG_A)
                .commitNow();
    }

    @Test
    @UiThreadTest
    public void testAddAndRemove() {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        TestFragment testFragmentA = TestFragment.newInstance(TAG_A);
        TestFragment testFragmentB = TestFragment.newInstance(TAG_B);

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(FragmentStateManager.MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK);

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragmentA, TAG_A)
                .commitNow();

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragmentB, TAG_B)
                .commitNow();

        assertEquals(fragmentManager.getBackStackEntryCount(), 2);

        fragmentManager.popBackStackImmediate();
        assertEquals(fragmentManager.getBackStackEntryCount(), 1);

        fragmentManager.popBackStackImmediate();
        assertEquals(fragmentManager.getBackStackEntryCount(), 0);
    }
}