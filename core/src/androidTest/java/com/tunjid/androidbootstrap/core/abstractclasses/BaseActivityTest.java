package com.tunjid.androidbootstrap.core.abstractclasses;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tunjid.androidbootstrap.core.testclasses.TestActivity;
import com.tunjid.androidbootstrap.core.testclasses.TestFragment;
import com.tunjid.androidbootstrap.test.TestUtils;
import com.tunjid.androidbootstrap.test.idlingresources.FragmentVisibleIdlingResource;
import com.tunjid.androidbootstrap.test.resources.TestIdler;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

/**
 * Tests methods in {@link BaseActivity}.
 * <p>
 * Created by tj.dahunsi on 4/29/17.
 */
@RunWith(AndroidJUnit4.class)
public class BaseActivityTest {

    private TestActivity testActivity;
    private TestIdler testIdler;

    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<>(TestActivity.class);

    @Before
    public void setUp() {
        testActivity = (TestActivity) activityRule.getActivity();
        testIdler = new TestIdler(5, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() {
        testActivity.getSupportFragmentManager().popBackStack("", 0);
        testActivity.finish();
        testActivity = null;

        // Unregister all idling resources before new tests start
        TestUtils.unregisterAllIdlingResources();
    }

    @Test
    public void testShowFragment() throws Exception {
        final TestFragment testFragment = TestFragment.newInstance("A");

        assertTrue(testActivity.showFragment(testFragment));

        // Wait till the fragment is attached to the activity
        FragmentVisibleIdlingResource resource = new FragmentVisibleIdlingResource(testActivity, testFragment.getStableTag(), true);
        testIdler.till(resource);

        assertEquals(testFragment, testActivity.getSupportFragmentManager().findFragmentByTag(testFragment.getStableTag()));
    }

    @Test
    public void testGetCurrentFragment() throws Exception {
        final TestFragment testFragment = TestFragment.newInstance("B");

        assertTrue(testActivity.showFragment(testFragment));

        // Wait till the fragment is attached to the activity
        FragmentVisibleIdlingResource resource = new FragmentVisibleIdlingResource(testActivity, testFragment.getStableTag(), true);
        testIdler.till(resource);

        assertEquals(testFragment, testActivity.getCurrentFragment());
    }

    @Test
    public void testSingleTagInstance() throws Exception {
        final TestFragment first = TestFragment.newInstance("C");
        final TestFragment second = TestFragment.newInstance("C");

        assertTrue(testActivity.showFragment(first));

        final FragmentVisibleIdlingResource firstVisible = new FragmentVisibleIdlingResource(testActivity, first.getStableTag(), true);
        testIdler.till(firstVisible);

        // assert that the second fragment is NOT queued to be shown because they use the same tag
        assertFalse(testActivity.showFragment(second));
    }

    @Test
    public void testMultipleTagInstance() throws Exception {
        final TestFragment first = TestFragment.newInstance("D");
        final TestFragment second = TestFragment.newInstance("E");

        // assert that the first fragment is queued to be shown
        assertTrue(testActivity.showFragment(first));

        final FragmentVisibleIdlingResource firstVisible = new FragmentVisibleIdlingResource(testActivity, first.getStableTag(), true);
        testIdler.till(firstVisible);

        // assert that the second fragment is queued to be shown because the tags are different
        assertTrue(testActivity.showFragment(second));
    }

    @Test
    public void testPopBackStack() throws Exception {
        final TestFragment first = TestFragment.newInstance("F");
        final TestFragment second = TestFragment.newInstance("G");

        assertTrue(testActivity.showFragment(first));

        // Wait for the first fragment to be visible
        testIdler.till(new FragmentVisibleIdlingResource(testActivity, first.getStableTag(), true));

        assertTrue(testActivity.showFragment(second));

        // Wait till second fragment shows
        testIdler.till(new FragmentVisibleIdlingResource(testActivity, second.getStableTag(), true));

        // Pop second fragment off
        testActivity.onBackPressed();

        // Wait till the first fragment is visible again
        testIdler.till(new FragmentVisibleIdlingResource(testActivity, first.getStableTag(), true));

        // Assert fragment with tag TAG_B is gone
        assertNull(testActivity.getSupportFragmentManager().findFragmentByTag(second.getStableTag()));

        // Assert fragment with tag TAG_A is still available
        assertNotNull(testActivity.getSupportFragmentManager().findFragmentByTag(first.getStableTag()));

        // Assert first and the fragment retrieved with the tag are equal and the same
        assertSame(first, testActivity.getSupportFragmentManager().findFragmentByTag(first.getStableTag()));

        // Assert that the first was indeed retrieved from the backstack
        TestFragment firstFoundByTag = (TestFragment) testActivity.getSupportFragmentManager().findFragmentByTag(first.getStableTag());
        assertTrue(firstFoundByTag.restoredFromBackStack());
        assertTrue(first.restoredFromBackStack());
    }

    @Test
    public void testHandledBackPress() throws Exception {
        final TestFragment first = TestFragment.newInstance("F");
        final TestFragment second = TestFragment.newInstance("G", true);

        assertTrue(testActivity.showFragment(first));

        // Wait for the first fragment to be visible
        testIdler.till(new FragmentVisibleIdlingResource(testActivity, first.getStableTag(), true));

        assertTrue(testActivity.showFragment(second));

        // Wait till second fragment shows
        testIdler.till(new FragmentVisibleIdlingResource(testActivity, second.getStableTag(), true));

        // Press back
        testActivity.onBackPressed();

        // Assert the second fragment is still visible
        assertNotNull(testActivity.getSupportFragmentManager().findFragmentByTag(second.getStableTag()));

        // Assert the first fragment is still available
        assertNotNull(testActivity.getSupportFragmentManager().findFragmentByTag(first.getStableTag()));

        // Assert first and the fragment retrieved with the tag are equal and the same
        assertSame(first, testActivity.getSupportFragmentManager().findFragmentByTag(first.getStableTag()));
    }
}