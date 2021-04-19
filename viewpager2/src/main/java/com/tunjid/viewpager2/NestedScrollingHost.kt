package com.tunjid.viewpager2

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewParent
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.absoluteValue
import kotlin.math.sign

fun ViewGroup.allowSameDirectionScrollingInViewPager2() =
    setOnTouchListener(NestedScrollableHost(context))

/**
 * Class to scroll a scrollable component inside a ViewPager2. Provided as a solution to the problem
 * where pages of ViewPager2 have nested scrollable elements that scroll in the same direction as
 * ViewPager2.
 *
 * This solution has limitations when using multiple levels of nested scrollable elements
 * (e.g. a horizontal RecyclerView in a vertical RecyclerView in a horizontal ViewPager2).
 */
private class NestedScrollableHost(context: Context) : View.OnTouchListener {

    private var touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var initialX = 0f
    private var initialY = 0f
    private val View.parentViewPager: ViewPager2?
        get() = generateSequence(this as? ViewParent, ViewParent::getParent)
            .filterIsInstance<ViewPager2>()
            .firstOrNull()

    private fun View.canScroll(orientation: Int, delta: Float): Boolean {
        val direction = -delta.sign.toInt()
        return when (orientation) {
            ViewPager2.ORIENTATION_HORIZONTAL -> canScrollHorizontally(direction)
            ViewPager2.ORIENTATION_VERTICAL -> canScrollVertically(direction)
            else -> throw IllegalArgumentException()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, e: MotionEvent): Boolean {
        handleInterceptTouchEvent(view, e)
        return false
    }

    private fun handleInterceptTouchEvent(view: View, e: MotionEvent) {
        val orientation = view.parentViewPager?.orientation ?: return

        // Early return if child can't scroll in same direction as parent
        if (!view.canScroll(orientation, -1f) && !view.canScroll(orientation, 1f)) {
            return
        }

        if (e.action == MotionEvent.ACTION_DOWN) {
            initialX = e.x
            initialY = e.y
            view.parent.requestDisallowInterceptTouchEvent(true)
        } else if (e.action == MotionEvent.ACTION_MOVE) {
            val dx = e.x - initialX
            val dy = e.y - initialY
            val isVpHorizontal = orientation == ViewPager2.ORIENTATION_HORIZONTAL

            // assuming ViewPager2 touch-slop is 2x touch-slop of child
            val scaledDx = dx.absoluteValue * if (isVpHorizontal) .5f else 1f
            val scaledDy = dy.absoluteValue * if (isVpHorizontal) 1f else .5f

            if (scaledDx > touchSlop || scaledDy > touchSlop) {
                if (isVpHorizontal == (scaledDy > scaledDx)) {
                    // Gesture is perpendicular, allow all parents to intercept
                    view.parent.requestDisallowInterceptTouchEvent(false)
                } else {
                    // Gesture is parallel, query child if movement in that direction is possible
                    if (view.canScroll(orientation, if (isVpHorizontal) dx else dy)) {
                        // Child can scroll, disallow all parents to intercept
                        view.parent.requestDisallowInterceptTouchEvent(true)
                    } else {
                        // Child cannot scroll, allow all parents to intercept
                        view.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
            }
        }
    }
}
