package com.tunjid.androidbootstrap.core.abstractclasses;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tunjid.androidbootstrap.core.testclasses.TestActivity;
import com.tunjid.androidbootstrap.core.testclasses.TestFragment;
import com.tunjid.androidbootstrap.test.TestUtils;
import com.tunjid.androidbootstrap.test.idlingresources.FragmentVisibleIdlingResource;
import com.tunjid.androidbootstrap.test.idlingresources.IdleCallBack;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
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

        TestUtils.unregisterAllIdlingResources();
    }

    @Test
    public void testShowFragment() {
        final TestFragment fragmentA = TestFragment.newInstance(TAG_A);
        final TestFragment fragmentB = TestFragment.newInstance(TAG_B);

        assertTrue(testActivity.showFragment(fragmentA));

        FragmentVisibleIdlingResource resourceA = new FragmentVisibleIdlingResource(testActivity, fragmentA.getStableTag(), true);
        Espresso.registerIdlingResources(resourceA);

        // Wait till fargmentA is added to the activity before calling showFragment
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {

                assertTrue(fragmentA.showFragment(fragmentB));

                FragmentVisibleIdlingResource resourceB = new FragmentVisibleIdlingResource(testActivity, fragmentA.getStableTag(), true);
                resourceB.setIdleCallBack(new IdleCallBack() {
                    @Override
                    public void onIdle() {

                        // Wait for fragmentB to be shown before checking
                        // if it's in the fragmentManager
                        getInstrumentation().runOnMainSync(new Runnable() {
                            @Override
                            public void run() {
                                assertEquals(fragmentB, testActivity.getSupportFragmentManager().findFragmentByTag(fragmentB.getStableTag()));
                            }
                        });
                    }
                });

                Espresso.registerIdlingResources(resourceB);
            }
        });
    }
}