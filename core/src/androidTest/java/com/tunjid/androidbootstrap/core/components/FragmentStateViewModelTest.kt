package com.tunjid.androidbootstrap.core.components

import androidx.fragment.app.FragmentManager
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.tunjid.androidbootstrap.core.R
import com.tunjid.androidbootstrap.core.testclasses.TestActivity
import com.tunjid.androidbootstrap.core.testclasses.TestFragment
import com.tunjid.androidbootstrap.test.TestUtils
import com.tunjid.androidbootstrap.test.idlingresources.FragmentGoneIdlingResource
import com.tunjid.androidbootstrap.test.idlingresources.FragmentVisibleIdlingResource
import com.tunjid.androidbootstrap.test.resources.TestIdler
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Tests the [FragmentStateViewModel]
 *
 *
 * Created by tj.dahunsi on 4/29/17.
 */

@RunWith(AndroidJUnit4::class)
class FragmentStateViewModelTest {

    private var activity: TestActivity? = null
    private var fragmentStateViewModel: FragmentStateViewModel? = null
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
        fragmentStateViewModel = FragmentStateViewModel(activity!!.supportFragmentManager)
    }

    @After
    fun tearDown() {
        activity!!.finish()
        activity = null
        testIdler = null

        // Unregister all idling resources before new tests start
        TestUtils.unregisterAllIdlingResources()
    }

    @Test
    @Throws(Throwable::class)
    fun testFragmentTagsAdded() {
        val fragmentManager: FragmentManager = activity!!.supportFragmentManager
        val testFragment = TestFragment.newInstance(TAG_A)

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragment, TAG_A)
                .addToBackStack(TAG_A)
                .commit()

        val resource = FragmentVisibleIdlingResource(fragmentManager, TAG_A, true)
        testIdler!!.till(resource)

        assertTrue(fragmentStateViewModel!!.fragmentTags.contains(TAG_A))
        assertTrue(fragmentStateViewModel!!.fragmentTags.size == 1)
    }

    @Test
    @Throws(Throwable::class)
    fun testFragmentTagsRestored() {
        val fragmentManager = fragmentStateViewModel!!.fragmentManager
        val testFragment = TestFragment.newInstance(TAG_A)

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragment, TAG_A)
                .addToBackStack(TAG_A)
                .commit()

        val resource = FragmentVisibleIdlingResource(fragmentManager, TAG_A, true)
        testIdler!!.till(resource)

        // create new instance of fragentStateManager and confirm all
        // the old tags are restored
        fragmentStateViewModel = FragmentStateViewModel(activity!!.supportFragmentManager)

        assertTrue(fragmentStateViewModel!!.fragmentTags.contains(TAG_A))
        assertTrue(fragmentStateViewModel!!.fragmentTags.size == 1)
    }

    @Test//(expected = IllegalStateException.class)
    @UiThreadTest
    fun testExceptionNotAddedToBackStack() {
        val fragmentManager: FragmentManager = activity!!.supportFragmentManager
        val testFragment = TestFragment.newInstance(TAG_A)

        expectedException.expect(IllegalStateException::class.java)
        expectedException.expectMessage(FragmentStateViewModel.MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK)

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragment, TAG_A)
                .commitNow()
    }

    @Test
    @Throws(Throwable::class)
    fun testAddAndRemove() {
        val fragmentManager: FragmentManager = activity!!.supportFragmentManager
        val testFragmentA = TestFragment.newInstance(TAG_A)
        val testFragmentB = TestFragment.newInstance(TAG_B)

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragmentA, testFragmentA.stableTag)
                .addToBackStack(testFragmentA.stableTag)
                .commit()

        testIdler!!.till(FragmentVisibleIdlingResource(activity, testFragmentA.stableTag, true))

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragmentB, testFragmentB.stableTag)
                .addToBackStack(testFragmentB.stableTag)
                .commit()

        testIdler!!.till(FragmentVisibleIdlingResource(activity, testFragmentB.stableTag, true))

        assertEquals(fragmentManager.backStackEntryCount, 2)

        fragmentManager.popBackStack()

        testIdler!!.till(FragmentGoneIdlingResource(activity, testFragmentB.stableTag, true))

        assertEquals(fragmentManager.backStackEntryCount, 1)

        fragmentManager.popBackStack()

        testIdler!!.till(FragmentGoneIdlingResource(activity, testFragmentA.stableTag, true))

        assertEquals(fragmentManager.backStackEntryCount, 0)
    }

    @Test
    @Throws(Throwable::class)
    fun testIgnoredId() {
        val fragmentManager: FragmentManager = activity!!.supportFragmentManager
        val testFragmentA = TestFragment.newInstance(TAG_A)
        val testFragmentB = TestFragment.newInstance(TAG_B)

        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, testFragmentA, testFragmentA.stableTag)
                .addToBackStack(testFragmentA.stableTag)
                .commit()

        testIdler!!.till(FragmentVisibleIdlingResource(activity, testFragmentA.stableTag, true))

        assertEquals(fragmentManager.backStackEntryCount, 1)
        assertEquals(fragmentManager.backStackEntryCount, fragmentStateViewModel!!.fragmentTags.size)

        fragmentManager.beginTransaction()
                .replace(activity!!.ignoredLayoutId, testFragmentB, testFragmentB.stableTag)
                .addToBackStack(testFragmentB.stableTag)
                .commit()

        testIdler!!.till(FragmentVisibleIdlingResource(activity, testFragmentB.stableTag, true))

        assertEquals(fragmentManager.backStackEntryCount, 2)
        assertFalse(fragmentManager.backStackEntryCount == fragmentStateViewModel!!.fragmentTags.size)
    }

    companion object {

        private const val DEFAULT_TIME_OUT = 5

        private const val TAG_A = "A"
        private const val TAG_B = "B"
    }
}