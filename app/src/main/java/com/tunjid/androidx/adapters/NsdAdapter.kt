package com.tunjid.androidx.adapters

import android.net.nsd.NsdServiceInfo
import android.view.ViewGroup

import com.tunjid.androidx.R
import com.tunjid.androidx.recyclerview.InteractiveAdapter
import com.tunjid.androidx.view.util.inflate
import com.tunjid.androidx.viewholders.NSDViewHolder

/**
 * Adapter for showing open NSD services
 *
 *
 * Created by tj.dahunsi on 2/4/17.
 */

class NsdAdapter(listener: ServiceClickedListener, private val infoList: List<NsdServiceInfo>)
    : InteractiveAdapter<NSDViewHolder, NsdAdapter.ServiceClickedListener>(listener) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NSDViewHolder =
            NSDViewHolder(parent.inflate(R.layout.viewholder_nsd_list), delegate)

    override fun onBindViewHolder(holder: NSDViewHolder, position: Int) =
            holder.bind(infoList[position], delegate)

    override fun getItemCount(): Int = infoList.size

    interface ServiceClickedListener {
        fun onServiceClicked(serviceInfo: NsdServiceInfo)

        fun isSelf(serviceInfo: NsdServiceInfo): Boolean
    }

}
