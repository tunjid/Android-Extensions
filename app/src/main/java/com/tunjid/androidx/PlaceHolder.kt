package com.tunjid.androidx

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

class PlaceHolder(viewGroup: ViewGroup) {

    private val text: TextView = viewGroup.findViewById(R.id.placeholder_text)
    private val icon: ImageView = viewGroup.findViewById(R.id.placeholder_icon)

    fun toggle(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE

        text.visibility = visibility
        icon.visibility = visibility
    }

    fun bind(state: State) {
        text.setText(state.text)
        icon.setImageResource(state.icon)
    }

    class State(@field:StringRes internal val text: Int, @field:DrawableRes internal val icon: Int)
}
