package com.tunjid.androidbootstrap.communications.nsd;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

/**
 * Class holding boiler plate for {@link NsdManager.RegistrationListener}
 * <p>
 * Created by tj.dahunsi on 2/13/17.
 */

public class RegistrationListener implements NsdManager.RegistrationListener {
    @Override
    public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

    }

    @Override
    public void onServiceRegistered(NsdServiceInfo serviceInfo) {

    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo serviceInfo) {

    }
}
