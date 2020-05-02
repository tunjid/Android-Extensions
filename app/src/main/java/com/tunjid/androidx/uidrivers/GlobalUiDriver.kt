package com.tunjid.androidx.uidrivers

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.databinding.ActivityMainBinding
import com.tunjid.androidx.distinctUntilChanged
import com.tunjid.androidx.map
import com.tunjid.androidx.material.animator.FabExtensionAnimator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.view.animator.ViewHider
import com.tunjid.androidx.view.util.MarginProperty
import com.tunjid.androidx.view.util.PaddingProperty
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

    private var insetsApplied: Boolean = false
    private var lastFragmentInsets: WindowInsets? = null

    private val toolbarHider: ViewHider<Toolbar> = binding.toolbar.run {
        setNavigationOnClickListener { navigator.pop() }
        ViewHider.of(this).setDirection(ViewHider.TOP).build()
    }

    private val fabExtensionAnimator: FabExtensionAnimator =
            FabExtensionAnimator(binding.fab).apply { isExtended = true }

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
                    fabClickListener = value.fabClickListener.lifecycleAware(),
                    fabTransitionOptions = value.fabTransitionOptions.lifecycleAware(),
                    toolbarMenuRefresher = value.toolbarMenuRefresher.lifecycleAware(),
                    toolbarMenuClickListener = value.toolbarMenuClickListener.lifecycleAware()
            )
            liveUiState.value = updated
            liveUiState.value = updated.copy(toolbarInvalidated = false) // Reset after firing once
        }

    init {
        host.window.decorView.systemUiVisibility = DEFAULT_SYSTEM_UI_FLAGS
        host.window.navigationBarColor = host.colorAt(R.color.transparent)
        host.window.statusBarColor = host.colorAt(R.color.transparent)
        host.supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                if (f != navigator.current) return

                lastFragmentInsets?.let(::onFragmentInsetsReceived)
                v.setOnApplyWindowInsetsListener { _, insets -> onFragmentInsetsReceived(insets) }
            }
        }, true)

        binding.root.setOnApplyWindowInsetsListener { _, insets -> onSystemInsetsReceived(insets) }
        binding.toolbar.doOnLayout { lastFragmentInsets?.let(::onFragmentInsetsReceived) }
        binding.bottomNavigation.doOnLayout { lastFragmentInsets?.let(::onFragmentInsetsReceived) }

        binding.contentContainer.spring(PaddingProperty.BOTTOM).apply {
            // Scroll to text that has focus
            addEndListener { _, _, _, _ -> (binding.contentContainer.innermostFocusedChild as? EditText)?.let { it.text = it.text } }
        }

        UiState::toolbarShows onChanged toolbarHider::set
        UiState::toolbarState onChanged toolbarHider.view::update
        UiState::toolbarMenuClickListener onChanged this::setMenuItemClickListener

        UiState::fabState onChanged this::setFabIcon
        UiState::fabClickListener onChanged this::setFabClickListener
        UiState::fabExtended onChanged fabExtensionAnimator::isExtended::set
        UiState::fabTransitionOptions onChanged this::setFabTransitionOptions

        UiState::snackbarText onChanged this::showSnackBar
        UiState::navBarColor onChanged this::setNavBarColor
        UiState::lightStatusBar onChanged this::setLightStatusBar
        UiState::backgroundColor onChanged binding.contentRoot::animateBackground
        UiState::positionState onChanged { lastFragmentInsets?.let(::onFragmentInsetsReceived) }
    }

    private fun onSystemInsetsReceived(insets: WindowInsets): WindowInsets = insets.apply {
        if (insetsApplied) return insets

        statusBarSize = insets.systemWindowInsetTop
        systemLeftInset = insets.systemWindowInsetLeft
        systemRightInset = insets.systemWindowInsetRight
        navBarSize = insets.systemWindowInsetBottom

        toolbarHider.view.marginLayoutParams.topMargin = statusBarSize
        lastFragmentInsets?.let(::onFragmentInsetsReceived)

        insetsApplied = true
    }

    private fun onFragmentInsetsReceived(insets: WindowInsets): WindowInsets = insets.apply {
        lastFragmentInsets = this

        binding.contentContainer.softSpring(PaddingProperty.TOP).animateToFinalPosition(contentTopPosition())
        binding.contentContainer.softSpring(PaddingProperty.BOTTOM).animateToFinalPosition(contentBottomPosition(systemWindowInsetBottom))
        binding.bottomNavigation.softSpring(SpringAnimation.TRANSLATION_Y).animateToFinalPosition(bottomNavPosition())
        binding.fab.softSpring(MarginProperty.BOTTOM).animateToFinalPosition(fabPosition(systemWindowInsetBottom))
    }

    private fun contentTopPosition(): Float = (
            statusBarSize.given(uiState.insetFlags.hasTopInset)
                    + binding.toolbar.height.given(!uiState.toolbarOverlaps)
            ).toFloat()

    private fun contentBottomPosition(systemBottomInset: Int): Float = when (systemBottomInset > navBarSize + binding.bottomNavigation.height.given(uiState.showsBottomNav)) {
        true -> systemBottomInset
        else -> (binding.bottomNavigation.height given uiState.showsBottomNav) + (navBarSize given uiState.insetFlags.hasBottomInset)
    }.toFloat()

    private fun fabPosition(systemBottomInset: Int): Float {
        val styleMargin = host.resources.getDimensionPixelSize(R.dimen.single_margin)
        val snackbarClearance = host.resources.getDimensionPixelSize(
                if (uiState.showsBottomNav) R.dimen.triple_margin
                else R.dimen.double_and_half_margin
        )
        if (!uiState.fabShows) return -binding.fab.height.toFloat()
        return when {
            systemBottomInset > navBarSize -> systemBottomInset + styleMargin
            else -> navBarSize + styleMargin + (binding.bottomNavigation.height given uiState.showsBottomNav)
        }.toFloat() + (snackbarClearance given uiState.snackbarText.isNotBlank())
    }

    private fun bottomNavPosition() = when {
        uiState.showsBottomNav -> -(navBarSize.given(uiState.insetFlags.hasBottomInset))
        else -> binding.bottomNavigation.height
    }.toFloat()

    private fun setMenuItemClickListener(item: ((MenuItem) -> Unit)?) =
            binding.toolbar.setOnMenuItemClickListener {
                item?.invoke(it)?.let { true } ?: host.onOptionsItemSelected(it)
            }

    private fun setNavBarColor(color: Int) {
        binding.navBackground.background = GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(color, Color.TRANSPARENT))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) uiFlagTweak {
            if (color.isBrightColor) it or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            else it and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
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

    private fun setFabIcon(fabState: FabState) = host.runOnUiThread {
        val (@DrawableRes icon: Int, title: CharSequence) = fabState
        if (icon != 0 && title.isNotBlank()) fabExtensionAnimator.updateGlyphs(title, icon)
    }

    private fun setFabClickListener(onClickListener: ((View) -> Unit)?) =
            binding.fab.setOnClickListener(onClickListener)

    private fun setFabTransitionOptions(options: (SpringAnimation.() -> Unit)?) {
        options?.let(fabExtensionAnimator::configureSpring)
    }

    private fun showSnackBar(message: CharSequence) = if (message.isNotBlank()) Snackbar.make(binding.contentRoot, message, Snackbar.LENGTH_SHORT).run {
        // Necessary to remove snackbar padding for keyboard on older versions of Android
        view.setOnApplyWindowInsetsListener { _, insets -> insets }
        addCallback(object : Snackbar.Callback(), LifecycleOwner {
            private val lifecycle = LifecycleRegistry(this)

            override fun getLifecycle(): Lifecycle = lifecycle

            override fun onShown(sb: Snackbar?) {
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                liveUiState.map(UiState::showsBottomNav).distinctUntilChanged().observe(this, ::onBottomNavChanged)
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                when (event) {
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_SWIPE,
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION,
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT,
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_MANUAL -> ::uiState.update { copy(snackbarText = "") }
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_CONSECUTIVE -> Unit
                }
            }

            private fun onBottomNavChanged(it: Boolean) = view.softSpring(DynamicAnimation.TRANSLATION_Y)
                    .animateToFinalPosition(
                            if (it) -binding.bottomNavigation.height.toFloat() - host.resources.getDimensionPixelSize(R.dimen.half_margin)
                            else 0f
                    )
        })
        show()
    } else Unit

    companion object {
        const val ANIMATION_DURATION = 300
        private const val DEFAULT_SYSTEM_UI_FLAGS =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

        var statusBarSize: Int = 0
            private set
        var navBarSize: Int = 0
            private set
        var systemLeftInset: Int = 0
            private set
        var systemRightInset: Int = 0
            private set
    }

    /**
     * Maps slices of the ui state to the function that should be invoked when it changes
     */
    private infix fun <T : Any?> ((UiState) -> T).onChanged(consumer: (T) -> Unit) {
        liveUiState.map(this).distinctUntilChanged().observe(host, consumer)
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

fun View.softSpring(property: FloatPropertyCompat<View>) =
        spring(property, SpringForce.STIFFNESS_LOW)

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
