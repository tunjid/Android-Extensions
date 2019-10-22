package com.tunjid.androidx

import android.graphics.Color

object MutedColors {
   private val colors = intArrayOf(
            Color.parseColor("#2c3e50"), // Midnight Blue
            Color.parseColor("#c0392b"), // Pomegranate
            Color.parseColor("#2980b9"), // Belize Hole
            Color.parseColor("#16a085"), // Green Sea
            Color.parseColor("#7f8c8d") // Concrete
    )

    fun atIndex(index: Int) = colors[index % colors.size]

    fun randomColor(): Int = colors[(Math.random() * colors.size).toInt()]
}

