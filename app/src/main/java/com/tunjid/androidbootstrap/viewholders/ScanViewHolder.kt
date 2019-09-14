package com.tunjid.androidbootstrap.viewholders

import android.view.View
import android.widget.TextView

import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.ScanAdapter
import com.tunjid.androidbootstrap.communications.bluetooth.ScanResultCompat
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder

class ScanViewHolder(itemView: View, scanAdapterListener: ScanAdapter.ScanAdapterListener)
    : InteractiveViewHolder<ScanAdapter.ScanAdapterListener>(itemView, scanAdapterListener), View.OnClickListener {

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
        when (v.id) {
            R.id.row_parent -> delegate.onBluetoothDeviceClicked(result.device)
        }
    }
}
