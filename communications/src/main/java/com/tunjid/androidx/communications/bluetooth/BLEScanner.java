package com.tunjid.androidx.communications.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

/**
 * BLE Scanner
 * <p>
 * Created by tj.dahunsi on 2/18/17.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BLEScanner {

    private final boolean hasLollipop;
    private final BluetoothAdapter bluetoothAdapter;
    private final List<ScanFilterCompat> compatFilters;

    private BleScanCallback callback;
    private ScanSettings scanSettings;

    private BluetoothAdapter.LeScanCallback oldCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            ScanResultCompat scanResultCompat = new ScanResultCompat(device,
                    ScanRecordCompat.parseFromBytes(scanRecord), rssi, 0);
            if (matchesFilters(scanResultCompat)) callback.onDeviceFound(scanResultCompat);
        }
    };

    private ScanCallback newCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
            byte[] scanRecord = result.getScanRecord() == null ? null : result.getScanRecord().getBytes();

            ScanResultCompat scanResultCompat = new ScanResultCompat(result.getDevice(),
                    ScanRecordCompat.parseFromBytes(scanRecord), result.getRssi(), result.getTimestampNanos());

            if (matchesFilters(scanResultCompat)) callback.onDeviceFound(scanResultCompat);
        }

        @Override
        public void onScanFailed(int errorCode) {
        }
    };

    private BLEScanner(@NonNull BluetoothAdapter bluetoothAdapter) {
        hasLollipop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        this.bluetoothAdapter = bluetoothAdapter;
        compatFilters = new ArrayList<>();
    }

    public static Builder getBuilder(BluetoothAdapter adapter) {
        return new Builder(adapter);
    }

    public boolean isEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    @SuppressWarnings("deprecation")
    public void startScan() {
//        if (hasLollipop)
//            bluetoothAdapter.getBluetoothLeScanner().startScan(scanFilters, scanSettings, newCallback);
        if (hasLollipop) bluetoothAdapter.getBluetoothLeScanner().startScan(newCallback);
        else bluetoothAdapter.startLeScan(oldCallback);
    }

    @SuppressWarnings("deprecation")
    public void stopScan() {
        if (hasLollipop) bluetoothAdapter.getBluetoothLeScanner().stopScan(newCallback);
        else bluetoothAdapter.stopLeScan(oldCallback);
    }

    private boolean matchesFilters(ScanResultCompat result) {
        if (compatFilters.isEmpty()) return true;
        for (ScanFilterCompat filter : compatFilters) if (filter.matches(result)) return true;
        return false;
    }

    /**
     * BLE scan callback
     * <p>
     * Created by tj.dahunsi on 2/18/17.
     */
    public interface BleScanCallback {
        void onDeviceFound(ScanResultCompat scanResult);
    }

    @SuppressWarnings("WeakerAccess")
    public static class Builder {
        private final BLEScanner scanner;

        private Builder(BluetoothAdapter adapter) {
            this.scanner = new BLEScanner(adapter);
        }

        public Builder withCallBack(BleScanCallback callBack) {
            if (callBack == null)
                throw new IllegalArgumentException("Scan callback cannot be null");
            scanner.callback = callBack;
            return this;
        }

        /**
         * Adds scan settings. @see {@link ScanSettings}
         */
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @SuppressWarnings("unused")
        public Builder withSettings(ScanSettings settings) {
            if (settings == null) {
                throw new IllegalArgumentException("Scan settings cannot be null");
            }
            if (scanner.hasLollipop) {
                scanner.scanSettings = settings;
            }
            return this;
        }

        /**
         * Adds a scan filter for scanning for BLE devices. It defaults to {@link ScanFilter}
         * on api levels greater than lollipop, or falls back to internal sorting on lower api
         * versions
         */
        public Builder addFilter(ScanFilterCompat filterCompat) {
            if (filterCompat == null) {
                throw new IllegalArgumentException("Scan filter cannot be null");
            }
            scanner.compatFilters.add(filterCompat);
            return this;
        }

        /**
         * Builds a configured BLE Scanner.
         */
        public BLEScanner build() {
            if (scanner.callback == null) {
                throw new IllegalArgumentException("Scan callback cannot be null");
            }
            if (scanner.hasLollipop && scanner.scanSettings == null) {
                scanner.scanSettings = new ScanSettings.Builder().build();
            }
            return scanner;
        }
    }
}
