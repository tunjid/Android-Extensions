package com.tunjid.androidx.uidrivers

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.core.view.forEach
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.graphics.drawable.withTint
import com.tunjid.androidx.core.text.color
import com.tunjid.androidx.databinding.ActivityMainBinding
import com.tunjid.androidx.distinctUntilChanged
import com.tunjid.androidx.map
import com.tunjid.androidx.material.animator.FabExtensionAnimator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.view.animator.ViewHider
import com.tunjid.androidx.view.util.innermostFocusedChild
import com.tunjid.androidx.view.util.marginLayoutParams
import com.tunjid.androidx.view.util.spring
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
        private val binding: ActivityMainBinding,
        private val navigator: Navigator
) : GlobalUiController {

    private var statusBarSize: Int = 0
    private var navBarSize: Int = 0
    private var systemLeftInset: Int = 0
    private var systemRightInset: Int = 0
    private var insetsApplied: Boolean = false
    private var lastFragmentInsets: WindowInsets? = null

    private val toolbarHider: ViewHider<Toolbar> = binding.toolbar.run {
        setOnMenuItemClickListener(this@GlobalUiDriver::onMenuItemClicked)
        setNavigationOnClickListener { navigator.pop() }
        ViewHider.of(this).setDirection(ViewHider.TOP).build()
    }

    private val fabExtensionAnimator: FabExtensionAnimator =
            FabExtensionAnimator(binding.fab).apply { isExtended = true }

    private val bottomNavSpring = binding.bottomNavigation.run {
        doOnLayout { lastFragmentInsets?.let(::onFragmentInsetsReceived) }
        springAnimationOf(View::getTranslationY) { translationY = it.toFloat() }
    }

    private val topContentSpring =
            binding.contentContainer.springAnimationOf(View::getPaddingTop) { updatePadding(top = it) }

    private val bottomContentSpring =
            binding.contentContainer.springAnimationOf(View::getPaddingBottom) { updatePadding(bottom = it) }
                    // Scroll to text that has focus
                    .apply { addEndListener { _, _, _, _ -> (binding.contentContainer.innermostFocusedChild as? EditText)?.let { it.text = it.text } } }

    private val fabSpring =
            binding.fab.springAnimationOf(View::marginBottom) { updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = it } }

    private val shortestAvailableLifecycle
        get() = when (val current = navigator.current) {
            null -> host.lifecycle
            else -> if (current.view == null) current.lifecycle else current.viewLifecycleOwner.lifecycle
        }

    private val liveUiState = MutableLiveData(UiState.freshState())

    override var uiState: UiState
        get() = liveUiState.value!!
        set(value) {
            val updated = value.copy(
                    fabClickListener = value.fabClickListener?.lifecycleAware(),
                    fabTransitionOptions = value.fabTransitionOptions?.lifecycleAware()
            )
            liveUiState.value = updated
            liveUiState.value = updated.copy(toolbarInvalidated = false) // Reset after firing once

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            uiFlagTweak {
                if (value.navBarColor.isBrightColor) it or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                else it and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }

            host.window.navigationBarColor = value.navBarColor
            lastFragmentInsets?.let(::onFragmentInsetsReceived)
        }

    init {
        host.window.statusBarColor = host.colorAt(R.color.transparent)
        host.window.decorView.systemUiVisibility = DEFAULT_SYSTEM_UI_FLAGS
        host.supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                if (f != navigator.current) return

                lastFragmentInsets?.let(::onFragmentInsetsReceived)
                v.setOnApplyWindowInsetsListener { _, insets -> onFragmentInsetsReceived(insets) }
            }
        }, true)

        binding.root.setOnApplyWindowInsetsListener { _, insets -> onSystemInsetsReceived(insets) }

        liveUiState.map(UiState::toolbarShows).distinctUntilChanged().observe(host, toolbarHider::set)
        liveUiState.map(UiState::fabExtended).distinctUntilChanged().observe(host, fabExtensionAnimator::isExtended::set)
        liveUiState.map(UiState::backgroundColor).distinctUntilChanged().observe(host, binding.contentRoot::animateBackground)

        liveUiState.map(UiState::fabState).distinctUntilChanged().observe(host, this::setFabIcon)
        liveUiState.map(UiState::snackbarText).distinctUntilChanged().observe(host, this::showSnackBar)
        liveUiState.map(UiState::navBarColor).distinctUntilChanged().observe(host, this::setNavBarColor)
        liveUiState.map(UiState::toolbarState).distinctUntilChanged().observe(host, this::updateMainToolBar)
        liveUiState.map(UiState::lightStatusBar).distinctUntilChanged().observe(host, this::setLightStatusBar)
        liveUiState.map(UiState::fabClickListener).distinctUntilChanged().observe(host, this::setFabClickListener)
        liveUiState.map(UiState::fabTransitionOptions).distinctUntilChanged().observe(host, this::setFabTransitionOptions)
    }

    private fun onSystemInsetsReceived(insets: WindowInsets): WindowInsets = insets.apply {
        if (insetsApplied) return insets

        statusBarSize = insets.systemWindowInsetTop
        systemLeftInset = insets.systemWindowInsetLeft
        systemRightInset = insets.systemWindowInsetRight
        navBarSize = insets.systemWindowInsetBottom

        toolbarHider.view.marginLayoutParams.topMargin = statusBarSize
        binding.bottomNavigation.marginLayoutParams.bottomMargin = navBarSize

        lastFragmentInsets?.let(::onFragmentInsetsReceived)

        insetsApplied = true
    }

    private fun onFragmentInsetsReceived(insets: WindowInsets): WindowInsets = insets.apply {
        lastFragmentInsets = this

        bottomNavSpring.animateToFinalPosition(bottomNavPosition())
        topContentSpring.animateToFinalPosition((statusBarSize given uiState.insetFlags.hasTopInset).toFloat())
        bottomContentSpring.animateToFinalPosition(contentPosition(systemWindowInsetBottom))
        fabSpring.animateToFinalPosition(fabPosition(systemWindowInsetBottom))
    }

    private fun bottomNavPosition() =
            binding.bottomNavigation.height.plus(navBarSize).given(!uiState.showsBottomNav).toFloat()

    private fun contentPosition(systemBottomInset: Int): Float = when (systemBottomInset > navBarSize + binding.bottomNavigation.height.given(uiState.showsBottomNav)) {
        true -> systemBottomInset
        else -> (binding.bottomNavigation.height given uiState.showsBottomNav) + (navBarSize given uiState.insetFlags.hasBottomInset)
    }.toFloat()

    private fun fabPosition(systemBottomInset: Int): Float {
        val styleMargin = host.resources.getDimensionPixelSize(R.dimen.single_margin)
        val snackbarClearance = host.resources.getDimensionPixelSize(R.dimen.double_and_half_margin)
        if (!uiState.fabShows) return -binding.fab.height.toFloat()
        return when {
            systemBottomInset > navBarSize -> systemBottomInset + styleMargin
            else -> navBarSize + styleMargin + (binding.bottomNavigation.height given uiState.showsBottomNav)
        }.toFloat() + (snackbarClearance given uiState.snackbarText.isNotBlank())
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        val fragment = navigator.current
        val selected = fragment != null && fragment.onOptionsItemSelected(item)

        return selected || host.onOptionsItemSelected(item)
    }

    private fun setNavBarColor(color: Int) {
        binding.navBackground.background = GradientDrawable(
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

    private fun updateMainToolBar(toolbarState: ToolbarState) = toolbarHider.view.run {
        update(toolbarState)
        navigator.current?.onPrepareOptionsMenu(this.menu)
        Unit
    }

    private fun setFabIcon(fabState: FabState) = host.runOnUiThread {
        val(@DrawableRes icon: Int, title: CharSequence) = fabState
        if (icon != 0 && title.isNotBlank()) fabExtensionAnimator.updateGlyphs(title, icon)
    }

    private fun setFabClickListener(onClickListener: ((View) -> Unit)?) =
            binding.fab.setOnClickListener(onClickListener)

    private fun setFabTransitionOptions(options: (SpringAnimation.() -> Unit)?) {
        if (options != null) fabExtensionAnimator.configureSpring(options)
    }

    private fun showSnackBar(message: CharSequence) = if (message.isNotBlank()) Snackbar.make(binding.contentRoot, message, Snackbar.LENGTH_SHORT).run {
        // Necessary to remove snackbar padding for keyboard on older versions of Android
        view.setOnApplyWindowInsetsListener { _, insets -> insets }
        addCallback(object : Snackbar.Callback(), LifecycleOwner {
            private val lifecycle = LifecycleRegistry(this)

            override fun getLifecycle(): Lifecycle = lifecycle

            override fun onShown(sb: Snackbar?) {
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                liveUiState.map(UiState::showsBottomNav)
                        .distinctUntilChanged()
                        .observe(this) {
                            view.spring(DynamicAnimation.TRANSLATION_Y)
                                    .soften()
                                    .animateToFinalPosition(if (it) -binding.bottomNavigation.height.toFloat() else 0f)
                        }
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                ::uiState.update { copy(snackbarText = "") }
            }
        })
        show()
    } else Unit

    private fun Toolbar.update(toolbarState: ToolbarState) {
        val (@MenuRes menu: Int, invalidatedAlone: Boolean, title: CharSequence) = toolbarState
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
        navigator.current?.onPrepareOptionsMenu(this.menu)
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
                if (navigator.previous == null) null
                else context.drawableAt(R.drawable.ic_arrow_back_24dp)?.withTint(tint)
    }

    private val Toolbar.titleTint: Int
        get() = (title as? Spanned)?.run {
            getSpans(0, title.length, ForegroundColorSpan::class.java)
                    .firstOrNull()
                    ?.foregroundColor
        } ?: context.themeColorAt(R.attr.prominent_text_color)

    companion object {
        const val ANIMATION_DURATION = 300
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

private fun View.springAnimationOf(getter: (View) -> Number, setter: View.(Int) -> Unit) =
        springAnimationOf(
                getter = { getter(this).toFloat() },
                setter = { setter(it.toInt()) },
                finalPosition = 0f
        ).soften()

private fun SpringAnimation.soften() = apply {
    spring.stiffness = SpringForce.STIFFNESS_LOW
    spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
}

private infix fun Int.given(flag: Boolean) = if (flag) this else 0

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
