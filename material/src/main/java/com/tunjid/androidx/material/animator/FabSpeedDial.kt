package com.tunjid.androidx.material.animator

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.RELATIVE_TO_PARENT
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.AnimationSet
import android.view.animation.LayoutAnimationController
import android.view.animation.LayoutAnimationController.ORDER_NORMAL
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.material.R
import com.tunjid.androidx.view.util.popOver

fun speedDial(
        anchor: View,
        @ColorInt tint: Int = anchor.context.themeColorAt(R.attr.colorPrimary),
        @StyleRes animationStyle: Int = android.R.style.Animation_Dialog,
        items: List<Pair<CharSequence?, Drawable>>,
        dismissListener: (Int?) -> Unit
) = LinearLayout(anchor.context).run root@{
    rotationY = MIRROR
    rotationX = MIRROR
    clipChildren = false
    clipToPadding = false
    orientation = VERTICAL
    layoutAnimation = LayoutAnimationController(speedDialAnimation, INITIAL_DELAY).apply { order = ORDER_NORMAL }

    popOver(anchor = anchor, adjuster = getOffset(anchor)) popUp@{
        this.animationStyle = animationStyle

        var dismissReason: Int? = null
        setOnDismissListener { dismissListener(dismissReason) }

        items.mapIndexed { index, pair ->  speedDialLayout(pair, tint, View.OnClickListener { dismissReason = index; dismiss() }) }
                .forEach(this@root::addView)
    }
}

private fun LinearLayout.speedDialLayout(pair: Pair<CharSequence?, Drawable>, tint: Int, clickListener: View.OnClickListener) = LinearLayout(context).apply {
    rotationY = MIRROR
    rotationX = MIRROR
    clipChildren = false
    clipToPadding = false
    layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

    updatePadding(bottom = context.resources.getDimensionPixelSize(R.dimen.single_margin))
    setOnClickListener(clickListener)

    addView(speedDialLabel(tint, pair.first, clickListener))
    addView(speedDialFab(tint, pair, clickListener))
}

private fun LinearLayout.speedDialLabel(tint: Int, label: CharSequence?, clicker: View.OnClickListener) = AppCompatTextView(context).apply {
    val dp4 = context.resources.getDimensionPixelSize(R.dimen.quarter_margin)
    val dp8 = context.resources.getDimensionPixelSize(R.dimen.half_margin)

    isClickable = true
    background = context.ripple(tint) { setAllCornerSizes(dp8.toFloat()) }

    isVisible = label != null
    text = label

    layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
        marginEnd = context.resources.getDimensionPixelSize(R.dimen.single_margin)
        gravity = Gravity.CENTER_VERTICAL
    }

    updatePadding(left = dp8, top = dp4, right = dp8, bottom = dp4)
    setOnClickListener(clicker)
}

private fun LinearLayout.speedDialFab(tint: Int, pair: Pair<CharSequence?, Drawable>, clicker: View.OnClickListener) = AppCompatImageButton(context).apply {
    val dp40 = context.resources.getDimensionPixelSize(R.dimen.double_and_half_margin)

    imageTintList = null
    background = context.ripple(tint) { setAllCornerSizes(dp40.toFloat()) }
    layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
        gravity = Gravity.CENTER_VERTICAL
        height = dp40
        width = dp40
    }

    setOnClickListener(clicker)
    setImageDrawable(if (pair.second !is BitmapDrawable) BitmapDrawable(context.resources, pair.second.toBitmap()) else pair.second)
}

private fun View.getOffset(anchor: View): () -> Point = {
    val dp40 = context.resources.getDimensionPixelSize(R.dimen.double_and_half_margin)
    val halfAnchorWidth = (anchor.width / 2)
    val halfMiniFabWidth = (dp40 / 2)
    val xOffset = if (width > anchor.width) halfAnchorWidth + halfMiniFabWidth - width else halfAnchorWidth - halfMiniFabWidth

    Point(xOffset, -(anchor.height / 2) - height)
}

private fun Context.ripple(tint: Int, shapeModifier: ShapeAppearanceModel.Builder.() -> Unit): RippleDrawable = RippleDrawable(
        ColorStateList.valueOf(translucentBlack),
        MaterialShapeDrawable(ShapeAppearanceModel.builder().run {
            shapeModifier(this)
            build()
        }).apply {
            tintList = ColorStateList.valueOf(tint)
            setShadowColor(Color.DKGRAY)
            initializeElevationOverlay(this@ripple)
        },
        null
)

private val speedDialAnimation: Animation
    get() = AnimationSet(false).apply {
        duration = 200L
        addAnimation(alpha())
        addAnimation(scale())
        addAnimation(translate())
    }

private fun alpha() = AlphaAnimation(0F, 1F).accelerateDecelerate()

private fun translate(): Animation = TranslateAnimation(
        RELATIVE_TO_PARENT,
        0F,
        RELATIVE_TO_PARENT,
        0F,
        RELATIVE_TO_PARENT,
        SPEED_DIAL_TRANSLATION_Y,
        RELATIVE_TO_PARENT,
        0F
).accelerateDecelerate()

private fun scale(): Animation = ScaleAnimation(
        SPEED_DIAL_SCALE,
        1F,
        SPEED_DIAL_SCALE,
        1F,
        RELATIVE_TO_SELF,
        SPEED_DIAL_SCALE_PIVOT,
        RELATIVE_TO_SELF,
        SPEED_DIAL_SCALE_PIVOT
).accelerateDecelerate()

private const val SPEED_DIAL_TRANSLATION_Y = -0.2F
private const val SPEED_DIAL_SCALE_PIVOT = 0.5F
private const val SPEED_DIAL_SCALE = 0.5F
private const val INITIAL_DELAY = 0.15F
private const val MIRROR = 180F

private val translucentBlack = Color.argb(50, 0, 0, 0)

private fun Animation.accelerateDecelerate() = apply { interpolator = AccelerateDecelerateInterpolator() }
