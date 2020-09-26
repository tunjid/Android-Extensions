package com.tunjid.androidx.uidrivers

import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.dynamicanimation.animation.SpringAnimation
import kotlin.reflect.KMutableProperty0

fun KMutableProperty0<UiState>.updatePartial(updater: UiState.() -> UiState) = set(updater.invoke(get()))

data class UiState(
        @MenuRes
        val toolbarMenuRes: Int = 0,
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
        val showsBottomNav: Boolean? = null,
        val insetFlags: InsetDescriptor = InsetFlags.ALL,
        val systemUI: SystemUI = NoOpSystemUI,
        val fabClickListener: (View) -> Unit = emptyCallback(),
        val fabTransitionOptions: SpringAnimation.() -> Unit = emptyCallback(),
        val toolbarMenuClickListener: (MenuItem) -> Unit = emptyCallback(),
        val toolbarMenuRefresher: (Menu) -> Unit = emptyCallback()
)

private fun <T> emptyCallback(): (T) -> Unit = {}

// Internal state slices for memoizing animations.
// They aggregate the parts of Global UI they react to

internal data class ToolbarState(
        val toolbarMenuRes: Int,
        val toolbarTitle: CharSequence,
        val toolbarInvalidated: Boolean
)

internal data class SnackbarPositionalState(
        val bottomNavVisible: Boolean,
        override val bottomInset: Int,
        override val navBarSize: Int,
        override val insetDescriptor: InsetDescriptor
) : KeyboardAware

internal data class FabPositionalState(
        val fabVisible: Boolean,
        val bottomNavVisible: Boolean,
        val hasSnackbar: Boolean,
        val snackbarHeight: Int,
        override val bottomInset: Int,
        override val navBarSize: Int,
        override val insetDescriptor: InsetDescriptor
) : KeyboardAware

internal data class FragmentContainerPositionalState(
        val statusBarSize: Int,
        val toolbarOverlaps: Boolean,
        val bottomNavVisible: Boolean,
        override val bottomInset: Int,
        override val navBarSize: Int,
        override val insetDescriptor: InsetDescriptor
) : KeyboardAware

internal data class BottomNavPositionalState(
        val insetDescriptor: InsetDescriptor,
        val bottomNavVisible: Boolean,
        val navBarSize: Int
)

internal val UiState.toolbarState
        get() = ToolbarState(
                toolbarMenuRes = toolbarMenuRes,
                toolbarTitle = toolbarTitle,
                toolbarInvalidated = toolbarInvalidated
        )

internal val UiState.fabState
        get() = FabPositionalState(
                fabVisible = fabShows,
                snackbarHeight = systemUI.dynamic.snackbarHeight,
                bottomNavVisible = showsBottomNav == true,
                hasSnackbar = snackbarText.isNotBlank() && snackbarText.isNotEmpty(),
                bottomInset = systemUI.dynamic.bottomInset,
                navBarSize = systemUI.static.navBarSize,
                insetDescriptor = insetFlags
        )

internal val UiState.snackbarPositionalState
        get() = SnackbarPositionalState(
                bottomNavVisible = showsBottomNav == true,
                bottomInset = systemUI.dynamic.bottomInset,
                navBarSize = systemUI.static.navBarSize,
                insetDescriptor = insetFlags
        )

internal val UiState.fabGlyphs
        get() = fabIcon to fabText

internal val UiState.toolbarPosition
        get() = systemUI.static.statusBarSize


internal val UiState.bottomNavPositionalState
        get() = BottomNavPositionalState(
                bottomNavVisible = showsBottomNav == true,
                navBarSize = systemUI.static.navBarSize,
                insetDescriptor = insetFlags
        )

internal val UiState.fragmentContainerState
        get() = FragmentContainerPositionalState(
                statusBarSize = systemUI.dynamic.topInset,
                insetDescriptor = insetFlags,
                toolbarOverlaps = toolbarOverlaps,
                bottomNavVisible = showsBottomNav == true,
                bottomInset = systemUI.dynamic.bottomInset,
                navBarSize = systemUI.static.navBarSize
        )

/**
 * Interface for [UiState] state slices that are aware of the keyboard. Useful for
 * keyboard visibility changes for bottom aligned views like Floating Action Buttons and Snack Bars
 */
interface KeyboardAware {
        val bottomInset: Int
        val navBarSize: Int
        val insetDescriptor: InsetDescriptor
}

internal val KeyboardAware.keyboardSize get() = bottomInset - navBarSize