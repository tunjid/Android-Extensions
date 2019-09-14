package com.tunjid.androidbootstrap.recyclerview

import androidx.recyclerview.widget.RecyclerView


abstract class InteractiveAdapter<VH : InteractiveViewHolder<*>, T : Any?> protected constructor(
        val adapterDelegate: T
) : RecyclerView.Adapter<VH>()
