package com.tunjid.androidx.navigation

import androidx.fragment.app.Fragment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.tunjid.androidx.savedstate.savedStateFor
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val TAG_A = "A"
private const val TAG_B = "B"
private const val TAG_C = "C"
private const val TAG_D = "D"

private const val ROOT_TAG_0 = "root 0"
private const val ROOT_TAG_1 = "root 1"
private const val ROOT_TAG_2 = "root 2"

val TAGS = listOf(ROOT_TAG_0, ROOT_TAG_1, ROOT_TAG_2)

class MultiStackNavigatorTest {

    private lateinit var activity: NavigationTestActivity

    @Rule
    @JvmField
    var activityRule: ActivityTestRule<*> = ActivityTestRule(NavigationTestActivity::class.java)

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before
    fun setUp() {
        activity = activityRule.activity as NavigationTestActivity

        instrumentation.waitForIdleSync()
    }

    @After
    fun tearDown() {
        activity.finish()
    }

    @Test
    fun testRootFragmentCreation() {
        val multiStackNavigator = multiStackNavigator(MultiStackNavigator.BackStackType.UniqueEntries)
        assertEquals(listOf(0, 1, 2).map(Int::toString), multiStackNavigator.stackFragments.map(Fragment::getTag))
    }

    @Test
    fun testVisitation() {
        val multiStackNavigator = multiStackNavigator(MultiStackNavigator.BackStackType.UniqueEntries)

        multiStackNavigator.waitForIdleSyncAfter { show(0) }.also {
            multiStackNavigator.assertNavigatorIndices(ROOT_TAG_0, null, null)
        }

        multiStackNavigator.waitForIdleSyncAfter { show(2) }.also {
            multiStackNavigator.assertNavigatorIndices(ROOT_TAG_0, null, ROOT_TAG_2)
        }

        multiStackNavigator.waitForIdleSyncAfter { show(1) }.also {
            multiStackNavigator.assertNavigatorIndices(ROOT_TAG_0, ROOT_TAG_1, ROOT_TAG_2)
            assertEquals(listOf(0, 2, 1), multiStackNavigator.stackVisitor.hosts().toList())
        }

        multiStackNavigator.waitForIdleSyncAfter { show(2) }.also {
            assertEquals(listOf(0, 1, 2), multiStackNavigator.stackVisitor.hosts().toList())
        }

        multiStackNavigator.waitForIdleSyncAfter { pop() }.also {
            assertEquals(listOf(0, 1), multiStackNavigator.stackVisitor.hosts().toList())
        }

        multiStackNavigator.waitForIdleSyncAfter { pop() }.also {
            assertEquals(listOf(0), multiStackNavigator.stackVisitor.hosts().toList())
        }
    }

    @Test
    fun testIndependentStacks() {
        val multiStackNavigator = multiStackNavigator(MultiStackNavigator.BackStackType.UniqueEntries)

        val testFragmentA = NavigationTestFragment.newInstance(TAG_A)
        val testFragmentB = NavigationTestFragment.newInstance(TAG_B)
        val testFragmentC = NavigationTestFragment.newInstance(TAG_C)
        val testFragmentD = NavigationTestFragment.newInstance(TAG_D)

        multiStackNavigator.waitForIdleSyncAfter { show(0) }.also {
            multiStackNavigator.assertNavigatorIndices(ROOT_TAG_0, null, null)
        }

        multiStackNavigator.waitForIdleSyncAfter { push(testFragmentA) }.also {
            multiStackNavigator.assertNavigatorIndices(TAG_A, null, null)
            assertSame(testFragmentA, multiStackNavigator.find(TAG_A))
        }

        multiStackNavigator.waitForIdleSyncAfter { push(testFragmentB) }.also {
            multiStackNavigator.assertNavigatorIndices(TAG_B, null, null)
            assertSame(testFragmentB, multiStackNavigator.find(TAG_B))
        }

        multiStackNavigator.waitForIdleSyncAfter { show(2) }.also {
            multiStackNavigator.assertNavigatorIndices(TAG_B, null, ROOT_TAG_2)
        }

        multiStackNavigator.waitForIdleSyncAfter { push(testFragmentC) }.also {
            multiStackNavigator.assertNavigatorIndices(TAG_B, null, TAG_C)
            assertSame(testFragmentC, multiStackNavigator.find(TAG_C))
        }

        multiStackNavigator.waitForIdleSyncAfter { push(testFragmentD) }.also {
            multiStackNavigator.assertNavigatorIndices(TAG_B, null, TAG_D)
            assertSame(testFragmentD, multiStackNavigator.find(TAG_D))
        }

        assertSame(testFragmentD, multiStackNavigator.current)

        multiStackNavigator.waitForIdleSyncAfter { show(0) }.also {
            multiStackNavigator.assertNavigatorIndices(TAG_B, null, TAG_D)
            assertSame(testFragmentB, multiStackNavigator.current)
        }

        multiStackNavigator.waitForIdleSyncAfter { pop() }.also {
            multiStackNavigator.assertNavigatorIndices(TAG_A, null, TAG_D)
            assertSame(testFragmentA, multiStackNavigator.current)
        }

        multiStackNavigator.waitForIdleSyncAfter { show(2) }.also {
            assertSame(testFragmentD, multiStackNavigator.current)
        }

        multiStackNavigator.waitForIdleSyncAfter { show(1) }.also {
            multiStackNavigator.assertNavigatorIndices(TAG_A, ROOT_TAG_1, TAG_D)
            assertEquals(ROOT_TAG_1, multiStackNavigator.current?.tag)
        }
    }

    @Test
    fun testPeekAcrossStacks() {
        val multiStackNavigator = multiStackNavigator(MultiStackNavigator.BackStackType.UniqueEntries)

        val testFragmentA = NavigationTestFragment.newInstance(TAG_A)
        val testFragmentB = NavigationTestFragment.newInstance(TAG_B)

        multiStackNavigator.waitForIdleSyncAfter { show(0) }.also {
            multiStackNavigator.assertNavigatorIndices(ROOT_TAG_0, null, null)
        }

        multiStackNavigator.waitForIdleSyncAfter { push(testFragmentA) }.also {
            multiStackNavigator.assertNavigatorIndices(TAG_A, null, null)
        }

        multiStackNavigator.waitForIdleSyncAfter { push(testFragmentB) }.also {
            assertSame(testFragmentA, multiStackNavigator.previous)
            multiStackNavigator.assertNavigatorIndices(TAG_B, null, null)
        }

        multiStackNavigator.waitForIdleSyncAfter { show(1) }.also {
            assertSame(testFragmentB, multiStackNavigator.previous)
            multiStackNavigator.assertNavigatorIndices(TAG_B, ROOT_TAG_1, null)
        }
    }

    @Test
    fun testClear() {
        val multiStackNavigator = multiStackNavigator(MultiStackNavigator.BackStackType.UniqueEntries)

        val initial = multiStackNavigator.current!!
        val initialTag = initial.tag

        InstrumentationRegistry.getInstrumentation().runOnMainSync { multiStackNavigator.clearAll() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        multiStackNavigator.assertNavigatorIndices(ROOT_TAG_0, null, null)

        val current = multiStackNavigator.current!!
        val currentTag = current.tag

        assertNotNull(initialTag)
        assertNotNull(currentTag)
        assertNull(initial.activity)
        assertNotNull(current.activity)
        assertNotSame(initial, current)
        assertEquals(initialTag, currentTag)

        assertFalse(multiStackNavigator.stackFragments[0].isDetached)
        assertTrue(multiStackNavigator.stackFragments[1].isDetached)
        assertTrue(multiStackNavigator.stackFragments[2].isDetached)

        assertEquals(listOf(0), multiStackNavigator.stackVisitor.hosts().toList())
    }

    @Test
    fun testClearIndex() {
        val multiStackNavigator = multiStackNavigator(MultiStackNavigator.BackStackType.UniqueEntries)

        multiStackNavigator.waitForIdleSyncAfter { show(1) }.also {
            multiStackNavigator.assertNavigatorIndices(ROOT_TAG_0, ROOT_TAG_1, null)
        }

        InstrumentationRegistry.getInstrumentation().runOnMainSync { multiStackNavigator.clearAll() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        multiStackNavigator.assertNavigatorIndices(ROOT_TAG_0, null, null)
        assertFalse(multiStackNavigator.stackFragments[0].isDetached)
        assertTrue(multiStackNavigator.stackFragments[1].isDetached)
        assertTrue(multiStackNavigator.stackFragments[2].isDetached)

        assertEquals(listOf(0), multiStackNavigator.stackVisitor.hosts().toList())
    }

    @Test
    fun testBackStackTypeUnlimited() {
        val multiStackNavigator = multiStackNavigator(MultiStackNavigator.BackStackType.Unlimited)

        multiStackNavigator.waitForIdleSyncAfter { show(index = 1) }
        multiStackNavigator.waitForIdleSyncAfter { show(index = 2) }
        multiStackNavigator.waitForIdleSyncAfter { show(index = 1) }
        multiStackNavigator.waitForIdleSyncAfter { show(index = 2) }

        assertFalse(multiStackNavigator.stackFragments[2].isDetached)
        assertEquals(listOf(0, 1, 2, 1, 2), multiStackNavigator.stackVisitor.hosts().toList())
    }

    @Test
    fun testBackStackTypeRestricted() {
        val multiStackNavigator = multiStackNavigator(MultiStackNavigator.BackStackType.Restricted(count = 2))

        multiStackNavigator.waitForIdleSyncAfter { show(index = 1) }
        multiStackNavigator.waitForIdleSyncAfter { show(index = 2) }
        multiStackNavigator.waitForIdleSyncAfter { show(index = 1) }
        multiStackNavigator.waitForIdleSyncAfter { show(index = 2) }

        assertFalse(multiStackNavigator.stackFragments[2].isDetached)
        assertEquals(listOf(1, 2), multiStackNavigator.stackVisitor.hosts().toList())
    }

    @Test
    fun testBackStackTypeUnique() {
        val multiStackNavigator = multiStackNavigator(MultiStackNavigator.BackStackType.UniqueEntries)

        multiStackNavigator.waitForIdleSyncAfter { show(index = 1) }
        multiStackNavigator.waitForIdleSyncAfter { show(index = 2) }
        multiStackNavigator.waitForIdleSyncAfter { show(index = 1) }
        multiStackNavigator.waitForIdleSyncAfter { show(index = 2) }

        assertFalse(multiStackNavigator.stackFragments[2].isDetached)
        assertEquals(listOf(0, 1, 2), multiStackNavigator.stackVisitor.hosts().toList())
    }

    @Test
    fun testSequentialOperations() = runBlocking {
        val multiStackNavigator = multiStackNavigator(MultiStackNavigator.BackStackType.UniqueEntries)

        multiStackNavigator.performConsecutively(this) {
            val testFragmentA = NavigationTestFragment.newInstance(TAG_A)

            val root0 = show(0)
            assertEquals(ROOT_TAG_0, root0.tag)

            val pushedA = push(testFragmentA)
            assertSame(testFragmentA, pushedA)

            val root1 = show(1)
            assertEquals(ROOT_TAG_1, root1.tag)

            val popStack1FragA = pop()
            assertSame(testFragmentA, popStack1FragA)

            val root0Again = pop()
            assertEquals(ROOT_TAG_0, root0Again?.tag)

            val nullPop = pop()
            assertNull(nullPop)

            clearAll()
            val newRoot0 = show(0)

            assertSame(root0, root0Again)
            assertNotSame(root0, newRoot0)
            assertNotSame(root0Again, newRoot0)

            val pushes = listOf(TAG_B, TAG_C, TAG_D)
                .map(NavigationTestFragment::newInstance)
                .map { push(it) }

            assertSame(current, pushes[2])
            pop()
            assertSame(current, pushes[1])
            pop()
            assertSame(current, pushes[0])
            pop()
            assertSame(current, newRoot0)
            assertNull(previous)
        }
    }

    @Test
    fun testSequentialAggressiveClearAll() = runBlocking {
        val multiStackNavigator = multiStackNavigator(MultiStackNavigator.BackStackType.UniqueEntries)

        multiStackNavigator.performConsecutively(this) {
            push(NavigationTestFragment.newInstance(TAG_A))
            clearAll()

            push(NavigationTestFragment.newInstance(TAG_B))
            clearAll()

            push(NavigationTestFragment.newInstance(TAG_C))
            clearAll()

            push(NavigationTestFragment.newInstance(TAG_D))

            assertSame(TAG_D, current?.tag)
        }
    }

    private fun MultiStackNavigator.assertNavigatorIndices(vararg tags: String?) {
        tags.forEachIndexed { index, tag -> assertEquals(tag, navigatorAt(index)?.current?.tag) }
    }

    private fun multiStackNavigator(type: MultiStackNavigator.BackStackType): MultiStackNavigator {
        var navigator: MultiStackNavigator? = null
        instrumentation.runOnMainSync {
            navigator = MultiStackNavigator(
                stackCount = 3,
                stateContainer = savedStateFor(activity, "test"),
                fragmentManager = activity.supportFragmentManager,
                backStackType = type,
                containerId = activity.containerId
            ) { NavigationTestFragment.newInstance(TAGS[it]) }
        }

        instrumentation.waitForIdleSync()

        return navigator!!
    }
}