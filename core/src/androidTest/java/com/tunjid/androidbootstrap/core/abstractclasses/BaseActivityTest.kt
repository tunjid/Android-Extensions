package com.tunjid.androidbootstrap.core.abstractclasses

import android.app.Activity
import androidx.fragment.app.FragmentManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.tunjid.androidbootstrap.core.testclasses.TestActivity
import com.tunjid.androidbootstrap.core.testclasses.TestFragment
import com.tunjid.androidbootstrap.test.TestUtils
import com.tunjid.androidbootstrap.test.idlingresources.FragmentVisibleIdlingResource
import com.tunjid.androidbootstrap.test.resources.TestIdler
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit


/**
 * Tests methods in [BaseActivity].
 */
@RunWith(AndroidJUnit4::class)
class BaseActivityTest {

    private var testActivity: TestActivity? = null
    private var testIdler: TestIdler? = null

    @Rule
    @JvmField
    var activityRule: ActivityTestRule<*> = ActivityTestRule(TestActivity::class.java)

    @Before
    fun setUp() {
        testActivity = activityRule.activity as TestActivity
        testIdler = TestIdler(5, TimeUnit.SECONDS)
    }

    @After
    fun tearDown() {
        testActivity?.manager?.popBackStack("", 0)
        testActivity?.finish()
        testActivity = null
        testIdler = null

        // Unregister all idling resources before new tests start
        TestUtils.unregisterAllIdlingResources()
    }

    @Test
    @Throws(Exception::class)
    fun testShowFragment() {
        val testActivity = testActivity
                ?: throw IllegalStateException("testActivity not initialized")
        val testIdler = testIdler ?: throw IllegalStateException("TestIdler not initialized")

        val testFragment = TestFragment.newInstance("A")

        assertTrue(testActivity.showFragment(testFragment))

        // Wait till the fragment is attached to the activity
        val resource = FragmentVisibleIdlingResource(testActivity, testFragment.stableTag, true)
        testIdler.till(resource)

        assertEquals(testFragment, testActivity.manager.findFragmentByTag(testFragment.stableTag))
    }

    @Test
    @Throws(Exception::class)
    fun testGetCurrentFragment() {
        val testActivity = testActivity
                ?: throw IllegalStateException("testActivity not initialized")
        val testIdler = testIdler ?: throw IllegalStateException("TestIdler not initialized")

        val testFragment = TestFragment.newInstance("B")

        assertTrue(testActivity.showFragment(testFragment))

        // Wait till the fragment is attached to the activity
        val resource = FragmentVisibleIdlingResource(testActivity, testFragment.stableTag, true)
        testIdler.till(resource)

        assertEquals(testFragment, testActivity.currentFragment)
    }

    @Test
    @Throws(Exception::class)
    fun testSingleTagInstance() {
        val testActivity = testActivity
                ?: throw IllegalStateException("testActivity not initialized")
        val testIdler = testIdler ?: throw IllegalStateException("TestIdler not initialized")

        val first = TestFragment.newInstance("C")
        val second = TestFragment.newInstance("C")

        assertTrue(testActivity.showFragment(first))

        val firstVisible = FragmentVisibleIdlingResource(testActivity, first.stableTag, true)
        testIdler.till(firstVisible)

        // assert that the second fragment is NOT queued to be shown because they use the same tag
        assertFalse(testActivity.showFragment(second))
    }

    @Test
    @Throws(Exception::class)
    fun testMultipleTagInstance() {
        val testActivity = testActivity
                ?: throw IllegalStateException("testActivity not initialized")
        val testIdler = testIdler ?: throw IllegalStateException("TestIdler not initialized")

        val first = TestFragment.newInstance("D")
        val second = TestFragment.newInstance("E")

        // assert that the first fragment is queued to be shown
        assertTrue(testActivity.showFragment(first))

        val firstVisible = FragmentVisibleIdlingResource(testActivity, first.stableTag, true)
        testIdler.till(firstVisible)

        // assert that the second fragment is queued to be shown because the tags are different
        assertTrue(testActivity.showFragment(second))
    }

    @Test
    @Throws(Exception::class)
    fun testPopBackStack() {
        val testActivity = testActivity
                ?: throw IllegalStateException("testActivity not initialized")
        val testIdler = testIdler ?: throw IllegalStateException("TestIdler not initialized")

        val first = TestFragment.newInstance("F")
        val second = TestFragment.newInstance("G")

        assertTrue(testActivity.showFragment(first))

        // Wait for the first fragment to be visible
        testIdler.till(FragmentVisibleIdlingResource(testActivity, first.stableTag, true))

        assertTrue(testActivity.showFragment(second))

        // Wait till second fragment shows
        testIdler.till(FragmentVisibleIdlingResource(testActivity, second.stableTag, true))

        // Pop second fragment off
        testActivity.root.onBackPressed()

        // Wait till the first fragment is visible again
        testIdler.till(FragmentVisibleIdlingResource(testActivity, first.stableTag, true))

        // Assert fragment with tag TAG_B is gone
        assertNull(testActivity.manager.findFragmentByTag(second.stableTag))

        // Assert fragment with tag TAG_A is still available
        assertNotNull(testActivity.manager.findFragmentByTag(first.stableTag))

        // Assert first and the fragment retrieved with the tag are equal and the same
        assertSame(first, testActivity.manager.findFragmentByTag(first.stableTag))

        // Assert that the first was indeed retrieved from the backstack
        val firstFoundByTag = testActivity.manager.findFragmentByTag(first.stableTag) as TestFragment?
        assertTrue(firstFoundByTag?.restoredFromBackStack()!!)
        assertTrue(first.restoredFromBackStack())
    }

    private val TestActivity.manager: FragmentManager
        get() = supportFragmentManager

    private val TestActivity.root: Activity
        get() = this
}