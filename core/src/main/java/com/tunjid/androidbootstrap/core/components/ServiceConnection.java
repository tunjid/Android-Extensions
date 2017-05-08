package com.tunjid.androidbootstrap.core.components;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

/**
 * A class for binding a {@link Service}
 * <p>
 * Created by tj.dahunsi on 4/2/17.
 */

@SuppressWarnings("WeakerAccess")
public class ServiceConnection<T extends Service> implements android.content.ServiceConnection {

    private static final String TAG = "ServiceConnection";

    private Context bindingContext;
    private T boundService;
    private final Class<T> serviceClass;
    @Nullable
    private final BindCallback<T> bindCallback;

    public ServiceConnection(Class<T> serviceClass) {
        this(serviceClass, null);
    }

    public ServiceConnection(Class<T> serviceClass, @Nullable BindCallback<T> bindCallback) {
        this.serviceClass = serviceClass;
        this.bindCallback = bindCallback;
    }

    @Override
    @SuppressWarnings("unchecked")
    // Type safety quasi guaranteed, provided binding is done through API
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (!(service instanceof ServiceConnection.Binder)) {
            throw new IllegalArgumentException("Bound Service is not a Binder");
        }

        boundService = ((Binder<T>) service).getService();
        if (bindCallback != null) bindCallback.onServiceBound(boundService);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        boundService = null;
    }

    public boolean isBound() {
        return boundService != null;
    }

    public Binding<T> with(Context context) {
        bindingContext = context;
        return new Binding<>(context, this);
    }

    public T getBoundService() {
        return boundService;
    }

    public Class<T> getServiceClass() {
        return serviceClass;
    }

    public boolean unbindService() {
        if (bindingContext != null) {
            try {
                bindingContext.unbindService(this);
                bindingContext = null;
                return true;
            }
            catch (IllegalArgumentException e) {
                Log.i(TAG, serviceClass.getName() + " was not bound");
                return false;
            }
        }
        return false;
    }

    public static boolean isServiceRunning(Class<? extends Service> serviceClass, Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())) {
                return true;
            }
        }
        return false;
    }

    public abstract static class Binder<T extends Service> extends android.os.Binder {
        public abstract T getService();
    }

    public interface BindCallback<T extends Service> {
        void onServiceBound(T service);
    }

    @SuppressWarnings("WeakerAccess") // Class is used in a public API
    public static class Binding<T extends Service> {
        private final ServiceConnection<T> serviceConnection;
        private final Intent intent;
        private final Context context;

        private Binding(Context context, ServiceConnection<T> serviceConnection) {
            this.context = context;
            this.serviceConnection = serviceConnection;
            intent = new Intent(context, serviceConnection.getServiceClass());
        }

        public Binding setExtras(Bundle extras) {
            intent.replaceExtras(extras);
            return this;
        }

        public boolean bind() {
            return context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        public void start() {
            context.startService(intent);
        }
    }
}
