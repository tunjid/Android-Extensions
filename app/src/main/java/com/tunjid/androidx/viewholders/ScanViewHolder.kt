package com.tunjid.androidx.viewholders

import android.bluetooth.BluetoothDevice
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.tunjid.androidx.R
import com.tunjid.androidx.communications.bluetooth.ScanResultCompat

class ScanViewHolder(
        itemView: View,
        private val clickListener: (BluetoothDevice) -> Unit
)
    : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private val deviceName: TextView = itemView.findViewById(R.id.device_name)
    private val deviceAddress: TextView = itemView.findViewById(R.id.device_address)

    private lateinit var result: ScanResultCompat

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(result: ScanResultCompat) {
        this.result = result
        if (result.scanRecord != null) {
            deviceName.text = result.scanRecord!!.deviceName
            deviceAddress.text = result.device.address
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.row_parent) clickListener(result.device)
    }
}
