package com.tunjid.androidx.constraintlayout.animator


import android.database.DataSetObserver
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnAdapterChangeListener
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import java.util.*
import java.util.Objects.requireNonNull

typealias IndicatorWatcher = (indicator: ImageView, position: Int, fraction: Float, totalTranslation: Float) -> Unit

open class ViewPagerIndicatorAnimator constructor(
    private val indicatorWidth: Int = 0,
    private val indicatorHeight: Int = 0,
    private val indicatorPadding: Int = 0,
    @DrawableRes activeDrawable: Int,
    @param:DrawableRes @field:DrawableRes
    private val inActiveDrawable: Int,
    @DrawableRes backgroundDrawable: Int = 0,
    private val container: ConstraintLayout,
    private val viewPager: ViewPager,
    private val guide: View
) {

    private var chainIds: IntArray? = null
    private var indicatorCount: Int = 0
    private var guideLineWidth: Int = 0
    val indicator: ImageView

    private val animator: Animator

    private val watchers: MutableList<IndicatorWatcher>

    init {

        this.indicator = buildIndicator()
        this.animator = Animator()
        this.watchers = ArrayList()

        val adapter = viewPager.adapter

        if (adapter != null) buildIndicators(adapter)
        val context = viewPager.context

        if (backgroundDrawable != 0)
            guide.background = ContextCompat.getDrawable(context, backgroundDrawable)
        if (activeDrawable != 0) this.indicator.setImageResource(activeDrawable)
    }

    fun getIndicatorAt(index: Int): ImageView =
        if (index < 0 || chainIds == null || index > chainIds!!.size - 1) indicator
        else container.findViewById(chainIds!![index])

    fun addIndicatorWatcher(watcher: IndicatorWatcher) {
        if (!watchers.contains(watcher)) watchers.add(watcher)
    }

    fun removeIndicatorWatcher(watcher: IndicatorWatcher) {
        watchers.remove(watcher)
    }

    private fun buildIndicator(): ImageView {
        val result = ImageView(this.container.context)
        result.id = View.generateViewId()

        val params = LayoutParams(indicatorWidth, indicatorHeight)
        params.leftMargin = indicatorPadding
        params.rightMargin = params.leftMargin
        params.bottomToBottom = guide.id
        container.addView(result, params)
        return result
    }

    private fun buildIndicators(newAdapter: PagerAdapter?) {
        if (newAdapter == null) return

        val pageCount = newAdapter.count

        if (this.container.childCount > MIN_VIEWS_IN_LAYOUT) {
            this.container.removeViews(MIN_VIEWS_IN_LAYOUT, this.indicatorCount)
        }
        if (pageCount < MIN_INDICATORS_TO_SHOW) {
            this.indicator.visibility = View.GONE
            return
        }

        val chainIds = IntArray(pageCount)
        this.chainIds = chainIds

        indicatorCount = 0
        indicator.visibility = View.VISIBLE

        while (indicatorCount < pageCount) {
            val imageView = buildIndicator()
            chainIds[this.indicatorCount] = imageView.id
            imageView.setImageResource(this.inActiveDrawable)
            this.indicatorCount++
        }

        val guideId = this.guide.id
        val indicatorId = this.indicator.id

        guide.layoutParams.width = pageCount * (this.indicatorWidth + this.indicatorPadding * 2)

        val set = ConstraintSet()
        set.clone(container)
        set.createHorizontalChain(guideId, ConstraintSet.LEFT, guideId, ConstraintSet.RIGHT, chainIds, null, 2)
        set.connect(indicatorId, ConstraintSet.LEFT, guideId, ConstraintSet.LEFT)
        set.connect(chainIds[0], ConstraintSet.LEFT, guideId, ConstraintSet.LEFT)
        set.connect(chainIds[pageCount - 1], ConstraintSet.RIGHT, guideId, ConstraintSet.RIGHT)
        set.applyTo(container)
        container.bringChildToFront(indicator)

        val observer = guide.viewTreeObserver
        if (observer.isAlive) {
            observer.addOnGlobalLayoutListener(animator)
            guide.invalidate()
        }
    }

    private inner class Animator :
        DataSetObserver(),
        OnPageChangeListener,
        OnAdapterChangeListener,
        OnGlobalLayoutListener {

        private var lastPositionOffset: Float = 0.toFloat()

        init {
            val adapter = viewPager.adapter
            adapter?.registerDataSetObserver(this)

            viewPager.addOnPageChangeListener(this)
            viewPager.addOnAdapterChangeListener(this)
        }

        override fun onAdapterChanged(viewPager: ViewPager, oldAdapter: PagerAdapter?, newAdapter: PagerAdapter?) {
            if (newAdapter == null) return
            newAdapter.registerDataSetObserver(this)
            buildIndicators(newAdapter)
        }

        override fun onPageScrolled(position: Int, fraction: Float, pixelOffset: Int) {
            val currentPositionOffset = position.toFloat() + fraction
            val toTheRight = currentPositionOffset > lastPositionOffset

            onMoved(toTheRight, if (toTheRight) position else position + 1, fraction)
            lastPositionOffset = currentPositionOffset
        }

        override fun onGlobalLayout() {
            val observer = guide.viewTreeObserver
            if (observer.isAlive) observer.removeOnGlobalLayoutListener(this)

            guideLineWidth = guide.width
            onMoved(false, viewPager.currentItem + 1, 0.0f)
        }

        private fun getTranslation(toTheRight: Boolean, originalPosition: Int, fraction: Float): Float {
            var fraction = fraction
            if (!toTheRight) fraction = 1.0f - fraction

            val chunkWidth = guideLineWidth.toFloat() / requireNonNull<PagerAdapter>(viewPager.adapter).count
            val currentChunk = originalPosition.toFloat() * chunkWidth
            val diff = chunkWidth * fraction

            return if (toTheRight) currentChunk + diff else currentChunk - diff
        }

        private fun onMoved(toTheRight: Boolean, position: Int, fraction: Float) {
            val translation = getTranslation(toTheRight, position, fraction)
            indicator.translationX = translation
            for (watcher in watchers.asReversed()) {
                watcher(indicator, position, fraction, translation)
            }
        }

        override fun onPageSelected(position: Int) {}

        override fun onPageScrollStateChanged(state: Int) {}
    }

    companion object {

        private const val MIN_INDICATORS_TO_SHOW = 2
        private const val MIN_VIEWS_IN_LAYOUT = 3
    }
}
