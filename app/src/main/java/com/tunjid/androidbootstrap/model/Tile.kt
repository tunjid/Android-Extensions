package com.tunjid.androidbootstrap.model

import android.graphics.Color

import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

import androidx.annotation.ColorInt

class Tile private constructor(
        val number: Int,
        @field:ColorInt @get:ColorInt val color: Int) : Differentiable {

    override fun getId(): String = number.toString()

    override fun areContentsTheSame(other: Differentiable): Boolean =
            if (other !is Tile) false else id == other.id && color == other.color

    override fun getChangePayload(other: Differentiable): Any = other

    override fun toString(): String = id

    companion object {

        private val colors = intArrayOf(Color.RED, Color.BLACK, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.GREEN, Color.LTGRAY, Color.DKGRAY)

        fun generate(id: Int): Tile = Tile(id, colors[randomIndex()])

        private fun randomIndex(): Int = (Math.random() * colors.size).toInt()
    }
}
