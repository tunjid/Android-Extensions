package com.tunjid.androidbootstrap.viewholders

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.model.Tile
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder

class TileViewHolder(itemView: View, scanAdapterListener: (tile: Tile) -> Unit)
    : InteractiveViewHolder<(tile: Tile) -> Unit>(itemView, scanAdapterListener) {

    private lateinit var tile: Tile
    private val text: TextView = itemView.findViewById(R.id.tile_text)
    private val animator: ValueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), Color.RED)
    private val listener: ValueAnimator.AnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener(this::updateTextColor)

    init {
        animator.duration = COLOR_CHANGE_DURATION.toLong()
        itemView.setOnClickListener { delegate.invoke(tile) }
    }

    fun bind(tile: Tile) {
        this.tile = tile
        text.text = tile.id

        animator.setIntValues(text.currentTextColor, tile.color)
        animator.addUpdateListener(listener)
        animator.startDelay = START_DELAY.toLong() // Cheeky bit of code to keep scrolling smooth on fling
        animator.start()
    }

    fun unBind() {
        animator.cancel()
        animator.removeUpdateListener(listener)
    }

    private fun updateTextColor(animation: ValueAnimator) {
        text.setTextColor(animation.animatedValue as Int)
    }

    companion object {

        private const val COLOR_CHANGE_DURATION = 1000
        private const val START_DELAY = 300
    }
}
