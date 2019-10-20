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

    private lateinit var context: Context
    private lateinit var connection: HardServiceConnection<TestService>

    private fun intent() = Intent(ApplicationProvider.getApplicationContext<Application>(), TestService::class.java)

    @Before
    @Throws(Exception::class)
    fun setUp() {
        context = getInstrumentation().context
        connection = HardServiceConnection(context, TestService::class.java)
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

        connection = HardServiceConnection(context, TestService::class.java) {
            assertNotNull(it)
            bound = it
        }

        bind()

        assertNotNull(connection.boundService)
        assertSame(connection.boundService, bound)
    }

    @Test
    fun testUnbind() {
        bind()
        assertNotNull(connection.boundService)
    }


    private fun bind() {
        connection.bind()
        val binder: IBinder = serviceRule.bindService(intent(), connection, Context.BIND_AUTO_CREATE)
        require(binder is SelfBinder<*>) { "Bound Service is not a SelfBinder" }
    }
}
