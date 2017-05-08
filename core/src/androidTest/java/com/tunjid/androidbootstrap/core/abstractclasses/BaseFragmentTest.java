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
import static junit.framework.Assert.assertTrue;

/**
 * Tests {@link BaseFragment}
 * <p>
 * Created by tj.dahunsi on 4/29/17.
 */
@RunWith(AndroidJUnit4.class)
public class BaseFragmentTest {

    private static final String TAG_A = "A";
    private static final String TAG_B = "B";

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
        testActivity.finish();
        testActivity = null;

        TestUtils.unregisterAllIdlingResources();
    }

    @Test
    public void testShowFragment() throws Exception {
        final TestFragment fragmentA = TestFragment.newInstance(TAG_A);
        final TestFragment fragmentB = TestFragment.newInstance(TAG_B);

        assertTrue(testActivity.showFragment(fragmentA));

        FragmentVisibleIdlingResource resourceA = new FragmentVisibleIdlingResource(testActivity, fragmentA.getStableTag(), true);
        testIdler.till(resourceA);

        // Wait till fargmentA is added to the activity before calling showFragment

        assertTrue(fragmentA.showFragment(fragmentB));

        FragmentVisibleIdlingResource resourceB = new FragmentVisibleIdlingResource(testActivity, fragmentA.getStableTag(), true);
        testIdler.till(resourceB);

        // Wait for fragmentB to be shown before checking
        // if it's in the fragmentManager
        assertEquals(fragmentB, testActivity.getSupportFragmentManager().findFragmentByTag(fragmentB.getStableTag()));
    }
}