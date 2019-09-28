package com.tunjid.androidx.navigation

import androidx.fragment.app.FragmentManager
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.tunjid.androidx.core.testclasses.TestActivity
import com.tunjid.androidx.core.testclasses.TestFragment
import com.tunjid.androidx.savedstate.savedStateFor
import com.tunjid.androidx.test.TestUtils
import com.tunjid.androidx.test.idlingresources.FragmentGoneIdlingResource
import com.tunjid.androidx.test.idlingresources.FragmentVisibleIdlingResource
import com.tunjid.androidx.test.resources.TestIdler
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Tests the [StackNavigator]
 *
 *
 * Created by tj.dahunsi on 4/29/17.
 */

@RunWith(AndroidJUnit4::class)
class StackNavigatorTest {

    private var activity: TestActivity? = null
    private var stackNavigator: StackNavigator? = null
    private var testIdler: TestIdler? = null

    @Rule
    @JvmField
    var activityRule: ActivityTestRule<*> = ActivityTestRule(TestActivity::class.java)

    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    @Before
    fun setUp() {
        testIdler = TestIdler(DEFAULT_TIME_OUT.toLong(), TimeUnit.SECONDS)
        activity = activityRule.activity as TestActivity
        stackNavigator = StackNavigator(savedStateFor(activity!!, "TEST"), activity!!.supportFragmentManager, activity!!.containerId)
    }

    @After
    fun tearDown() {
        activity?.finish()
        activity = null
        testIdler = null

        // Unregister all idling resources before new tests start
        TestUtils.unregisterAllIdlingResources()
    }

    @Test
    @Throws(Throwable::class)
    fun testFragmentTagsAdded() {
        val testIdler = testIdler ?: throw IllegalStateException("testIdler not initialized")
        val activity = activity ?: throw IllegalStateException("Activity not initialized")
        val fragmentStackNavigator = stackNavigator
                ?: throw IllegalStateException("stackNavigator not initialized")

        val fragmentManager: FragmentManager = activity.supportFragmentManager
        val testFragment = TestFragment.newInstance(TAG_A)

        fragmentManager.beginTransaction()
                .replace(activity.containerId, testFragment, TAG_A)
                .addToBackStack(TAG_A)
                .commit()

        val resource = FragmentVisibleIdlingResource(fragmentManager, TAG_A, true)
        testIdler.till(resource)

        assertTrue(fragmentStackNavigator.fragmentTags.contains(TAG_A))
        assertTrue(fragmentStackNavigator.fragmentTags.size == 1)
    }

    @Test
    @Throws(Throwable::class)
    fun testFragmentTagsRestored() {
        val testIdler = testIdler ?: throw IllegalStateException("testIdler not initialized")
        val activity = activity ?: throw IllegalStateException("Activity not initialized")
        val fragmentStackNavigator = stackNavigator
                ?: throw IllegalStateException("stackNavigator not initialized")

        val fragmentManager = fragmentStackNavigator.fragmentManager
        val testFragment = TestFragment.newInstance(TAG_A)

        fragmentManager.beginTransaction()
                .replace(activity.containerId, testFragment, TAG_A)
                .addToBackStack(TAG_A)
                .commit()

        val resource = FragmentVisibleIdlingResource(fragmentManager, TAG_A, true)
        testIdler.till(resource)

        // create new instance of fragentStateManager and confirm all
        // the old tags are restored
        val copy = StackNavigator(savedStateFor(activity, "OTHER"), activity.supportFragmentManager, activity.containerId)

        assertTrue(copy.fragmentTags.contains(TAG_A))
        assertTrue(copy.fragmentTags.size == 1)
    }

    @Test//(expected = IllegalStateException.class)
    @UiThreadTest
    fun testExceptionNotAddedToBackStack() {
        val activity = activity ?: throw IllegalStateException("Activity not initialized")

        val fragmentManager: FragmentManager = activity.supportFragmentManager
        val testFragment = TestFragment.newInstance(TAG_A)

        expectedException.expect(IllegalStateException::class.java)
        expectedException.expectMessage(StackNavigator.MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK)

        fragmentManager.beginTransaction()
                .replace(activity.containerId, testFragment, TAG_A)
                .commitNow()
    }

    @Test
    @Throws(Throwable::class)
    fun testAddAndRemove() {
        val testIdler = testIdler ?: throw IllegalStateException("testIdler not initialized")
        val activity = activity ?: throw IllegalStateException("Activity not initialized")

        val fragmentManager: FragmentManager = activity.supportFragmentManager
        val testFragmentA = TestFragment.newInstance(TAG_A)
        val testFragmentB = TestFragment.newInstance(TAG_B)

        fragmentManager.beginTransaction()
                .replace(activity.containerId, testFragmentA, testFragmentA.stableTag)
                .addToBackStack(testFragmentA.stableTag)
                .commit()

        testIdler.till(FragmentVisibleIdlingResource(activity, testFragmentA.stableTag, true))

        fragmentManager.beginTransaction()
                .replace(activity.containerId, testFragmentB, testFragmentB.stableTag)
                .addToBackStack(testFragmentB.stableTag)
                .commit()

        testIdler.till(FragmentVisibleIdlingResource(activity, testFragmentB.stableTag, true))

        assertEquals(fragmentManager.backStackEntryCount, 2)

        fragmentManager.popBackStack()

        testIdler.till(FragmentGoneIdlingResource(activity, testFragmentB.stableTag, true))

        assertEquals(fragmentManager.backStackEntryCount, 1)

        fragmentManager.popBackStack()

        testIdler.till(FragmentGoneIdlingResource(activity, testFragmentA.stableTag, true))

        assertEquals(fragmentManager.backStackEntryCount, 0)
    }

    @Test
    @Throws(Throwable::class)
    fun testIgnoredId() {
        val testIdler = testIdler ?: throw IllegalStateException("testIdler not initialized")
        val activity = activity ?: throw IllegalStateException("Activity not initialized")
        val fragmentStackNavigator = stackNavigator
                ?: throw IllegalStateException("stackNavigator not initialized")

        val fragmentManager: FragmentManager = activity.supportFragmentManager
        val testFragmentA = TestFragment.newInstance(TAG_A)
        val testFragmentB = TestFragment.newInstance(TAG_B)

        fragmentManager.beginTransaction()
                .replace(activity.containerId, testFragmentA, testFragmentA.stableTag)
                .addToBackStack(testFragmentA.stableTag)
                .commit()

        testIdler.till(FragmentVisibleIdlingResource(activity, testFragmentA.stableTag, true))

        assertEquals(fragmentManager.backStackEntryCount, 1)
        assertEquals(fragmentManager.backStackEntryCount, fragmentStackNavigator.fragmentTags.size)

        fragmentManager.beginTransaction()
                .replace(activity.ignoredLayoutId, testFragmentB, testFragmentB.stableTag)
                .addToBackStack(testFragmentB.stableTag)
                .commit()

        testIdler.till(FragmentVisibleIdlingResource(activity, testFragmentB.stableTag, true))

        assertEquals(fragmentManager.backStackEntryCount, 2)
        assertFalse(fragmentManager.backStackEntryCount == fragmentStackNavigator.fragmentTags.size)
    }

    companion object {

        private const val DEFAULT_TIME_OUT = 5

        private const val TAG_A = "A"
        private const val TAG_B = "B"
    }
}