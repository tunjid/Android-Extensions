package com.tunjid.androidbootstrap.adapters

import android.net.nsd.NsdServiceInfo
import android.view.ViewGroup

import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.viewholders.NSDViewHolder

/**
 * Adapter for showing open NSD services
 *
 *
 * Created by tj.dahunsi on 2/4/17.
 */

class NsdAdapter(listener: ServiceClickedListener, private val infoList: List<NsdServiceInfo>)
    : InteractiveAdapter<NSDViewHolder, NsdAdapter.ServiceClickedListener>(listener) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NSDViewHolder =
            NSDViewHolder(getItemView(R.layout.viewholder_nsd_list, parent))

    override fun onBindViewHolder(holder: NSDViewHolder, position: Int) =
            holder.bind(infoList[position], adapterListener)

    override fun getItemCount(): Int = infoList.size

    interface ServiceClickedListener : AdapterListener {
        fun onServiceClicked(serviceInfo: NsdServiceInfo)

        fun isSelf(serviceInfo: NsdServiceInfo): Boolean
    }

}
