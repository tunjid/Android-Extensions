package com.tunjid.androidx.model

import androidx.annotation.ColorInt
import com.tunjid.androidx.MutedColors
import com.tunjid.androidx.recyclerview.diff.Differentiable

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

        fun generate(id: Int): Tile = Tile(id, MutedColors.random(false))

    }
}
