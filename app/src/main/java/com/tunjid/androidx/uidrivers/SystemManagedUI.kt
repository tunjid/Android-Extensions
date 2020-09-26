package com.tunjid.androidx.uidrivers

import android.view.WindowInsets

/**
 * Interface for system managed bits of global ui that we react to, but do not explicitly request
 */
interface SystemUI {
    val static: StaticSystemUI
    val dynamic: DynamicSystemUI
}

/**
 * Static global UI, never changes for a configuration lifecycle. If the app is rotated,
 * a new activity will be created with new configurations.
 */
interface StaticSystemUI {
    val statusBarSize: Int
    val navBarSize: Int
}

interface DynamicSystemUI {
    val leftInset: Int
    val topInset: Int
    val rightInset: Int
    val bottomInset: Int
    val snackbarHeight: Int
}

data class DelegateSystemUI(
        override val static: DelegateStaticSystemUI,
        override val dynamic: DelegateDynamicSystemUI
) : SystemUI

data class DelegateStaticSystemUI(
        override val statusBarSize: Int,
        override val navBarSize: Int
) : StaticSystemUI

data class DelegateDynamicSystemUI internal constructor(
        override val leftInset: Int,
        override val topInset: Int,
        override val rightInset: Int,
        override val bottomInset: Int,
        override val snackbarHeight: Int
) : DynamicSystemUI

fun SystemUI.filterNoOp(existing: SystemUI): SystemUI = when {
    // Prioritize existing over no op instances from route changes
    this is NoOpSystemUI && existing !is NoOpSystemUI -> existing
    else -> this
}

fun SystemUI.updateSnackbarHeight(snackbarHeight: Int) = when(this) {
    is DelegateSystemUI -> copy(dynamic = dynamic.copy(snackbarHeight = snackbarHeight))
    else -> this
}

fun UiState.reduceSystemInsets(windowInsets: WindowInsets, navBarHeightThreshold: Int): UiState {
    // Do this once, first call is the size
    val currentSystemUI = systemUI
    val currentStaticSystemUI = currentSystemUI.static

    val updatedStaticUI = when {
        currentStaticSystemUI !is DelegateStaticSystemUI -> DelegateStaticSystemUI(
                statusBarSize = windowInsets.systemWindowInsetTop,
                navBarSize = windowInsets.systemWindowInsetBottom
        )
        windowInsets.systemWindowInsetBottom < navBarHeightThreshold -> DelegateStaticSystemUI(
                statusBarSize = currentStaticSystemUI.statusBarSize,
                navBarSize = windowInsets.systemWindowInsetBottom
        )
        else -> currentStaticSystemUI
    }

    val updatedDynamicUI = DelegateDynamicSystemUI(
            leftInset = windowInsets.systemWindowInsetLeft,
            topInset = windowInsets.systemWindowInsetTop,
            rightInset = windowInsets.systemWindowInsetRight,
            bottomInset = windowInsets.systemWindowInsetBottom,
            snackbarHeight = currentSystemUI.dynamic.snackbarHeight
    )

    return copy(systemUI = DelegateSystemUI(
            static = updatedStaticUI,
            dynamic = updatedDynamicUI
    ))
}

object NoOpSystemUI : SystemUI {
    override val static: StaticSystemUI
        get() = NoOpStaticSystemUI
    override val dynamic: DynamicSystemUI
        get() = NoOpDynamicSystemUI
}

private object NoOpStaticSystemUI : StaticSystemUI {
    override val statusBarSize: Int
        get() = 0
    override val navBarSize: Int
        get() = 0
}

private object NoOpDynamicSystemUI : DynamicSystemUI {
    override val leftInset: Int
        get() = 0
    override val topInset: Int
        get() = 0
    override val bottomInset: Int
        get() = 0
    override val rightInset: Int
        get() = 0
    override val snackbarHeight: Int
        get() = 0
}
