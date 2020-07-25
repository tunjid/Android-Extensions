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

package com.tunjid.androidx.communications.nsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class wrapping {@link NsdManager} methods
 */
@SuppressWarnings("unused")
public class NsdHelper {

    private static final String TAG = NsdHelper.class.getSimpleName();
    private static final String SERVICE_TYPE = "_http._tcp.";

    private final NsdManager nsdManager;

    private final Consumer<String> stopDiscoveryConsumer;
    private final Consumer<String> startDiscoveryConsumer;
    private final Consumer<NsdServiceInfo> serviceLostConsumer;
    private final Consumer<NsdServiceInfo> serviceFoundConsumer;
    private final Consumer<NsdServiceInfo> resolveSuccessConsumer;
    private final Consumer<NsdServiceInfo> registerSuccessConsumer;
    private final Consumer<NsdServiceInfo> unregisterSuccessConsumer;

    private final BiConsumer<String, Integer> stopDiscoveryErrorConsumer;
    private final BiConsumer<String, Integer> startDiscoveryErrorConsumer;
    private final BiConsumer<NsdServiceInfo, Integer> resolveErrorConsumer;
    private final BiConsumer<NsdServiceInfo, Integer> registerErrorConsumer;
    private final BiConsumer<NsdServiceInfo, Integer> unregisterErrorConsumer;

    private final Set<NsdManager.DiscoveryListener> discoveryListeners = new HashSet<>();
    private final Set<NsdManager.RegistrationListener> registrationListeners = new HashSet<>();

    private NsdHelper(Context context,
                      @Nullable Consumer<String> stopDiscoveryConsumer,
                      @Nullable Consumer<String> startDiscoveryConsumer,
                      @Nullable Consumer<NsdServiceInfo> serviceLostConsumer,
                      @Nullable Consumer<NsdServiceInfo> serviceFoundConsumer,
                      @Nullable Consumer<NsdServiceInfo> resolveSuccessConsumer,
                      @Nullable Consumer<NsdServiceInfo> registerSuccessConsumer,
                      @Nullable Consumer<NsdServiceInfo> unregisterSuccessConsumer,
                      @Nullable BiConsumer<String, Integer> stopDiscoveryErrorConsumer,
                      @Nullable BiConsumer<String, Integer> startDiscoveryErrorConsumer,
                      @Nullable BiConsumer<NsdServiceInfo, Integer> resolveErrorConsumer,
                      @Nullable BiConsumer<NsdServiceInfo, Integer> registerErrorConsumer,
                      @Nullable BiConsumer<NsdServiceInfo, Integer> unregisterErrorConsumer) {
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        this.stopDiscoveryConsumer = orNull(stopDiscoveryConsumer);
        this.startDiscoveryConsumer = orNull(startDiscoveryConsumer);

        this.serviceLostConsumer = orNull(serviceLostConsumer);
        this.serviceFoundConsumer = orNull(serviceFoundConsumer);
        this.resolveSuccessConsumer = orNull(resolveSuccessConsumer);
        this.registerSuccessConsumer = orNull(registerSuccessConsumer);
        this.unregisterSuccessConsumer = orNull(unregisterSuccessConsumer);

        this.resolveErrorConsumer = orNull(resolveErrorConsumer);
        this.registerErrorConsumer = orNull(registerErrorConsumer);
        this.unregisterErrorConsumer = orNull(unregisterErrorConsumer);
        this.stopDiscoveryErrorConsumer = orNull(stopDiscoveryErrorConsumer);
        this.startDiscoveryErrorConsumer = orNull(startDiscoveryErrorConsumer);
    }

    public static Builder getBuilder(Context context) {
        return new Builder(context);
    }

    public static PrintWriter createPrintWriter(Socket socket) throws IOException {
        return new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
    }

    public static BufferedReader createBufferedReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public NsdManager getNsdManager() {
        return nsdManager;
    }

    public void registerService(int port, String serviceName) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(SERVICE_TYPE);

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, getRegistrationListener());
    }

    public void discoverServices() {
        stopServiceDiscovery();
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, getDiscoveryListener());
    }

    public void resolveService(NsdServiceInfo serviceInfo) {
        nsdManager.resolveService(serviceInfo, getResolveListener());
    }

    public void stopServiceDiscovery() {
        for (NsdManager.DiscoveryListener listener : discoveryListeners) {
            try {
                nsdManager.stopServiceDiscovery(listener);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Discovery listener not added");
            }
        }
        discoveryListeners.clear();
    }

    private void unregisterService() {
        for (NsdManager.RegistrationListener listener : registrationListeners) {
            try {
                nsdManager.unregisterService(listener);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Registration listener not added");
            }
        }
        registrationListeners.clear();
    }

    public void tearDown() {
        stopServiceDiscovery();
        unregisterService();
    }

    private <T> Consumer<T> orNull(@Nullable Consumer<T> source) {
        return source == null ? ignored -> {
        } : source;
    }

    private <T, R> BiConsumer<T, R> orNull(@Nullable BiConsumer<T, R> source) {
        return source == null ? (ignoredA, ignoredB) -> {
        } : source;
    }

    @NonNull
    private NsdManager.ResolveListener getResolveListener() {
        return new NsdManager.ResolveListener() {
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                resolveErrorConsumer.accept(serviceInfo, errorCode);
                Log.w(TAG, String.format("Failed to resolve service\n%1$s\nwith code %2$d", serviceInfo, errorCode));
            }

            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                resolveSuccessConsumer.accept(serviceInfo);
                Log.w(TAG, "resolved service\n" + serviceInfo);
            }
        };
    }

    private NsdManager.DiscoveryListener getDiscoveryListener() {
        NsdManager.DiscoveryListener listener = new NsdManager.DiscoveryListener() {
            public void onServiceFound(NsdServiceInfo service) {
                serviceFoundConsumer.accept(service);
                Log.w(TAG, "Found service\n" + service);
            }

            public void onServiceLost(NsdServiceInfo service) {
                serviceLostConsumer.accept(service);
                Log.w(TAG, "Lost service:\n" + service);
            }

            public void onDiscoveryStarted(String regType) {
                startDiscoveryConsumer.accept(regType);
                Log.w(TAG, "Service discovery started for " + regType);
            }

            public void onDiscoveryStopped(String serviceType) {
                stopDiscoveryConsumer.accept(serviceType);
                Log.w(TAG, "Service discovery stopped for " + serviceType);
            }

            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                startDiscoveryErrorConsumer.accept(serviceType, errorCode);
                Log.w(TAG, String.format("Service discovery start failed for service type\n%1$s\nwith code %2$d", serviceType, errorCode));
            }

            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                stopDiscoveryErrorConsumer.accept(serviceType, errorCode);
                Log.w(TAG, String.format("Service discovery stop failed for service type\n%1$s\nwith code %2$d", serviceType, errorCode));
            }
        };
        discoveryListeners.add(listener);
        return listener;
    }

    private NsdManager.RegistrationListener getRegistrationListener() {
        NsdManager.RegistrationListener listener = new NsdManager.RegistrationListener() {
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                registerErrorConsumer.accept(serviceInfo, errorCode);
                Log.w(TAG, String.format("Service registration failed for service type\n%1$s\nwith code %2$d", serviceInfo.toString(), errorCode));
            }

            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                unregisterErrorConsumer.accept(serviceInfo, errorCode);
                Log.w(TAG, String.format("Service un-registration failed for service type\n%1$s\nwith code %2$d", serviceInfo.toString(), errorCode));
            }

            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                registerSuccessConsumer.accept(serviceInfo);
                Log.w(TAG, "Registered service:\n" + serviceInfo);
            }

            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                unregisterSuccessConsumer.accept(serviceInfo);
                Log.w(TAG, "Un-registered service:\n" + serviceInfo);
            }
        };
        registrationListeners.add(listener);
        return listener;
    }

    @FunctionalInterface
    public interface Consumer<T> {
        void accept(T t);
    }

    @FunctionalInterface
    public interface BiConsumer<T, U> {
        void accept(T t, U u);
    }

    @SuppressWarnings("WeakerAccess")
    public static final class Builder {

        private Context context;
        private Consumer<String> stopDiscoveryConsumer;
        private Consumer<String> startDiscoveryConsumer;
        private Consumer<NsdServiceInfo> serviceLostConsumer;
        private Consumer<NsdServiceInfo> serviceFoundConsumer;
        private Consumer<NsdServiceInfo> resolveSuccessConsumer;
        private Consumer<NsdServiceInfo> registerSuccessConsumer;
        private Consumer<NsdServiceInfo> unregisterSuccessConsumer;
        private BiConsumer<String, Integer> stopDiscoveryErrorConsumer;
        private BiConsumer<String, Integer> startDiscoveryErrorConsumer;
        private BiConsumer<NsdServiceInfo, Integer> resolveErrorConsumer;
        private BiConsumer<NsdServiceInfo, Integer> registerErrorConsumer;
        private BiConsumer<NsdServiceInfo, Integer> unregisterErrorConsumer;

        private Builder(Context context) {
            this.context = context;
        }

        public Builder with(Context context) {
            return new Builder(context);
        }


        public Builder setStopDiscoveryConsumer(Consumer<String> stopDiscoveryConsumer) {
            this.stopDiscoveryConsumer = stopDiscoveryConsumer;
            return this;
        }

        public Builder setStartDiscoveryConsumer(Consumer<String> startDiscoveryConsumer) {
            this.startDiscoveryConsumer = startDiscoveryConsumer;
            return this;
        }

        public Builder setServiceLostConsumer(Consumer<NsdServiceInfo> serviceLostConsumer) {
            this.serviceLostConsumer = serviceLostConsumer;
            return this;
        }

        public Builder setServiceFoundConsumer(Consumer<NsdServiceInfo> serviceFoundConsumer) {
            this.serviceFoundConsumer = serviceFoundConsumer;
            return this;
        }

        public Builder setResolveSuccessConsumer(Consumer<NsdServiceInfo> resolveSuccessConsumer) {
            this.resolveSuccessConsumer = resolveSuccessConsumer;
            return this;
        }

        public Builder setRegisterSuccessConsumer(Consumer<NsdServiceInfo> registerSuccessConsumer) {
            this.registerSuccessConsumer = registerSuccessConsumer;
            return this;
        }

        public Builder setUnregisterSuccessConsumer(Consumer<NsdServiceInfo> unregisterSuccessConsumer) {
            this.unregisterSuccessConsumer = unregisterSuccessConsumer;
            return this;
        }

        public Builder setStopDiscoveryErrorConsumer(BiConsumer<String, Integer> stopDiscoveryErrorConsumer) {
            this.stopDiscoveryErrorConsumer = stopDiscoveryErrorConsumer;
            return this;
        }

        public Builder setStartDiscoveryErrorConsumer(BiConsumer<String, Integer> startDiscoveryErrorConsumer) {
            this.startDiscoveryErrorConsumer = startDiscoveryErrorConsumer;
            return this;
        }

        public Builder setResolveErrorConsumer(BiConsumer<NsdServiceInfo, Integer> resolveErrorConsumer) {
            this.resolveErrorConsumer = resolveErrorConsumer;
            return this;
        }

        public Builder setRegisterErrorConsumer(BiConsumer<NsdServiceInfo, Integer> registerErrorConsumer) {
            this.registerErrorConsumer = registerErrorConsumer;
            return this;
        }

        public Builder setUnregisterErrorConsumer(BiConsumer<NsdServiceInfo, Integer> unregisterErrorConsumer) {
            this.unregisterErrorConsumer = unregisterErrorConsumer;
            return this;
        }

        public NsdHelper build() {
            return new NsdHelper(context,
                    stopDiscoveryConsumer,
                    startDiscoveryConsumer,
                    serviceLostConsumer,
                    serviceFoundConsumer,
                    resolveSuccessConsumer,
                    registerSuccessConsumer,
                    unregisterSuccessConsumer,
                    stopDiscoveryErrorConsumer,
                    startDiscoveryErrorConsumer,
                    resolveErrorConsumer,
                    registerErrorConsumer,
                    unregisterErrorConsumer);
        }
    }
}
