package com.tunjid.androidx.uidrivers

import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat

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
    val statusBars: Insets
    val navBars: Insets
    val cutouts: Insets
    val captions: Insets
    val ime: Insets
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
    override val statusBars: Insets,
    override val navBars: Insets,
    override val cutouts: Insets,
    override val captions: Insets,
    override val ime: Insets,
    override val snackbarHeight: Int
) : DynamicSystemUI

fun SystemUI.filterNoOp(existing: SystemUI): SystemUI = when {
    // Prioritize existing over no op instances from route changes
    this is NoOpSystemUI && existing !is NoOpSystemUI -> existing
    else -> this
}

fun SystemUI.updateSnackbarHeight(snackbarHeight: Int) = when (this) {
    is DelegateSystemUI -> copy(dynamic = dynamic.copy(snackbarHeight = snackbarHeight))
    else -> this
}

fun UiState.reduceSystemInsets(windowInsets: WindowInsetsCompat, navBarHeightThreshold: Int): UiState {
    // Do this once, first call is the size
    val currentSystemUI = systemUI
    val currentStaticSystemUI = currentSystemUI.static

    val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
    val navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
    val cutouts = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
    val captions = windowInsets.getInsets(WindowInsetsCompat.Type.captionBar())
    val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

    val updatedStaticUI = when {
        currentStaticSystemUI !is DelegateStaticSystemUI -> DelegateStaticSystemUI(
            statusBarSize = statusBars.top,
            navBarSize = navBars.bottom
        )
        navBars.bottom < navBarHeightThreshold -> DelegateStaticSystemUI(
            statusBarSize = currentStaticSystemUI.statusBarSize,
            navBarSize = navBars.bottom
        )
        else -> currentStaticSystemUI
    }

    val updatedDynamicUI = DelegateDynamicSystemUI(
        statusBars = statusBars,
        navBars = navBars,
        cutouts = cutouts,
        captions = captions,
        ime = ime,
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
    override val statusBars: Insets
        get() = emptyInsets
    override val navBars: Insets
        get() = emptyInsets
    override val cutouts: Insets
        get() = emptyInsets
    override val captions: Insets
        get() = emptyInsets
    override val ime: Insets
        get() = emptyInsets
    override val snackbarHeight: Int
        get() = 0
}

private val emptyInsets = Insets.of(0, 0, 0, 0)