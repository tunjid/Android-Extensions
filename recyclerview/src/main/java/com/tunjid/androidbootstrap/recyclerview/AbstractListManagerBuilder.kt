package com.tunjid.androidbootstrap.recyclerview

import android.util.Log
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tunjid.androidbootstrap.recyclerview.ListManager.Companion.TAG
import java.util.*
import kotlin.math.max

/**
 * Abstract thisInstance class for creating [scrollmanagers][ListManager]
 *
 *
 * The breakdown for the generic types are as follows:
 *
 *
 * B: The implicit type of the Builder. This is only necessary to make the return type for each
 * thisInstance method be the type of any inheriting class, and not this base class. Otherwise inheritors of
 * this subclass will need to override each method here to return their custom thisInstance type to
 * maintain the fluency of the API which is fairly tedious.
 *
 *
 * S: The type of the [ListManager] to be built.
 *
 *
 * VH: The [ViewHolder] type in the [RecyclerView]
 *
 *
 * T: The type bound in the [ListPlaceholder]
 */
@Suppress("unused")
abstract class AbstractListManagerBuilder<B : AbstractListManagerBuilder<B, S, VH, T>, S : ListManager<VH, T>, VH : ViewHolder, T> {

    protected var spanCount: Int = 0
    protected var layoutManagerType: Int = 0
    protected var endlessScrollVisibleThreshold: Int = 0
    protected var hasFixedSize: Boolean = false

    protected var placeholder: ListPlaceholder<T>? = null
    protected var refreshLayout: SwipeRefreshLayout? = null

    protected var recyclerView: RecyclerView? = null
    protected var adapter: RecyclerView.Adapter<out VH>? = null
    protected var customLayoutManager: RecyclerView.LayoutManager? = null

    protected var swipeDragOptions: SwipeDragOptions<VH>? = null
    protected var recycledViewPool: RecyclerView.RecycledViewPool? = null

    protected var endlessScrollConsumer: ((Int) -> Unit)? = null
    protected var layoutManagerConsumer: ((RecyclerView.LayoutManager) -> Unit)? = null
    protected var handler: ((IndexOutOfBoundsException) -> Unit)? = null

    protected var itemDecorations: MutableList<RecyclerView.ItemDecoration> = ArrayList()
    protected var stateConsumers: MutableList<(Int) -> Unit> = ArrayList()
    protected var displacementConsumers: MutableList<(Int, Int) -> Unit> = ArrayList()

    @Suppress("UNCHECKED_CAST")
    protected val thisInstance: B = this as B

    fun setHasFixedSize(): B = thisInstance.apply { this.hasFixedSize = true }

    fun withRecyclerView(recyclerView: RecyclerView): B = thisInstance.apply { this.recyclerView = recyclerView }

    fun withAdapter(adapter: RecyclerView.Adapter<out VH>): B = thisInstance.apply { this.adapter = adapter }

    fun onLayoutManager(layoutManagerConsumer: (RecyclerView.LayoutManager) -> Unit): B = thisInstance.apply { this.layoutManagerConsumer = layoutManagerConsumer }

    fun withLinearLayoutManager(): B = thisInstance.apply { layoutManagerType = LINEAR_LAYOUT_MANAGER }

    fun withGridLayoutManager(spanCount: Int): B {
        layoutManagerType = GRID_LAYOUT_MANAGER
        this.spanCount = spanCount
        return thisInstance
    }

    fun withStaggeredGridLayoutManager(spanCount: Int): B {
        layoutManagerType = STAGGERED_GRID_LAYOUT_MANAGER
        this.spanCount = spanCount
        return thisInstance
    }

    fun withCustomLayoutManager(layoutManager: RecyclerView.LayoutManager): B = thisInstance.apply { this.customLayoutManager = layoutManager }

    fun withRecycledViewPool(recycledViewPool: RecyclerView.RecycledViewPool): B = thisInstance.apply { this.recycledViewPool = recycledViewPool }

    fun withInconsistencyHandler(handler: (IndexOutOfBoundsException) -> Unit): B = thisInstance.apply { this.handler = handler }

    fun withEndlessScrollCallback(threshold: Int, endlessScrollConsumer: (Int) -> Unit): B {
        this.endlessScrollVisibleThreshold = threshold
        this.endlessScrollConsumer = endlessScrollConsumer
        return thisInstance
    }

    fun addStateListener(stateListener: (Int) -> Unit): B = thisInstance.apply { this.stateConsumers.add(stateListener) }

    fun addScrollListener(scrollListener: (Int, Int) -> Unit): B = thisInstance.apply { this.displacementConsumers.add(scrollListener) }

    fun addDecoration(decoration: RecyclerView.ItemDecoration): B = thisInstance.apply { this.itemDecorations.add(decoration) }

    fun withRefreshLayout(refreshLayout: SwipeRefreshLayout, refreshAction: () -> Unit): B {
        this.refreshLayout = refreshLayout
        refreshLayout.setOnRefreshListener { refreshAction() }
        return thisInstance
    }

    fun withPlaceholder(placeholder: ListPlaceholder<T>): B = thisInstance.apply { this.placeholder = placeholder }

    fun withSwipeDragOptions(swipeDragOptions: SwipeDragOptions<VH>): B = thisInstance.apply { this.swipeDragOptions = swipeDragOptions }

    abstract fun build(): S

    protected fun buildEndlessScroller(layoutManager: RecyclerView.LayoutManager): EndlessScroller? =
            if (endlessScrollConsumer == null) null
            else object : EndlessScroller(endlessScrollVisibleThreshold, layoutManager) {
                override fun onLoadMore(currentItemCount: Int) {
                    endlessScrollConsumer?.invoke(currentItemCount)
                }
            }

    @CallSuper
    protected fun buildScrollListeners(): List<RecyclerView.OnScrollListener> {
        val stateConsumersSize = stateConsumers.size
        val scrollConsumersSize = displacementConsumers.size
        val max = max(stateConsumersSize, scrollConsumersSize)

        val scrollListeners = ArrayList<RecyclerView.OnScrollListener>(max)

        for (i in 0 until max) {

            val consumer: ((Int) -> Unit)? =
                    if (i < stateConsumersSize) stateConsumers[i]
                    else null

            val biConsumer: ((Int, Int) -> Unit)? =
                    if (i < scrollConsumersSize) displacementConsumers[i]
                    else null

            scrollListeners.add(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    biConsumer?.invoke(dx, dy)
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    consumer?.invoke(newState)
                }
            })
        }

        stateConsumers.clear()
        displacementConsumers.clear()

        return scrollListeners
    }

    protected fun buildLayoutManager(): RecyclerView.LayoutManager {
        val context = recyclerView?.context
                ?: throw IllegalArgumentException("RecyclerView not provided")
        val layoutManager: RecyclerView.LayoutManager = when (layoutManagerType) {
            STAGGERED_GRID_LAYOUT_MANAGER -> object : StaggeredGridLayoutManager(spanCount, VERTICAL) {
                override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) =
                        handleLayout { super.onLayoutChildren(recycler, state) }
            }
            GRID_LAYOUT_MANAGER -> object : GridLayoutManager(context, spanCount) {
                override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) =
                        handleLayout { super.onLayoutChildren(recycler, state) }
            }
            LINEAR_LAYOUT_MANAGER -> object : LinearLayoutManager(context) {
                override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) =
                        handleLayout { super.onLayoutChildren(recycler, state) }
            }
            else -> customLayoutManager
                    ?: throw IllegalArgumentException("LayoutManager must be provided")
        }

        if (handler == null)
            Log.w(TAG, "InconsistencyHandler is not provided, " + "inconsistencies in the RecyclerView adapter will cause crashes at runtime")

        layoutManagerConsumer?.invoke(layoutManager)

        return layoutManager
    }

    private fun RecyclerView.LayoutManager.handleLayout(superCall: () -> Unit) {
        try {
            superCall()
        } catch (e: IndexOutOfBoundsException) {
            handler?.invoke(e) ?: throw e
        }
    }

    companion object {

        private const val LINEAR_LAYOUT_MANAGER = 1
        private const val GRID_LAYOUT_MANAGER = 2
        private const val STAGGERED_GRID_LAYOUT_MANAGER = 3
    }
}
