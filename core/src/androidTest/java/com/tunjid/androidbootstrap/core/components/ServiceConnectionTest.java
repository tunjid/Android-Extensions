package com.tunjid.androidbootstrap.core.components;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.test.runner.AndroidJUnit4;

import com.tunjid.androidbootstrap.core.testclasses.TestService;
import com.tunjid.androidbootstrap.test.resources.TestIdler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link ServiceConnection}
 * <p>
 * Created by tj.dahunsi on 4/30/17.
 */
@RunWith(AndroidJUnit4.class)
public class ServiceConnectionTest {

    private static final int DEFAULT_TIME_OUT = 5;
    private static final String TEST_KEY = "test key";

    private TestIdler testIdler;
    private TestIdler.TestCondition bindCondition;
    private ServiceConnection<TestService> connection;
    private Context context;

    @Before
    public void setUp() throws Exception {
        connection = spy(new ServiceConnection<>(TestService.class));
        context = getInstrumentation().getContext();
        assertFalse(ServiceConnection.isServiceRunning(TestService.class, context));

        testIdler = new TestIdler(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
        bindCondition = new TestIdler.TestCondition() {
            @Override
            public boolean satisfied() {
                return connection.isBound();
            }
        };
    }

    @After
    public void tearDown() {
        if (connection.isBound()) connection.getBoundService().stopSelf();
        connection.unbindService();
        connection = null;
    }

    @Test
    public void testBind() throws Exception {
        connection.with(context).bind();
        testIdler.till(bindCondition);

        verify(connection).onServiceConnected(any(ComponentName.class), any(ServiceConnection.Binder.class));
        assertNotNull(connection.getBoundService());
    }

    @Test
    public void testBindWithExtras() throws Exception {
        final String data = "data";
        Bundle extras = new Bundle();
        extras.putString(TEST_KEY, data);

        connection.with(context).setExtras(extras).bind();
        testIdler.till(bindCondition);

        verify(connection).onServiceConnected(any(ComponentName.class), any(ServiceConnection.Binder.class));
        assertNotNull(connection.getBoundService());
        assertEquals(data, connection.getBoundService().boundIntent.getStringExtra(TEST_KEY));
    }

    @Test
    public void testBindWithCallback() throws Exception {
        connection = spy(new ServiceConnection<>(TestService.class, new ServiceConnection.BindCallback<TestService>() {
            @Override
            public void onServiceBound(TestService service) {
                assertNotNull(service);
            }
        }));

        connection.with(context).bind();
        testIdler.till(bindCondition);

        assertNotNull(connection.getBoundService());
    }

    @Test
    public void testUnbind() throws Exception {
        connection.with(context).bind();
        testIdler.till(bindCondition);

        verify(connection).onServiceConnected(any(ComponentName.class), any(ServiceConnection.Binder.class));
        assertNotNull(connection.getBoundService());
        assertTrue(connection.unbindService());
    }
}