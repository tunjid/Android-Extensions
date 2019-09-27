package com.tunjid.androidx.viewholders

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.RouteAdapter
import com.tunjid.androidx.model.Route
import com.tunjid.androidx.recyclerview.InteractiveViewHolder

import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.TextViewCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

class RouteItemViewHolder(itemView: View, listener: RouteAdapter.RouteAdapterListener) : InteractiveViewHolder<RouteAdapter.RouteAdapterListener>(itemView, listener), View.OnClickListener {

    private var route: Route? = null

    private val routeDestination: TextView = itemView.findViewById(R.id.destination)
    private val routeDescription: TextView = itemView.findViewById(R.id.description)

    init {
        itemView.setOnClickListener(this)
        routeDescription.setOnClickListener(this)

        setIcons(true, routeDestination)
    }

    fun bind(route: Route) {
        this.route = route

        routeDestination.text = route.destination
        routeDescription.text = route.description
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.description -> delegate?.onItemClicked(route!!)
            else -> changeVisibility(routeDestination, routeDescription)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setIcons(isDown: Boolean, vararg textViews: TextView) {
        val resVal = if (isDown) R.drawable.anim_vect_down_to_right_arrow else R.drawable.anim_vect_right_to_down_arrow

        for (textView in textViews) {
            var icon: Drawable? = AnimatedVectorDrawableCompat.create(itemView.context, resVal)
            if (icon != null) {
                icon = DrawableCompat.wrap(icon.mutate())
                DrawableCompat.setTint(icon!!, R.color.dark_grey)
                DrawableCompat.setTintMode(icon, PorterDuff.Mode.SRC_IN)
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView, null, null, icon, null)
            }
        }
    }

    private fun changeVisibility(clicked: TextView, vararg changing: View) {
        TransitionManager.beginDelayedTransition(itemView.parent as ViewGroup, AutoTransition())

        val visible = changing[0].visibility == View.VISIBLE

        setIcons(visible, clicked)

        val animatedDrawable = TextViewCompat.getCompoundDrawablesRelative(clicked)[2] as AnimatedVectorDrawableCompat

        animatedDrawable.start()

        val visibility = if (visible) View.GONE else View.VISIBLE
        for (view in changing) view.visibility = visibility
    }
}
