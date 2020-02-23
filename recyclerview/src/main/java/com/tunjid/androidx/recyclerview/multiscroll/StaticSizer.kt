package com.tunjid.androidx.recyclerview.multiscroll

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.recyclerview.R

@UseExperimental(ExperimentalRecyclerViewMultiScrolling::class)
class StaticSizer(
        private val sizeLookup: (Int) -> Int
) : RecyclerViewMultiScroller.Sizer {

    private var appContext: Context? = null

    override fun sizeAt(position: Int): Int = sizeLookup(position)

    override fun include(recyclerView: RecyclerView) {
        appContext = recyclerView.context.applicationContext
    }

    override fun exclude(recyclerView: RecyclerView) = Unit

    override fun positionAndOffsetForDisplacement(displacement: Int): Pair<Int, Int> {
        var offset = displacement
        var position = 0
        while (offset > 0) {
            val sizeAtPosition = appContext?.resources?.getDimensionPixelSize(R.dimen.sexdecuple_margin)
            offset -= (sizeAtPosition ?: -1)
            if (sizeAtPosition != null) position++
        }

        Log.i("TEST", "Syncing. position: $position; offset: $offset")

        return position to offset
    }
}