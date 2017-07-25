package com.tunjid.androidbootstrap.core.components;

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.FragmentManager;

import com.tunjid.androidbootstrap.core.R;
import com.tunjid.androidbootstrap.core.testclasses.TestActivity;
import com.tunjid.androidbootstrap.core.testclasses.TestFragment;
import com.tunjid.androidbootstrap.test.TestUtils;
import com.tunjid.androidbootstrap.test.idlingresources.FragmentGoneIdlingResource;
import com.tunjid.androidbootstrap.test.idlingresources.FragmentVisibleIdlingResource;
import com.tunjid.androidbootstrap.test.resources.TestIdler;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Tests the {@link FragmentStateManager}
 * <p>
 * Created by tj.dahunsi on 4/29/17.
 */

@RunWith(AndroidJUnit4.class)
public class FragmentStateManagerTest {

    private static final int DEFAULT_TIME_OUT = 5;

    private static final String TAG_A = "A";
    private static final String TAG_B = "B";

    private TestActivity activity;
    private FragmentStateManager fragmentStateManager;
    private TestIdler testIdler;

    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<>(TestActivity.class);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        testIdler = new TestIdler(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
        activity = (TestActivity) activityRule.getActivity();
        fragmentStateManager = new FragmentStateManager(activity.getSupportFragmentManager());
    }

    @After
    public void tearDown() {
        activity.finish();
        activity = null;
        testIdler = null;

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
        testIdler.till(resource);

        assertTrue(fragmentStateManager.fragmentTags.contains(TAG_A));
        assertTrue(fragmentStateManager.fragmentTags.size() == 1);
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
        testIdler.till(resource);

        // create new instance of fragentStateManager and confirm all
        // the old tags are restored
        fragmentStateManager = new FragmentStateManager(activity.getSupportFragmentManager());

        assertTrue(fragmentStateManager.fragmentTags.contains(TAG_A));
        assertTrue(fragmentStateManager.fragmentTags.size() == 1);
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
    public void testAddAndRemove() throws Throwable {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        TestFragment testFragmentA = TestFragment.newInstance(TAG_A);
        TestFragment testFragmentB = TestFragment.newInstance(TAG_B);

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragmentA, testFragmentA.getStableTag())
                .addToBackStack(testFragmentA.getStableTag())
                .commit();

        testIdler.till(new FragmentVisibleIdlingResource(activity, testFragmentA.getStableTag(), true));

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragmentB, testFragmentB.getStableTag())
                .addToBackStack(testFragmentB.getStableTag())
                .commit();

        testIdler.till(new FragmentVisibleIdlingResource(activity, testFragmentB.getStableTag(), true));

        assertEquals(fragmentManager.getBackStackEntryCount(), 2);

        fragmentManager.popBackStack();

        testIdler.till(new FragmentGoneIdlingResource(activity, testFragmentB.getStableTag(), true));

        assertEquals(fragmentManager.getBackStackEntryCount(), 1);

        fragmentManager.popBackStack();

        testIdler.till(new FragmentGoneIdlingResource(activity, testFragmentA.getStableTag(), true));

        assertEquals(fragmentManager.getBackStackEntryCount(), 0);
    }

    @Test
    public void testIgnoredId() throws Throwable {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        TestFragment testFragmentA = TestFragment.newInstance(TAG_A);
        TestFragment testFragmentB = TestFragment.newInstance(TAG_B);

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragmentA, testFragmentA.getStableTag())
                .addToBackStack(testFragmentA.getStableTag())
                .commit();

        testIdler.till(new FragmentVisibleIdlingResource(activity, testFragmentA.getStableTag(), true));

        assertEquals(fragmentManager.getBackStackEntryCount(), 1);
        assertEquals(fragmentManager.getBackStackEntryCount(), fragmentStateManager.fragmentTags.size());

        fragmentManager.beginTransaction()
                .replace(activity.ignoredLayoutId, testFragmentB, testFragmentB.getStableTag())
                .addToBackStack(testFragmentB.getStableTag())
                .commit();

        testIdler.till(new FragmentVisibleIdlingResource(activity, testFragmentB.getStableTag(), true));

        assertEquals(fragmentManager.getBackStackEntryCount(), 2);
        assertFalse(fragmentManager.getBackStackEntryCount() == fragmentStateManager.fragmentTags.size());
    }
}