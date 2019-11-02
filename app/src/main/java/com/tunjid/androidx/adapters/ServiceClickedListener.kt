package com.tunjid.androidx.adapters

import android.net.nsd.NsdServiceInfo

interface ServiceClickedListener {
    fun onServiceClicked(serviceInfo: NsdServiceInfo)

    fun isSelf(serviceInfo: NsdServiceInfo): Boolean
}