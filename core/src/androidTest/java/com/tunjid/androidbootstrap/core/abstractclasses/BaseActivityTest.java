package com.tunjid.androidbootstrap.core.abstractclasses;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tunjid.androidbootstrap.core.testclasses.TestActivity;
import com.tunjid.androidbootstrap.core.testclasses.TestFragment;
import com.tunjid.androidbootstrap.test.TestUtils;
import com.tunjid.androidbootstrap.test.idlingresources.FragmentVisibleIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

/**
 * Tests methods in {@link BaseActivity}.
 * <p>
 * Running cartain assertions on the main thread is to make sure idling resources are
 * idle before assertions.
 * Created by tj.dahunsi on 4/29/17.
 */
@RunWith(AndroidJUnit4.class)
public class BaseActivityTest {

    private static final String TAG_A = "A";
    private static final String TAG_B = "B";

    private TestActivity testActivity;

    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<>(TestActivity.class);

    @Before
    public void setUp() {
        testActivity = (TestActivity) activityRule.getActivity();
    }

    @After
    public void tearDown() {
        testActivity.finish();
        testActivity = null;

        // Unregister all idling resources before new tests start
        TestUtils.unregisterAllIdlingResources();
    }

    @Test
    public void testShowFragment() {
        final TestFragment testFragment = TestFragment.newInstance(TAG_A);

        assertTrue(testActivity.showFragment(testFragment));

        // Wait till the fragment is attached to the activity
        FragmentVisibleIdlingResource resource = new FragmentVisibleIdlingResource(testActivity, testFragment.getStableTag(), true);
        Espresso.registerIdlingResources(resource);

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                assertEquals(testFragment, testActivity.getSupportFragmentManager().findFragmentByTag(testFragment.getStableTag()));
            }
        });
    }

    @Test
    public void testGetCurrentFragment() {
        final TestFragment testFragment = TestFragment.newInstance(TAG_A);

        assertTrue(testActivity.showFragment(testFragment));

        // Wait till the fragment is attached to the activity
        FragmentVisibleIdlingResource resource = new FragmentVisibleIdlingResource(testActivity, testFragment.getStableTag(), true);
        Espresso.registerIdlingResources(resource);

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                assertEquals(testFragment, testActivity.getCurrentFragment());
            }
        });
    }

    @Test
    public void testSingleTagInstance() {
        final TestFragment first = TestFragment.newInstance(TAG_A);
        final TestFragment second = TestFragment.newInstance(TAG_A);

        assertTrue(testActivity.showFragment(first));

        final FragmentVisibleIdlingResource firstVisible = new FragmentVisibleIdlingResource(testActivity, first.getStableTag(), true);
        Espresso.registerIdlingResources(firstVisible);

        // assert that the second fragment is NOT queued to be shown because they use the same tag
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                assertFalse(testActivity.showFragment(second));
            }
        });
    }

    @Test
    public void testMultipleTagInstance() {
        final TestFragment first = TestFragment.newInstance(TAG_A);
        final TestFragment second = TestFragment.newInstance(TAG_B);

        // assert that the first fragment is queued to be shown
        assertTrue(testActivity.showFragment(first));

        final FragmentVisibleIdlingResource firstVisible = new FragmentVisibleIdlingResource(testActivity, first.getStableTag(), true);
        Espresso.registerIdlingResources(firstVisible);

        // assert that the second fragment is queued to be shown because the tags are different
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                assertTrue(testActivity.showFragment(second));
            }
        });
    }

    @Test
    public void testPopBackStack() {
        final TestFragment first = TestFragment.newInstance(TAG_A);
        final TestFragment second = TestFragment.newInstance(TAG_B);

        // assert that both fragments were queued to be shown
        assertTrue(testActivity.showFragment(first));
        assertTrue(testActivity.showFragment(second));

        // Wait till second fragment shows
        final FragmentVisibleIdlingResource secondVisible = new FragmentVisibleIdlingResource(testActivity, second.getStableTag(), true);
        Espresso.registerIdlingResources(secondVisible);

        // Press back to pop fragment with TAG_B off
        Espresso.pressBack();

        // Register an idling resource for the first fragment to be visible again
        final FragmentVisibleIdlingResource firstVisible = new FragmentVisibleIdlingResource(testActivity, first.getStableTag(), true);
        Espresso.registerIdlingResources(firstVisible);

        // Make Espresso idle till the second fragment is gone and the first is visible
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
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
        });
    }

}