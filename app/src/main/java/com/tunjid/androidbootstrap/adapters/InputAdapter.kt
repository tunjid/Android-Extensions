package com.tunjid.androidbootstrap.adapters

import android.view.ViewGroup

import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.viewholders.InputViewHolder

class InputAdapter(private val hints: List<String>)
    : InteractiveAdapter<InputViewHolder, InteractiveAdapter.AdapterListener>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InputViewHolder =
            InputViewHolder(getItemView(R.layout.viewholder_simple_input, parent))

    override fun onBindViewHolder(holder: InputViewHolder, position: Int) =
            holder.bind(hints[position])

    override fun onViewDetachedFromWindow(holder: InputViewHolder) =
            holder.text.removeTextChangedListener(holder)

    override fun onViewRecycled(holder: InputViewHolder) =
            holder.text.removeTextChangedListener(holder)

    override fun getItemCount(): Int = hints.size

    override fun getItemId(position: Int): Long = hints[position].hashCode().toLong()

}