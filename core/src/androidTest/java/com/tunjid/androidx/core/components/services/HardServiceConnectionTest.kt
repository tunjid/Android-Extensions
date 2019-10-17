package com.tunjid.androidx.core.components.services


import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ServiceTestRule
import com.tunjid.androidx.core.testclasses.TestService
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests [HardServiceConnection]
 *
 *
 * Created by tj.dahunsi on 4/30/17.
 */
@RunWith(AndroidJUnit4::class)
class HardServiceConnectionTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    private lateinit var connection: HardServiceConnection<TestService>
    private lateinit var context: Context

    private fun intent() = Intent(ApplicationProvider.getApplicationContext<Application>(), TestService::class.java)

    @Before
    @Throws(Exception::class)
    fun setUp() {
        connection = HardServiceConnection(TestService::class.java)
        context = getInstrumentation().context
        assertFalse(HardServiceConnection.isServiceRunning(TestService::class.java, context))
    }

    @After
    fun tearDown() {
        connection.boundService?.stopSelf()
        connection.unbindService()
    }

    @Test
    fun testBind() {
        bind()
        assertNotNull(connection.boundService)
    }

    @Test
    fun testBindWithCallback() {
        var bound: TestService? = null

        connection = HardServiceConnection(TestService::class.java) {
            assertNotNull(it)
            bound = it
        }

        bind()

        assertNotNull(connection.boundService)
        assertSame(connection.boundService, bound)
    }


    private fun bind() {
        val binder: IBinder = serviceRule.bindService(intent(), connection, Context.BIND_AUTO_CREATE)
        require(binder is SelfBinder<*>) { "Bound Service is not a SelfBinder" }
    }
}
