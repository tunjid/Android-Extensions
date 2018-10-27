package com.tunjid.androidbootstrap.adapters;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.communications.bluetooth.ScanResultCompat;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Adapter for BLE devices found while sacnning
 */
public class ScanAdapter extends InteractiveAdapter<ScanAdapter.ViewHolder, ScanAdapter.ScanAdapterListener> {

    private static final int BLE_DEVICE = 1;

    private List<ScanResultCompat> scanResults;

    public ScanAdapter(ScanAdapterListener scanAdapterListener, List<ScanResultCompat> scanResults) {
        super(scanAdapterListener);
        this.scanResults = scanResults;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(getItemView(R.layout.viewholder_scan, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
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

    // ViewHolder for actual content
    static class ViewHolder extends InteractiveViewHolder<ScanAdapterListener>
            implements
            View.OnClickListener {

        TextView deviceName;
        TextView deviceAddress;

        ScanResultCompat result;

        ViewHolder(View itemView, ScanAdapterListener scanAdapterListener) {
            super(itemView, scanAdapterListener);

            deviceAddress = itemView.findViewById(R.id.device_address);
            deviceName = itemView.findViewById(R.id.device_name);
            itemView.setOnClickListener(this);
        }

        void bind(ScanResultCompat result) {
            this.result = result;
            if (result.getScanRecord() != null) {
                deviceName.setText(result.getScanRecord().getDeviceName());
                deviceAddress.setText(result.getDevice().getAddress());
            }
        }

        @Override
        public void onClick(View v) {
            switch ((v.getId())) {
                case R.id.row_parent:
                    adapterListener.onBluetoothDeviceClicked(result.getDevice());
                    break;
            }
        }
    }

    public interface ScanAdapterListener extends InteractiveAdapter.AdapterListener {
        void onBluetoothDeviceClicked(BluetoothDevice bluetoothDevice);
    }
}
