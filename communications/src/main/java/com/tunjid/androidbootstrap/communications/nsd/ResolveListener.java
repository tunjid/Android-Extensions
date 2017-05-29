package com.tunjid.androidbootstrap.communications.nsd;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

/**
 * Abstract {@link ResolveListener}
 * <p>
 * Created by tj.dahunsi on 2/4/17.
 */

public abstract class ResolveListener implements NsdManager.ResolveListener {
    @Override
    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {

    }

    @Override
    public void onServiceResolved(NsdServiceInfo serviceInfo) {

    }
}
