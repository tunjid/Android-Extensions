package com.tunjid.androidx.uidrivers

import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.dynamicanimation.animation.SpringAnimation
import com.tunjid.androidx.view.util.InsetFlags
import kotlin.reflect.KMutableProperty0

fun KMutableProperty0<UiState>.update(updater: UiState.() -> UiState) = set(updater.invoke(get()))

typealias PositionalState = List<Any>
typealias ToolbarState = Triple<Int, Boolean, CharSequence>
typealias FabState = Pair<Int, CharSequence>

val UiState.positionState: PositionalState get() = listOf(insetFlags, showsBottomNav, fabShows, snackbarText, toolbarOverlaps)
val UiState.toolbarState get() = ToolbarState(toolBarMenu, toolbarInvalidated, toolbarTitle)
val UiState.fabState get() = FabState(fabIcon, fabText)

data class UiState(
        @MenuRes
        val toolBarMenu: Int = 0,
        val toolbarShows: Boolean = false,
        val toolbarOverlaps: Boolean = false,
        val toolbarInvalidated: Boolean = false,
        val toolbarTitle: CharSequence = "",
        @DrawableRes
        val fabIcon: Int = 0,
        val fabShows: Boolean = false,
        val fabExtended: Boolean = true,
        val fabText: CharSequence = "",
        @ColorInt
        val backgroundColor: Int = Color.TRANSPARENT,
        val snackbarText: CharSequence = "",
        @ColorInt
        val navBarColor: Int = Color.BLACK,
        val lightStatusBar: Boolean = false,
        val showsBottomNav: Boolean = false,
        val insetFlags: InsetFlags = InsetFlags.ALL,
        val fabClickListener: (View) -> Unit = emptyCallback(),
        val fabTransitionOptions: SpringAnimation.() -> Unit = emptyCallback(),
        val toolbarMenuClickListener: (MenuItem) -> Unit = emptyCallback(),
        val toolbarMenuRefresher: (Menu) -> Unit = emptyCallback()
)

private fun <T> emptyCallback(): (T) -> Unit = {}