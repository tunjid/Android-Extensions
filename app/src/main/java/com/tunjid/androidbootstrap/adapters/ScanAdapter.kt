package com.tunjid.androidbootstrap.adapters

import android.bluetooth.BluetoothDevice
import android.view.ViewGroup

import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.communications.bluetooth.ScanResultCompat
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.view.util.inflate
import com.tunjid.androidbootstrap.viewholders.ScanViewHolder

/**
 * Adapter for BLE devices found while scanning
 */
class ScanAdapter(scanAdapterListener: ScanAdapterListener, private val scanResults: List<ScanResultCompat>)
    : InteractiveAdapter<ScanViewHolder, ScanAdapter.ScanAdapterListener>(scanAdapterListener) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ScanViewHolder =
            ScanViewHolder(viewGroup.inflate(R.layout.viewholder_scan), delegate)

    override fun onBindViewHolder(viewHolder: ScanViewHolder, position: Int) =
            viewHolder.bind(scanResults[position])

    override fun getItemViewType(position: Int): Int = BLE_DEVICE

    override fun getItemCount(): Int = scanResults.size

    interface ScanAdapterListener {
        fun onBluetoothDeviceClicked(bluetoothDevice: BluetoothDevice)
    }

    companion object {

        private const val BLE_DEVICE = 1
    }
}
