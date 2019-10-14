package com.tunjid.androidx.navigation

import androidx.fragment.app.FragmentManager
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith


/**
 * Tests the [StackNavigator]
 *
 *
 * Created by tj.dahunsi on 4/29/17.
 */

private const val TAG_A = "A"
private const val TAG_B = "B"
private const val TAG_C = "C"
private const val TAG_D = "D"
private const val TAG_E = "E"

@RunWith(AndroidJUnit4::class)
class StackNavigatorTest {

    private lateinit var activity: NavigationTestActivity
    private lateinit var stackNavigator: StackNavigator

    @Rule
    @JvmField
    var activityRule: ActivityTestRule<*> = ActivityTestRule(NavigationTestActivity::class.java)

    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    @Before
    fun setUp() {
        activity = activityRule.activity as NavigationTestActivity
        stackNavigator = StackNavigator(activity.supportFragmentManager, activity.containerId)
    }

    @After
    fun tearDown() {
        activity.finish()
    }

    @Test
    @Throws(Throwable::class)
    fun testFragmentTagsAdded() {
        val testFragment = NavigationTestFragment.newInstance(TAG_A)

       assertTrue( stackNavigator.push(testFragment))
        getInstrumentation().waitForIdleSync()

        assertTrue(stackNavigator.fragmentTags.contains(testFragment.stableTag))
        assertTrue(stackNavigator.fragmentTags.size == 1)
    }

    @Test
    @Throws(Throwable::class)
    fun testFragmentTagsRestored() {
        val testFragment = NavigationTestFragment.newInstance(TAG_A)

       assertTrue( stackNavigator.push(testFragment))
        getInstrumentation().waitForIdleSync()

        // create new instance of fragentStateManager and confirm all
        // the old tags are restored
        val copy = StackNavigator(activity.supportFragmentManager, activity.containerId)

        assertTrue(copy.fragmentTags.contains(testFragment.stableTag))
        assertTrue(copy.fragmentTags.size == 1)
    }

    @Test//(expected = IllegalStateException.class)
    @UiThreadTest
    fun testExceptionNotAddedToBackStack() {
        val fragmentManager: FragmentManager = activity.supportFragmentManager
        val testFragment = NavigationTestFragment.newInstance(TAG_A)

        expectedException.expect(IllegalStateException::class.java)
        expectedException.expectMessage(StackNavigator.MSG_FRAGMENT_NOT_ADDED_TO_BACKSTACK)

        fragmentManager.beginTransaction()
                .replace(activity.containerId, testFragment, TAG_A)
                .commitNow()
    }

    @Test//(expected = IllegalStateException.class)
    fun testIndependentContainer() {
        val fragmentManager: FragmentManager = activity.supportFragmentManager
        val testFragmentA = NavigationTestFragment.newInstance(TAG_A)
        val testFragmentB = NavigationTestFragment.newInstance(TAG_B)

        assertTrue(stackNavigator.push(testFragmentA))
        getInstrumentation().waitForIdleSync()
        fragmentManager.beginTransaction()
                .replace(activity.ignoredLayoutId, testFragmentB, testFragmentB.stableTag)
                .commit()
        getInstrumentation().waitForIdleSync()

        assertEquals(1, stackNavigator.fragmentTags.size)
        assertEquals(listOf(TAG_A), stackNavigator.fragmentTags)
    }
    
    @Test
    @Throws(Throwable::class)
    fun testAddAndRemove() {
        val testFragmentA = NavigationTestFragment.newInstance(TAG_A)
        val testFragmentB = NavigationTestFragment.newInstance(TAG_B)

        assertTrue(stackNavigator.push(testFragmentA))
        getInstrumentation().waitForIdleSync()

        assertTrue(stackNavigator.push(testFragmentB))
        getInstrumentation().waitForIdleSync()

        assertEquals(2, stackNavigator.fragmentTags.size)
        assertSame(testFragmentA, stackNavigator.peek())

        stackNavigator.pop()
        getInstrumentation().waitForIdleSync()

        assertEquals(1, stackNavigator.fragmentTags.size)
        assertFalse(stackNavigator.pop())
    }

    @Test
    @Throws(Throwable::class)
    fun testNoDoublePushSameTag() {
        val testFragmentA = NavigationTestFragment.newInstance(TAG_A)
        val testFragmentDuplicateA = NavigationTestFragment.newInstance(TAG_A)

        assertTrue(stackNavigator.push(testFragmentA))

        getInstrumentation().waitForIdleSync()

        assertEquals(1, stackNavigator.fragmentTags.size)

        assertFalse(stackNavigator.push(testFragmentDuplicateA))

        getInstrumentation().waitForIdleSync()

        assertEquals(1, stackNavigator.fragmentTags.size)
    }

    @Test
    @Throws(Throwable::class)
    fun testClear() {
        val testFragmentA = NavigationTestFragment.newInstance(TAG_A)
        val testFragmentB = NavigationTestFragment.newInstance(TAG_B)
        val testFragmentC = NavigationTestFragment.newInstance(TAG_C)
        val testFragmentD = NavigationTestFragment.newInstance(TAG_D)
        val testFragmentE = NavigationTestFragment.newInstance(TAG_E)

        assertTrue(stackNavigator.push(testFragmentA))
        getInstrumentation().waitForIdleSync()

        assertEquals(1, stackNavigator.fragmentTags.size)

        assertTrue(stackNavigator.push(testFragmentB))
        getInstrumentation().waitForIdleSync()

        assertEquals(2, stackNavigator.fragmentTags.size)

        stackNavigator.clear(includeMatch = true)
        getInstrumentation().waitForIdleSync()

        assertEquals(0, stackNavigator.fragmentTags.size)

        assertTrue(stackNavigator.push(testFragmentC))
        getInstrumentation().waitForIdleSync()
        assertTrue(stackNavigator.push(testFragmentD))
        getInstrumentation().waitForIdleSync()
        assertTrue(stackNavigator.push(testFragmentE))
        getInstrumentation().waitForIdleSync()

        assertEquals(3, stackNavigator.fragmentTags.size)
        assertEquals(listOf(TAG_C, TAG_D, TAG_E), stackNavigator.fragmentTags)

        stackNavigator.clear(TAG_C)
        getInstrumentation().waitForIdleSync()

        assertEquals(1, stackNavigator.fragmentTags.size)
        assertEquals(listOf(TAG_C), stackNavigator.fragmentTags)
    }

}