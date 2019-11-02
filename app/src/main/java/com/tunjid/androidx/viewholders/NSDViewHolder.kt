package com.tunjid.androidx.viewholders

import android.net.nsd.NsdServiceInfo
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.ServiceClickedListener
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.content.themeColorAt

class NSDViewHolder(
        itemView: View,
        private val listener: ServiceClickedListener
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private val textView: TextView = itemView as TextView
    private lateinit var serviceInfo: NsdServiceInfo

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(info: NsdServiceInfo) {
        serviceInfo = info

        val stringBuilder = StringBuilder()
        stringBuilder.append(info.serviceName).append("\n")
                .append(if (info.host != null) info.host.hostAddress else "")

        val isSelf = listener.isSelf(info)

        if (isSelf) stringBuilder.append(" (SELF)")

        val color =
                if (isSelf) itemView.context.colorAt(R.color.dark_grey)
                else itemView.context.themeColorAt(R.attr.prominent_text_color)

        textView.setTextColor(color)
        textView.text = stringBuilder.toString()
    }

    override fun onClick(v: View) = listener.onServiceClicked(serviceInfo)
}
