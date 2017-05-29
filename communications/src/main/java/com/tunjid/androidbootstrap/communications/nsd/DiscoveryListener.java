package com.tunjid.androidbootstrap.communications.nsd;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

/**
 * Abstract {@link DiscoveryListener}
 * <p>
 * Created by tj.dahunsi on 2/4/17.
 */

public abstract class DiscoveryListener implements NsdManager.DiscoveryListener {
    private static final String TAG = DiscoveryListener.class.getSimpleName();

    @Override
    public void onDiscoveryStarted(String regType) {
        Log.d(TAG, "Service discovery started");
    }

    @Override
    public void onServiceFound(NsdServiceInfo service) {
        Log.d(TAG, "Service discovery success " + service);
    }

    @Override
    public void onServiceLost(NsdServiceInfo service) {
        Log.e(TAG, "service lost " + service);
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.d(TAG, "Discovery stopped: " + serviceType);
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.d(TAG, "Discovery failed: Error code: " + errorCode);
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.d(TAG, "Discovery failed: Error code: " + errorCode);
    }
}
