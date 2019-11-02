package com.tunjid.androidx.viewholders

import android.net.nsd.NsdServiceInfo
import android.view.View
import android.widget.TextView
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.ServiceClickedListener
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.recyclerview.InteractiveViewHolder

class NSDViewHolder(itemView: View, listener: ServiceClickedListener)
    : InteractiveViewHolder<ServiceClickedListener>(itemView, listener), View.OnClickListener {

    private val textView: TextView = itemView as TextView
    private lateinit var serviceInfo: NsdServiceInfo

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(info: NsdServiceInfo, listener: ServiceClickedListener) {
        serviceInfo = info
        delegate = listener

        val stringBuilder = StringBuilder()
        stringBuilder.append(info.serviceName).append("\n")
                .append(if (info.host != null) info.host.hostAddress else "")

        val isSelf = delegate?.isSelf(info) ?: return

        if (isSelf) stringBuilder.append(" (SELF)")

        val color =
                if (isSelf) itemView.context.colorAt(R.color.dark_grey)
                else itemView.context.themeColorAt(R.attr.prominent_text_color)

        textView.setTextColor(color)
        textView.text = stringBuilder.toString()
    }

    override fun onClick(v: View) = delegate?.onServiceClicked(serviceInfo) ?: Unit
}
