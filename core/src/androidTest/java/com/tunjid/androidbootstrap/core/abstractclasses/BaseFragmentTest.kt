package com.tunjid.androidbootstrap.core.abstractclasses

import androidx.fragment.app.FragmentManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.tunjid.androidbootstrap.core.testclasses.TestActivity
import com.tunjid.androidbootstrap.core.testclasses.TestFragment
import com.tunjid.androidbootstrap.test.TestUtils
import com.tunjid.androidbootstrap.test.idlingresources.FragmentVisibleIdlingResource
import com.tunjid.androidbootstrap.test.resources.TestIdler
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Tests [BaseFragment]
 *
 *
 * Created by tj.dahunsi on 4/29/17.
 */
@RunWith(AndroidJUnit4::class)
class BaseFragmentTest {

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
        testActivity!!.finish()
        testActivity = null

        TestUtils.unregisterAllIdlingResources()
    }

    @Test
    @Throws(Exception::class)
    fun testShowFragment() {
        val fragmentA = TestFragment.newInstance(TAG_A)
        val fragmentB = TestFragment.newInstance(TAG_B)

        assertTrue(testActivity!!.showFragment(fragmentA))

        val resourceA = FragmentVisibleIdlingResource(testActivity, fragmentA.stableTag, true)
        testIdler!!.till(resourceA)

        // Wait till fragmentA is added to the activity before calling showFragment

        assertTrue(fragmentA.showFragment(fragmentB))

        val resourceB = FragmentVisibleIdlingResource(testActivity, fragmentA.stableTag, true)
        testIdler!!.till(resourceB)

        // Wait for fragmentB to be shown before checking
        // if it's in the fragmentManager
        val fragmentManager: FragmentManager = testActivity!!.supportFragmentManager
        assertEquals(fragmentB, fragmentManager.findFragmentByTag(fragmentB.stableTag))
    }

    companion object {

        private const val TAG_A = "A"
        private const val TAG_B = "B"
    }
}