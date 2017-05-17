/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tunjid.androidbootstrap.communications.nsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Utility class wrapping {@link NsdManager} methods
 */
@SuppressWarnings("unused")
public class NsdHelper {

    private static final String TAG = NsdHelper.class.getSimpleName();
    private static final String SERVICE_TYPE = "_http._tcp.";

    private final NsdManager nsdManager;

    private NsdManager.ResolveListener resolveListener;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.RegistrationListener registrationListener;

    private NsdHelper(Context context) {
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public static Builder getBuilder(Context context){
        return new Builder(context);
    }

    public NsdManager getNsdManager() {
        return nsdManager;
    }

    public void registerService(int port, String serviceName) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(SERVICE_TYPE);

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    public boolean discoverServices() {
        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
            return true;
        }
        catch (IllegalArgumentException e) {
            Log.w(TAG, "Nsd Discovery Listener already added");
            return false;
        }
    }

    public boolean unregisterService() {
        try {
            nsdManager.unregisterService(registrationListener);
            return true;
        }
        catch (IllegalArgumentException e) {
            Log.w(TAG, "Nsd Registration listener not added");
            return false;
        }
    }

    public boolean resolveService(NsdServiceInfo serviceInfo) {
        try {
            nsdManager.resolveService(serviceInfo, resolveListener);
            return true;
        }
        catch (IllegalArgumentException e) {
            Log.w(TAG, "Nsd Discovery Listener already added");
            return false;
        }
    }

    public boolean stopServiceDiscovery() {
        try { // May or may not be looking for services, throws IllegalArgumentException if it isn't
            nsdManager.stopServiceDiscovery(discoveryListener);
            return true;
        }
        catch (IllegalArgumentException e) {
            Log.w(TAG, "Nsd Discovery Listener not added");
            return false;
        }
    }

    public boolean tearDown() {
        stopServiceDiscovery();
        try { // May or may not be registered, throws IllegalArgumentException if it isn't
            nsdManager.unregisterService(registrationListener);
            return true;
        }
        catch (IllegalArgumentException e) {
            Log.w(TAG, "Nsd Registration Listener not added");
            return false;
        }
    }

    public static PrintWriter createPrintWriter(Socket socket) throws IOException {
        return new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
    }

    public static BufferedReader createBufferedReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @SuppressWarnings("WeakerAccess")
    public static final class Builder {

        private NsdHelper nsdHelper;

        private Builder(Context context) {
            nsdHelper = new NsdHelper(context);
        }

        public Builder with(Context context) {
            return new Builder(context);
        }

        public Builder setDiscoveryListener(NsdManager.DiscoveryListener listener) {
            nsdHelper.discoveryListener = listener;
            return this;
        }

        public Builder setRegistrationListener(@NonNull NsdManager.RegistrationListener listener) {
            nsdHelper.registrationListener = listener;
            return this;
        }

        public Builder setResolveListener(@NonNull NsdManager.ResolveListener listener) {
            nsdHelper.resolveListener = listener;
            return this;
        }

        public NsdHelper build() {
            return nsdHelper;
        }
    }
}
