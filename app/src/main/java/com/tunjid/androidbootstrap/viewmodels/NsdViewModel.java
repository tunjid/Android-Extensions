package com.tunjid.androidbootstrap.viewmodels;

import android.app.Application;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import com.tunjid.androidbootstrap.communications.nsd.NsdHelper;
import com.tunjid.androidbootstrap.recyclerview.Diff;
import com.tunjid.androidbootstrap.recyclerview.Differentiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class NsdViewModel extends AndroidViewModel {

    private static final long SCAN_PERIOD = 10;

    private final NsdHelper nsdHelper;
    private final List<NsdServiceInfo> services;
    private PublishProcessor<Diff<NsdServiceInfo>> processor;


    public NsdViewModel(@NonNull Application application) {
        super(application);

        nsdHelper = NsdHelper.getBuilder(getApplication())
                .setServiceFoundConsumer(this::onServiceFound)
                .setResolveSuccessConsumer(this::onServiceResolved)
                .setResolveErrorConsumer(this::onServiceResolutionFailed)
                .build();

        services = new ArrayList<>();
        reset();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        nsdHelper.stopServiceDiscovery();
        nsdHelper.tearDown();
    }

    public List<NsdServiceInfo> getServices() {
        return services;
    }

    public Flowable<DiffUtil.DiffResult> findDevices() {
        reset();
        nsdHelper.discoverServices();

        // Clear list first, then start scanning.
        return Flowable.fromCallable(() -> Differentiable.diff(services,
                Collections.emptyList(),
                (__, ___) -> Collections.emptyList(),
                info -> Differentiable.fromCharSequence(info::getServiceName)))
                .concatWith(processor.take(SCAN_PERIOD, TimeUnit.SECONDS, Schedulers.io()))
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread()).map(diff -> {
                    services.clear();
                    services.addAll(diff.cumulative);
                    return diff.result;
                });
    }

    public void stopScanning() {
        if (processor == null) processor = PublishProcessor.create();
        else if (!processor.hasComplete()) processor.onComplete();

        nsdHelper.stopServiceDiscovery();
    }

    private void reset() {
        stopScanning();
        processor = PublishProcessor.create();
    }

    private void onServiceFound(NsdServiceInfo service) {
        nsdHelper.resolveService(service);
    }

    private void onServiceResolved(NsdServiceInfo service) {
        if (!services.contains(service) && !processor.hasComplete()) processor.onNext(Differentiable.diff(
                services,
                Collections.singletonList(service),
                this::addServices,
                info -> Differentiable.fromCharSequence(info::getServiceName)));
    }

    private void onServiceResolutionFailed(NsdServiceInfo service, int errorCode) {
        if (errorCode == NsdManager.FAILURE_ALREADY_ACTIVE) nsdHelper.resolveService(service);
    }

    private List<NsdServiceInfo> addServices(List<NsdServiceInfo> currentServices, List<NsdServiceInfo> foundServices) {
        currentServices.addAll(foundServices);
        return currentServices;
    }
}
