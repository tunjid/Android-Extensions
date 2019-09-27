package com.tunjid.androidx.core.testclasses;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.tunjid.androidx.core.components.ServiceConnection;

public class TestService extends Service {

    private final IBinder binder = new Binder();
    public Intent boundIntent;

    public TestService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        boundIntent = intent;
        return binder;
    }

    private class Binder extends ServiceConnection.Binder<TestService> {
        @Override
        public TestService getService() {
            return TestService.this;
        }
    }
}
