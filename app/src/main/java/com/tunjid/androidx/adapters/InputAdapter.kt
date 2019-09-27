package com.tunjid.androidx.adapters

import android.view.ViewGroup

import com.tunjid.androidx.R
import com.tunjid.androidx.recyclerview.InteractiveAdapter
import com.tunjid.androidx.view.util.inflate
import com.tunjid.androidx.viewholders.InputViewHolder

class InputAdapter(private val hints: List<String>)
    : InteractiveAdapter<InputViewHolder, Unit>(Unit) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InputViewHolder =
            InputViewHolder(parent.inflate(R.layout.viewholder_simple_input))

    override fun onBindViewHolder(holder: InputViewHolder, position: Int) =
            holder.bind(hints[position])

    override fun onViewDetachedFromWindow(holder: InputViewHolder) =
            holder.text.removeTextChangedListener(holder)

    override fun onViewRecycled(holder: InputViewHolder) =
            holder.text.removeTextChangedListener(holder)

    override fun getItemCount(): Int = hints.size

    override fun getItemId(position: Int): Long = hints[position].hashCode().toLong()

}