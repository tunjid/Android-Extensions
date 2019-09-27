package com.tunjid.androidx.model

import android.graphics.Color

import com.tunjid.androidx.recyclerview.diff.Differentiable

import androidx.annotation.ColorInt

class Tile private constructor(
        val number: Int,
        @field:ColorInt @get:ColorInt val color: Int) : Differentiable {

    override val diffId
        get() = number.toString()

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other !is Tile) false else diffId == other.diffId && color == other.color

    override fun getChangePayload(other: Differentiable): Any = other

    override fun toString(): String = diffId

    companion object {

        private val colors = intArrayOf(Color.RED, Color.BLACK, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.GREEN, Color.LTGRAY, Color.DKGRAY)

        fun generate(id: Int): Tile = Tile(id, colors[randomIndex()])

        private fun randomIndex(): Int = (Math.random() * colors.size).toInt()
    }
}
