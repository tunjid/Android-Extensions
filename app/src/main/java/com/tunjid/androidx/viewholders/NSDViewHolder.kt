package com.tunjid.androidx.viewholders

import android.net.nsd.NsdServiceInfo
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.NsdAdapter
import com.tunjid.androidx.recyclerview.InteractiveViewHolder

class NSDViewHolder(itemView: View, listener: NsdAdapter.ServiceClickedListener)
    : InteractiveViewHolder<NsdAdapter.ServiceClickedListener>(itemView, listener), View.OnClickListener {

    private val textView: TextView = itemView as TextView
    private lateinit var serviceInfo: NsdServiceInfo

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(info: NsdServiceInfo, listener: NsdAdapter.ServiceClickedListener) {
        serviceInfo = info
        delegate = listener

        val stringBuilder = StringBuilder()
        stringBuilder.append(info.serviceName).append("\n")
                .append(if (info.host != null) info.host.hostAddress else "")

        val isSelf = delegate?.isSelf(info) ?: return

        if (isSelf) stringBuilder.append(" (SELF)")

        val color = ContextCompat.getColor(itemView.context,
                if (isSelf) R.color.dark_grey
                else R.color.colorPrimary)

        textView.setTextColor(color)
        textView.text = stringBuilder.toString()
    }

    override fun onClick(v: View) = delegate?.onServiceClicked(serviceInfo) ?: Unit
}
