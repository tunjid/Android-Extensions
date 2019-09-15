package com.tunjid.androidbootstrap.recyclerview

import androidx.recyclerview.widget.RecyclerView
import android.view.View

abstract class InteractiveViewHolder<T>(
        itemView: View,
        protected var delegate: T?
) : RecyclerView.ViewHolder(itemView)
