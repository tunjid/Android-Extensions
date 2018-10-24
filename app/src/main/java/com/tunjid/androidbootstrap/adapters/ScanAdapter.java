package com.tunjid.androidbootstrap.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.communications.bluetooth.ScanResultCompat;
import com.tunjid.androidbootstrap.view.recyclerview.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.BaseViewHolder;

import java.util.List;

/**
 * Adapter for BLE devices found while sacnning
 */
public class ScanAdapter extends BaseRecyclerViewAdapter<ScanAdapter.ViewHolder, ScanAdapter.ScanAdapterListener> {

    private static final int BLE_DEVICE = 1;

    private List<ScanResultCompat> scanResults;

    public ScanAdapter(ScanAdapterListener scanAdapterListener, List<ScanResultCompat> scanResults) {
        super(scanAdapterListener);
        this.scanResults = scanResults;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_scan, viewGroup, false);

        return new ViewHolder(itemView, adapterListener);
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
    static class ViewHolder extends BaseViewHolder<ScanAdapterListener>
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

    public interface ScanAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onBluetoothDeviceClicked(BluetoothDevice bluetoothDevice);
    }
}
