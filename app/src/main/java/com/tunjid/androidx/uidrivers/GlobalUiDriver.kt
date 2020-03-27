package com.tunjid.androidx.uidrivers

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.graphics.drawable.withTint
import com.tunjid.androidx.core.text.color
import com.tunjid.androidx.material.animator.FabExtensionAnimator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.view.animator.ViewHider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * An interface for classes that host a [UiState], usually a [FragmentActivity].
 * Implementations should delegate to an instance of [GlobalUiDriver]
 */
interface GlobalUiController {
    var uiState: UiState
}

/**
 * Convenience method for [FragmentActivity] delegation to a [GlobalUiDriver] when implementing
 * [GlobalUiController]
 */
fun FragmentActivity.globalUiDriver(
        toolbarId: Int = R.id.toolbar,
        fabId: Int = R.id.fab,
        bottomNavId: Int = R.id.bottom_navigation,
        navBackgroundId: Int = R.id.nav_background,
        coordinatorLayoutId: Int = R.id.coordinator_layout,
        backgroundId: Int = R.id.constraint_layout,
        navigatorSupplier: () -> Navigator
) = object : ReadWriteProperty<FragmentActivity, UiState> {

    private val driver by lazy {
        GlobalUiDriver(
                host = this@globalUiDriver,
                toolbarId = toolbarId,
                fabId = fabId,
                bottomNavId = bottomNavId,
                navBackgroundId = navBackgroundId,
                backgroundId = backgroundId,
                coordinatorLayoutId = coordinatorLayoutId,
                navigatorSupplier = navigatorSupplier
        )
    }

    override operator fun getValue(thisRef: FragmentActivity, property: KProperty<*>): UiState =
            driver.uiState

    override fun setValue(thisRef: FragmentActivity, property: KProperty<*>, value: UiState) {
        driver.uiState = value
    }
}

/**
 * Convenience method for [Fragment] delegation to a [FragmentActivity] when implementing
 * [GlobalUiController]
 */
fun Fragment.activityGlobalUiController() = object : ReadWriteProperty<Fragment, UiState> {

    override operator fun getValue(thisRef: Fragment, property: KProperty<*>): UiState =
            (activity as? GlobalUiController)?.uiState
                    ?: throw IllegalStateException("This fragment is not hosted by a GlobalUiController")

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: UiState) {
        val host = activity
        check(host is GlobalUiController) { "This fragment is not hosted by a GlobalUiController" }
        host.uiState = value
    }
}

/**
 * Drives global UI that is common from screen to screen described by a [UiState].
 * This makes it so that these persistent UI elements aren't duplicated, and only animate themselves when they change.
 * This is the default implementation of [GlobalUiController] that other implementations of
 * the same interface should delegate to.
 */
class GlobalUiDriver(
        private val host: FragmentActivity,
        toolbarId: Int,
        fabId: Int,
        bottomNavId: Int,
        navBackgroundId: Int,
        backgroundId: Int,
        coordinatorLayoutId: Int,
        private val navigatorSupplier: () -> Navigator
) : GlobalUiController {

    init {
        host.window.statusBarColor = host.colorAt(R.color.transparent)
        host.window.decorView.systemUiVisibility = DEFAULT_SYSTEM_UI_FLAGS
    }

    private val toolbarHider: ViewHider<Toolbar> = host.findViewById<Toolbar>(toolbarId).run {
        setOnMenuItemClickListener(this@GlobalUiDriver::onMenuItemClicked)
        setNavigationOnClickListener { navigatorSupplier().pop() }
        ViewHider.of(this).setDirection(ViewHider.TOP).build()
    }

    private val fabHider: ViewHider<MaterialButton> = host.findViewById<MaterialButton>(fabId).run {
        ViewHider.of(this).setDirection(ViewHider.BOTTOM).build()
    }

    private val bottomNavHider: ViewHider<*> = host.findViewById<BottomNavigationView>(bottomNavId).run {
        ViewHider.of(this).setDirection(ViewHider.BOTTOM).build()
    }

    private val fabExtensionAnimator: FabExtensionAnimator =
            FabExtensionAnimator(fabHider.view).apply { isExtended = true }

    private val navBackgroundView: View =
            host.findViewById<View>(navBackgroundId)

    private val coordinatorLayout: CoordinatorLayout =
            host.findViewById(coordinatorLayoutId)

    private val backgroundView: View =
            host.findViewById(backgroundId)

    private val shortestAvailableLifecycle
        get() = when (val fragment = navigatorSupplier().current) {
            null -> host.lifecycle
            else -> when (fragment.view) {
                null -> fragment.lifecycle
                else -> fragment.viewLifecycleOwner.lifecycle
            }
        }
    private var state: UiState = UiState.freshState()

    override var uiState: UiState
        get() = state
        set(value) {
            val previous = state.copy()
            val updated = value.copy(
                    fabClickListener = value.fabClickListener?.lifecycleAware(),
                    fabTransitionOptions = value.fabTransitionOptions?.lifecycleAware()
            )
            state = updated.copy(toolbarInvalidated = false, snackbarText = "") // Reset after firing once
            previous.diff(
                    newState = updated,
                    showsBottomNavConsumer = bottomNavHider::set,
                    showsFabConsumer = fabHider::set,
                    showsToolbarConsumer = toolbarHider::set,
                    navBarColorConsumer = this::setNavBarColor,
                    lightStatusBarConsumer = this::setLightStatusBar,
                    fabStateConsumer = this::setFabIcon,
                    fabExtendedConsumer = fabExtensionAnimator::isExtended::set,
                    backgroundColorConsumer = backgroundView::animateBackground,
                    snackbarTextConsumer = this::showSnackBar,
                    toolbarStateConsumer = this::updateMainToolBar,
                    fabClickListenerConsumer = this::setFabClickListener,
                    fabTransitionOptionConsumer = this::setFabTransitionOptions
            )

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            uiFlagTweak {
                if (value.navBarColor.isBrightColor) it or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                else it and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }

            host.window.navigationBarColor = value.navBarColor
        }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        val fragment = navigatorSupplier().current
        val selected = fragment != null && fragment.onOptionsItemSelected(item)

        return selected || host.onOptionsItemSelected(item)
    }

    private fun setNavBarColor(color: Int) {
        navBackgroundView.background = GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(color, Color.TRANSPARENT))
    }

    private fun setLightStatusBar(lightStatusBar: Boolean) = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> uiFlagTweak { flags ->
            if (lightStatusBar) flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        else -> host.window.statusBarColor = host.colorAt(if (lightStatusBar) R.color.transparent else R.color.black_50)
    }

    private fun uiFlagTweak(tweaker: (Int) -> Int) = host.window.decorView.run {
        systemUiVisibility = tweaker(systemUiVisibility)
    }

    private fun updateMainToolBar(menu: Int, invalidatedAlone: Boolean, title: CharSequence) = toolbarHider.view.run {
        update(menu, invalidatedAlone, title)
        navigatorSupplier().current?.onPrepareOptionsMenu(this.menu)
        Unit
    }

    private fun setFabIcon(@DrawableRes icon: Int, title: CharSequence) = host.runOnUiThread {
        if (icon != 0 && title.isNotBlank()) fabExtensionAnimator.updateGlyphs(title, icon)
    }

    private fun setFabClickListener(onClickListener: ((View) -> Unit)?) =
            fabHider.view.setOnClickListener(onClickListener)

    private fun setFabTransitionOptions(options: (SpringAnimation.() -> Unit)?) {
        if (options != null) fabExtensionAnimator.configureSpring(options)
    }

    private fun showSnackBar(message: CharSequence) = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).run {
        // Necessary to remove snackbar padding for keyboard on older versions of Android
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets -> insets }
        show()
    }

    private fun Toolbar.update(@MenuRes menu: Int, invalidatedAlone: Boolean, title: CharSequence) {
        if (invalidatedAlone) return refreshMenu()

        val currentTitle = this.title?.toString() ?: ""
        if (currentTitle.isNotBlank()) TransitionManager.beginDelayedTransition(this, AutoTransition().apply {
            // We only want to animate the title, but it's lazy initialized.
            // If it's there, use it, else fuzzy match to it's initialization
            val titleTextView = children.filterIsInstance<TextView>()
                    .filter { it.text?.toString() == currentTitle }
                    .firstOrNull()
            if (titleTextView != null) addTarget(titleTextView) else addTarget(TextView::class.java)
        })

        this.title = if (title.isEmpty()) " " else title
        refreshMenu(menu)
        updateIcons()
    }

    private fun Toolbar.refreshMenu(menu: Int? = null) {
        if (menu != null) {
            this.menu.clear()
            if (menu != 0) inflateMenu(menu)
        }
        navigatorSupplier().current?.onPrepareOptionsMenu(this.menu)
    }

    private fun Toolbar.updateIcons() {
        TransitionManager.beginDelayedTransition(this, AutoTransition().setDuration(100).addTarget(ActionMenuView::class.java))
        val tint = titleTint

        menu.forEach {
            it.icon = it.icon?.withTint(tint)
            it.title = it.title.color(tint)
            it.actionView?.backgroundTintList = ColorStateList.valueOf(tint)
        }

        overflowIcon = overflowIcon?.withTint(tint)
        navigationIcon =
                if (navigatorSupplier().previous == null) null
                else context.drawableAt(R.drawable.ic_arrow_back_24dp)?.withTint(tint)
    }

    private val Toolbar.titleTint: Int
        get() = (title as? Spanned)?.run {
            getSpans(0, title.length, ForegroundColorSpan::class.java)
                    .firstOrNull()
                    ?.foregroundColor
        } ?: context.themeColorAt(R.attr.prominent_text_color)

    companion object {
        private const val DEFAULT_SYSTEM_UI_FLAGS =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
    }

    /**
     * Wraps an action with the shortest available lifecycle to make sure nothing leaks.
     * If [this] is already a [LifeCycleAwareCallback], it was previously wrapped and will NO-OP.
     */
    private fun <T> ((T) -> Unit).lifecycleAware(): (T) -> Unit =
            if (this is LifeCycleAwareCallback) this else LifeCycleAwareCallback(shortestAvailableLifecycle, this)
}

private fun View.animateBackground(@ColorInt to: Int) {
    val animator = getTag(R.id.doggo_image) as? ValueAnimator
            ?: ValueAnimator().apply {
                setTag(R.id.doggo_image, this)
                setIntValues(Color.TRANSPARENT)
                setEvaluator(ArgbEvaluator())
                addUpdateListener { setBackgroundColor(it.animatedValue as Int) }
            }

    if (animator.isRunning) animator.cancel()
    animator.setIntValues(animator.animatedValue as Int, to)
    animator.start()
}

private val Int.isBrightColor get() = ColorUtils.calculateLuminance(this) > 0.5

private fun ViewHider<*>.set(show: Boolean) =
        if (show) show()
        else hide()

private class LifeCycleAwareCallback<T>(lifecycle: Lifecycle, implementation: (T) -> Unit) : (T) -> Unit {
    private var callback: (T) -> Unit = implementation

    init {
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) callback = {}
        })
    }

    override fun invoke(type: T) = callback.invoke(type)
}
