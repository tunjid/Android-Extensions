package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.tunjid.androidx.savedstate.savedStateFor
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val TAG_A = "A"
private const val TAG_B = "B"
private const val TAG_C = "C"
private const val TAG_D = "D"

val TAGS = listOf("0", "1", "2")

class MultiStackNavigatorTest {

    private lateinit var activity: NavigationTestActivity
    private lateinit var multiStackNavigator: MultiStackNavigator

    @Rule
    @JvmField
    var activityRule: ActivityTestRule<*> = ActivityTestRule(NavigationTestActivity::class.java)

    @Before
    fun setUp() {
        activity = activityRule.activity as NavigationTestActivity
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            multiStackNavigator = MultiStackNavigator(
                    3,
                    savedStateFor(activity, "test"),
                    activity.supportFragmentManager,
                    activity.containerId
            ) { NavigationTestFragment.newInstance(TAGS[it]).run { this to stableTag } }
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        assertEquals(TAGS, multiStackNavigator.stackFragments.map(Fragment::getTag))
    }

    @After
    fun tearDown() {
        activity.finish()
    }

    @Test
    fun testVisitation() {
        multiStackNavigator.waitForIdleSyncAfter { show(0) }
        multiStackNavigator.waitForIdleSyncAfter { show(2) }
        multiStackNavigator.waitForIdleSyncAfter { show(1) }

        assertEquals(listOf(0, 2, 1), multiStackNavigator.visitStack.toList())

        multiStackNavigator.waitForIdleSyncAfter { show(2) }

        assertEquals(listOf(0, 1, 2), multiStackNavigator.visitStack.toList())

        multiStackNavigator.waitForIdleSyncAfter { pop() }

        assertEquals(listOf(0, 1), multiStackNavigator.visitStack.toList())

        multiStackNavigator.waitForIdleSyncAfter { pop() }

        assertEquals(listOf(0), multiStackNavigator.visitStack.toList())
    }

    @Test
    fun testIndependentStacks() {
        val testFragmentA = NavigationTestFragment.newInstance(TAG_A)
        val testFragmentB = NavigationTestFragment.newInstance(TAG_B)
        val testFragmentC = NavigationTestFragment.newInstance(TAG_C)
        val testFragmentD = NavigationTestFragment.newInstance(TAG_D)

        multiStackNavigator.waitForIdleSyncAfter { show(0) }
        multiStackNavigator.waitForIdleSyncAfter { push(testFragmentA) }
        multiStackNavigator.waitForIdleSyncAfter { push(testFragmentB) }

        multiStackNavigator.waitForIdleSyncAfter { show(2) }
        multiStackNavigator.waitForIdleSyncAfter { push(testFragmentC) }
        multiStackNavigator.waitForIdleSyncAfter { push(testFragmentD) }

        assertSame(testFragmentD, multiStackNavigator.current)

        multiStackNavigator.waitForIdleSyncAfter { show(0) }

        assertSame(testFragmentB, multiStackNavigator.current)

        multiStackNavigator.waitForIdleSyncAfter { pop() }

        assertSame(testFragmentA, multiStackNavigator.current)

        multiStackNavigator.waitForIdleSyncAfter { show(2) }

        assertSame(testFragmentD, multiStackNavigator.current)

        multiStackNavigator.waitForIdleSyncAfter { show(1) }

        assertEquals("1", multiStackNavigator.current?.tag)
    }

    @Test
    fun testPeekAcrossStacks() {
        val testFragmentA = NavigationTestFragment.newInstance(TAG_A)
        val testFragmentB = NavigationTestFragment.newInstance(TAG_B)

        multiStackNavigator.waitForIdleSyncAfter { show(0) }
        multiStackNavigator.waitForIdleSyncAfter { push(testFragmentA) }
        multiStackNavigator.waitForIdleSyncAfter { push(testFragmentB) }

        assertSame(testFragmentA, multiStackNavigator.previous)

        multiStackNavigator.waitForIdleSyncAfter { show(1) }

        assertSame(testFragmentB, multiStackNavigator.previous)
    }

    @Test
    fun testClear() {
        val initial =  multiStackNavigator.current!!
        val initialTag =  initial.tag

        InstrumentationRegistry.getInstrumentation().runOnMainSync { multiStackNavigator.clearAll() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        val current =  multiStackNavigator.current!!
        val currentTag =  current.tag

        assertNotNull(initialTag)
        assertNotNull(currentTag)
        assertNull(initial.activity)
        assertNotNull(current.activity)
        assertNotSame(initial, current)
        assertEquals(initialTag, currentTag)
    }
}