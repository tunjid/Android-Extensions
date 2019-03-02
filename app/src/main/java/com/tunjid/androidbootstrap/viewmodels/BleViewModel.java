package com.tunjid.androidbootstrap.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;

import com.tunjid.androidbootstrap.communications.bluetooth.BLEScanner;
import com.tunjid.androidbootstrap.communications.bluetooth.ScanFilterCompat;
import com.tunjid.androidbootstrap.communications.bluetooth.ScanResultCompat;
import com.tunjid.androidbootstrap.functions.Function;
import com.tunjid.androidbootstrap.functions.collections.Lists;
import com.tunjid.androidbootstrap.recyclerview.diff.Diff;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class BleViewModel extends AndroidViewModel {

    private static final long SCAN_PERIOD = 10;
    private static final String CUSTOM_SERVICE_UUID = "195AE58A-437A-489B-B0CD-B7C9C394BAE4";

    @Nullable
    private final BLEScanner scanner;
    private final List<ScanResultCompat> scanResults;
    private PublishProcessor<Diff<ScanResultCompat>> processor;


    public BleViewModel(@NonNull Application application) {
        super(application);
        scanResults = new ArrayList<>();

        BluetoothManager bluetoothManager = application.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                ? (BluetoothManager) application.getSystemService(Context.BLUETOOTH_SERVICE)
                : null;

        if (bluetoothManager != null) scanner = BLEScanner.getBuilder(bluetoothManager.getAdapter())
                .addFilter(ScanFilterCompat.getBuilder()
                        .setServiceUuid(new ParcelUuid(UUID.fromString(CUSTOM_SERVICE_UUID)))
                        .build())
                .withCallBack(this::onDeviceFound)
                .build();
        else scanner = null;

        reset();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (scanner != null) scanner.stopScan();
    }

    public boolean hasBle() {
        return scanner != null;
    }

    public boolean isBleOn() {
        return scanner != null && scanner.isEnabled();
    }

    public List<ScanResultCompat> getScanResults() {
        return scanResults;
    }

    public Flowable<DiffUtil.DiffResult> findDevices() {
        if (scanner == null) return Flowable.empty();

        reset();
        scanner.startScan();

        // Clear list first, then start scanning.
        return Flowable.fromCallable(() -> Diff.calculate(scanResults,
                Collections.emptyList(),
                (__, ___) -> Collections.emptyList(),
                result -> Differentiable.fromCharSequence(result.getDevice()::getAddress)))
                .concatWith(processor.take(SCAN_PERIOD, TimeUnit.SECONDS, Schedulers.io()))
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread()).map(diff -> {
                    Lists.replace(scanResults, diff.items);
                    return diff.result;
                });
    }

    public void stopScanning() {
        if (processor == null) processor = PublishProcessor.create();
        else if (!processor.hasComplete()) processor.onComplete();

        if (scanner != null) scanner.stopScan();
    }

    private void reset() {
        stopScanning();
        processor = PublishProcessor.create();
    }


    private void onDeviceFound(ScanResultCompat scanResult) {
        if (!processor.hasComplete()) processor.onNext(Diff.calculate(
                scanResults,
                Collections.singletonList(scanResult),
                this::addServices,
                result -> Differentiable.fromCharSequence(result.getDevice()::getAddress)));
    }

    private List<ScanResultCompat> addServices(List<ScanResultCompat> currentServices, List<ScanResultCompat> foundServices) {
        Function<ScanResultCompat, String> equalityMapper = result -> result.getDevice().getAddress();
        List<ScanResultCompat> union = Lists.union(currentServices, foundServices, equalityMapper);
        Lists.replace(currentServices, union);
        Collections.sort(currentServices, (a, b) -> equalityMapper.apply(a).compareTo(equalityMapper.apply(b)));
        return currentServices;
    }
}
