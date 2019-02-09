package com.tunjid.androidbootstrap.adapters;

import android.bluetooth.BluetoothDevice;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.communications.bluetooth.ScanResultCompat;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.viewholders.ScanViewHolder;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Adapter for BLE devices found while sacnning
 */
public class ScanAdapter extends InteractiveAdapter<ScanViewHolder, ScanAdapter.ScanAdapterListener> {

    private static final int BLE_DEVICE = 1;

    private List<ScanResultCompat> scanResults;

    public ScanAdapter(ScanAdapterListener scanAdapterListener, List<ScanResultCompat> scanResults) {
        super(scanAdapterListener);
        this.scanResults = scanResults;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ScanViewHolder(getItemView(R.layout.viewholder_scan, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder viewHolder, final int position) {
        viewHolder.bind(scanResults.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return BLE_DEVICE;
    }

    @Override
    public int getItemCount() {
        return scanResults.size();
    }

    public interface ScanAdapterListener extends InteractiveAdapter.AdapterListener {
        void onBluetoothDeviceClicked(BluetoothDevice bluetoothDevice);
    }
}
