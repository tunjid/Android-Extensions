package com.tunjid.androidbootstrap.viewholders

import android.net.nsd.NsdServiceInfo
import android.view.View
import android.widget.TextView

import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.NsdAdapter
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder

import androidx.core.content.ContextCompat

class NSDViewHolder(itemView: View)
    : InteractiveViewHolder<NsdAdapter.ServiceClickedListener>(itemView), View.OnClickListener {

    private val textView: TextView = itemView as TextView
    private lateinit var serviceInfo: NsdServiceInfo

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(info: NsdServiceInfo, listener: NsdAdapter.ServiceClickedListener) {
        serviceInfo = info
        adapterListener = listener

        val stringBuilder = StringBuilder()
        stringBuilder.append(info.serviceName).append("\n")
                .append(if (info.host != null) info.host.hostAddress else "")

        val isSelf = adapterListener.isSelf(info)

        if (isSelf) stringBuilder.append(" (SELF)")

        val color = ContextCompat.getColor(itemView.context,
                if (isSelf) R.color.dark_grey
                else R.color.colorPrimary)

        textView.setTextColor(color)
        textView.text = stringBuilder.toString()
    }

    override fun onClick(v: View) = adapterListener.onServiceClicked(serviceInfo)
}
