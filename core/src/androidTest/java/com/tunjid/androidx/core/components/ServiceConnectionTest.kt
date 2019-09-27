package com.tunjid.androidx.core.components


import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.tunjid.androidx.core.testclasses.TestService
import com.tunjid.androidx.test.resources.TestIdler
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import java.util.concurrent.TimeUnit

/**
 * Tests [ServiceConnection]
 *
 *
 * Created by tj.dahunsi on 4/30/17.
 */
@RunWith(AndroidJUnit4::class)
class ServiceConnectionTest {

    private var testIdler: TestIdler? = null
    private var bindCondition: TestIdler.TestCondition? = null
    private var connection: ServiceConnection<TestService>? = null
    private var context: Context? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        connection = spy(ServiceConnection(TestService::class.java))
        context = getInstrumentation().context
        assertFalse(ServiceConnection.isServiceRunning(TestService::class.java, context!!))

        testIdler = TestIdler(DEFAULT_TIME_OUT.toLong(), TimeUnit.SECONDS)
        bindCondition = TestIdler.TestCondition { connection!!.isBound }
    }

    @After
    fun tearDown() {
        if (connection?.isBound == true) connection?.boundService?.stopSelf()
        connection?.unbindService()
        connection = null
    }

    @Test
    @Throws(Exception::class)
    fun testBind() {
        connection!!.with(context!!).bind()
        testIdler!!.till(bindCondition!!)

        verify<ServiceConnection<TestService>>(connection).onServiceConnected(any(ComponentName::class.java), any(ServiceConnection.Binder::class.java))
        assertNotNull(connection!!.boundService)
    }

    @Test
    @Throws(Exception::class)
    fun testBindWithExtras() {
        val data = "data"
        val extras = Bundle()
        extras.putString(TEST_KEY, data)

        connection!!.with(context!!).setExtras(extras).bind()
        testIdler!!.till(bindCondition!!)

        verify<ServiceConnection<TestService>>(connection).onServiceConnected(any(ComponentName::class.java), any(ServiceConnection.Binder::class.java))
        assertNotNull(connection!!.boundService)
        assertEquals(data, connection!!.boundService!!.boundIntent.getStringExtra(TEST_KEY))
    }

    @Test
    @Throws(Exception::class)
    fun testBindWithCallback() {
        connection = spy(ServiceConnection(TestService::class.java, object : ServiceConnection.BindCallback<TestService> {
            override fun onServiceBound(service: TestService?) {
                assertNotNull(service)
            }
        }))

        connection!!.with(context!!).bind()
        testIdler!!.till(bindCondition!!)

        assertNotNull(connection!!.boundService)
    }

    @Test
    @Throws(Exception::class)
    fun testUnbind() {
        connection!!.with(context!!).bind()
        testIdler!!.till(bindCondition!!)

        verify<ServiceConnection<TestService>>(connection).onServiceConnected(any(ComponentName::class.java), any(ServiceConnection.Binder::class.java))
        assertNotNull(connection!!.boundService)
        assertTrue(connection!!.unbindService())
    }

    companion object {

        private const val DEFAULT_TIME_OUT = 5
        private const val TEST_KEY = "test key"
    }
}

private fun <T> any(stub: Class<T>): T {
    Mockito.any<T>(stub)
    return uninitialized()
}

private fun <T> uninitialized(): T = null as T