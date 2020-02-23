package com.tunjid.androidx.recyclerview.multiscroll

import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.recyclerview.R
import com.tunjid.androidx.recyclerview.notifyItemChanged
import kotlin.math.max

@UseExperimental(ExperimentalRecyclerViewMultiScrolling::class)
class DynamicSizer(
        @RecyclerView.Orientation override val orientation: Int = RecyclerView.HORIZONTAL
) : Sizer {

    private val columnSizeMap = mutableMapOf<Int, Int>()
    private val syncedScrollers = mutableSetOf<RecyclerView>()

    private val onChildAttachStateChangeListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewDetachedFromWindow(view: View) = excludeChild(view)

        override fun onChildViewAttachedToWindow(view: View) = includeChild(view)
    }

    override fun sizeAt(position: Int): Int = columnSizeMap[position] ?: -1

    override fun include(recyclerView: RecyclerView) {
        syncedScrollers.add(recyclerView)
        recyclerView.addOnChildAttachStateChangeListener(onChildAttachStateChangeListener)
        recyclerView.children.forEach { includeChild(it) }
    }

    override fun exclude(recyclerView: RecyclerView) {
        syncedScrollers.remove(recyclerView)
        recyclerView.removeOnChildAttachStateChangeListener(onChildAttachStateChangeListener)
        recyclerView.children.forEach { excludeChild(it) }
    }

    override fun positionAndOffsetForDisplacement(displacement: Int): Pair<Int, Int> {
        var offset = displacement
        var position = 0
        while (offset > 0) {
            val sizeAtPosition = columnSizeMap[position]
            offset -= (sizeAtPosition ?: -1)
            if (sizeAtPosition != null) position++
        }

        Log.i("TEST", "Syncing. position: $position; offset: $offset")

        return position to offset
    }

    private fun includeChild(it: View) {
        it.setSizer()
        val lastSize = columnSizeMap[it.currentColumn] ?: return
        it.adjustMinSize(lastSize)
    }

    private fun excludeChild(it: View) {
        it.wasDetached = true
        it.removeSizer()
    }

    private fun View.adjustMinSize(newMaxSize: Int) {
        if (size == newMaxSize || currentColumn < 0) return

        layoutSize = newMaxSize
        parentRecyclerView?.apply {
            if (isComputingLayout) post { notifyItemChanged(currentColumn) }
            else notifyItemChanged(currentColumn)
        }
    }

    private fun View.setSizer() {
        val existing = getTag(R.id.recyclerview_pre_draw) as? ViewTreeObserver.OnPreDrawListener
        if (existing != null) return

        val view = this@setSizer

        val listener = ViewTreeObserver.OnPreDrawListener {
            val recyclerView = parentRecyclerView ?: return@OnPreDrawListener true

            val column = view.currentColumn
            val currentSize = if (view.wasDetached) 0 else view.size
            val oldMaxSize = columnSizeMap[column] ?: 0
            val newMaxSize = max(oldMaxSize, currentSize)
            if (oldMaxSize != newMaxSize && column == 0) {
                Log.i("TEST", "UMM WTF. current: $currentSize; oldMaxSize: $oldMaxSize; new: $newMaxSize")
            }
            columnSizeMap[column] = newMaxSize

            if (view.wasDetached && view.previousColumn != column) {
                Log.i("TEST", "previous column: ${view.previousColumn}; current column: $column")
            }

            view.previousColumn = column

//        Log.i("TEST", "row: $row; column: $column; current: $currentSize; oldMaxSize: $oldMaxSize; newMaxSize: $newMaxSize")
//        Log.i("TEST", "why: $why")
//                Log.i("TEST", "Changing. column: $column; current: $currentSize; oldMaxSize: $oldMaxSize; new: $newMaxSize")

            if (currentSize != newMaxSize) view.adjustMinSize(newMaxSize)

            if (oldMaxSize != newMaxSize) for (it in syncedScrollers) {
                if (it == recyclerView) continue
                it.childIn(column)?.adjustMinSize(newMaxSize)
            }

            true
        }

        viewTreeObserver.addOnPreDrawListener(listener)
        setTag(R.id.recyclerview_pre_draw, listener)
    }

    private fun View.removeSizer() {
        val listener = getTag(R.id.recyclerview_pre_draw) as? ViewTreeObserver.OnPreDrawListener
                ?: return
        viewTreeObserver.removeOnPreDrawListener(listener)
        setTag(R.id.recyclerview_pre_draw, null)
    }

    private val View.size get() = if (orientation == RecyclerView.HORIZONTAL) width else height

//    private var View.layoutSize: Int
//        get() =
//            if (orientation == RecyclerView.HORIZONTAL) layoutParams.width
//            else layoutParams.height
//        set(value) =
//            if (orientation == RecyclerView.HORIZONTAL) layoutParams.width = value
//            else layoutParams.height = value

    private fun RecyclerView.childIn(column: Int): View? {
        return this.findViewHolderForAdapterPosition(column)?.itemView
    }

    private val View.parentRecyclerView: RecyclerView?
        get() = parent as? RecyclerView

    private var View.layoutSize: Int
        get() =
            if (orientation == RecyclerView.HORIZONTAL) minimumWidth
            else minimumHeight
        set(value) =
            if (orientation == RecyclerView.HORIZONTAL) minimumWidth = value
            else minimumHeight = value

    private var View.wasDetached: Boolean
        get() = getTag(R.id.recyclerview_was_detached) as? Boolean ?: false
        set(value) = setTag(R.id.recyclerview_was_detached, value)

    private var View.previousColumn: Int
        get() = getTag(R.id.recyclerview_previous_column) as? Int ?: -1
        set(value) = setTag(R.id.recyclerview_previous_column, value)
}