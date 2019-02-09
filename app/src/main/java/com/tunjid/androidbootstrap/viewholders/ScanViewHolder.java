package com.tunjid.androidbootstrap.viewholders;

import android.view.View;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.ScanAdapter;
import com.tunjid.androidbootstrap.communications.bluetooth.ScanResultCompat;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

public class ScanViewHolder extends InteractiveViewHolder<ScanAdapter.ScanAdapterListener>
         implements
         View.OnClickListener {

     private TextView deviceName;
     private TextView deviceAddress;

     private ScanResultCompat result;

    public ScanViewHolder(View itemView, ScanAdapter.ScanAdapterListener scanAdapterListener) {
         super(itemView, scanAdapterListener);

         deviceAddress = itemView.findViewById(R.id.device_address);
         deviceName = itemView.findViewById(R.id.device_name);
         itemView.setOnClickListener(this);
     }

    public void bind(ScanResultCompat result) {
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
