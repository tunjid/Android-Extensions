package com.tunjid.androidx

import android.graphics.Color

object MutedColors {
    private val mutedColors = intArrayOf(
        Color.parseColor("#2980b9"), // Belize Hole
        Color.parseColor("#2c3e50"), // Midnight Blue
        Color.parseColor("#c0392b"), // Pomegranate
        Color.parseColor("#16a085"), // Green Sea
        Color.parseColor("#7f8c8d") // Concrete
    )

    private val darkerMutedColors = intArrayOf(
        Color.parseColor("#304233"),
        Color.parseColor("#353b45"),
        Color.parseColor("#392e3a"),
        Color.parseColor("#3e2a2a"),
        Color.parseColor("#474747")
    )

    fun colorAt(isDark: Boolean, index: Int) = palette(isDark).circular(index)

    fun random(isDark: Boolean): Int = palette(isDark).random()

    private fun palette(isDark: Boolean): IntArray = when (isDark) {
        true -> darkerMutedColors
        else -> mutedColors
    }
}

private fun IntArray.circular(index: Int) = this[index % size]

private fun IntArray.random() = this[(Math.random() * size).toInt()]