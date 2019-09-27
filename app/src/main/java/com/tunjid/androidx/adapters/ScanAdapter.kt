package com.tunjid.androidx.adapters

import android.bluetooth.BluetoothDevice
import android.view.ViewGroup

import com.tunjid.androidx.R
import com.tunjid.androidx.communications.bluetooth.ScanResultCompat
import com.tunjid.androidx.recyclerview.InteractiveAdapter
import com.tunjid.androidx.view.util.inflate
import com.tunjid.androidx.viewholders.ScanViewHolder

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
