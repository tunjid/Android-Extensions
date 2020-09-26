package com.tunjid.androidx.uidrivers

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.databinding.ActivityMainBinding
import com.tunjid.androidx.distinctUntilChanged
import com.tunjid.androidx.map
import com.tunjid.androidx.material.animator.FabExtensionAnimator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.view.animator.ViewHider
import com.tunjid.androidx.view.util.PaddingProperty
import com.tunjid.androidx.view.util.innermostFocusedChild
import com.tunjid.androidx.view.util.spring
import com.tunjid.androidx.view.util.withOneShotEndListener
import kotlin.math.max

/**
 * An interface for classes that host a [UiState], usually a [FragmentActivity].
 * Implementations should delegate to an instance of [GlobalUiDriver]
 */

interface GlobalUiHost {
    val globalUiController: GlobalUiController
}

interface GlobalUiController {
    var uiState: UiState
    val liveUiState: LiveData<UiState>
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

    private val toolbarHider: ViewHider<Toolbar> = binding.toolbar.run {
        setNavigationOnClickListener { navigator.pop() }
        ViewHider.of(this).setDirection(ViewHider.TOP).build()
    }

    private val fabExtensionAnimator: FabExtensionAnimator =
            FabExtensionAnimator(binding.fab).apply { isExtended = true }

    private val snackbar = Snackbar.make(binding.contentRoot, "", Snackbar.LENGTH_SHORT).apply {
        view.setOnApplyWindowInsetsListener(noOpInsetsListener)
        view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            ::uiState.updatePartial {
                copy(systemUI = systemUI.updateSnackbarHeight(view.height))
            }
        }
        addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                when (event) {
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_SWIPE,
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION,
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT,
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_MANUAL -> ::uiState.updatePartial {
                        copy(snackbarText = "", systemUI = systemUI.updateSnackbarHeight(0))
                    }
                    BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_CONSECUTIVE -> Unit
                }
            }
        })
    }

    private val shortestAvailableLifecycle
        get() = when (val current = navigator.current) {
            null -> host.lifecycle
            else -> if (current.view == null) current.lifecycle else current.viewLifecycleOwner.lifecycle
        }

    private val uiSizes = UISizes(host)
    private val noOpInsetsListener = View.OnApplyWindowInsetsListener { _, insets -> insets }
    private val rootInsetsListener = View.OnApplyWindowInsetsListener { _, insets ->
        liveUiState.value = uiState.reduceSystemInsets(insets, uiSizes.navBarHeightThreshold)
        // Consume insets so other views will not see them.
        insets.consumeSystemWindowInsets()
    }
    override val liveUiState = MutableLiveData<UiState>()

    override var uiState: UiState
        get() = liveUiState.value ?: UiState()
        set(value) {
            val updated = value.copy(
                    systemUI = value.systemUI.filterNoOp(uiState.systemUI),
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

        binding.root.setOnApplyWindowInsetsListener(rootInsetsListener)

        binding.toolbar.setOnApplyWindowInsetsListener(noOpInsetsListener)

        binding.bottomNavigation.doOnLayout { updateBottomNav(this@GlobalUiDriver.uiState.bottomNavPositionalState) }
        binding.bottomNavigation.setOnApplyWindowInsetsListener(noOpInsetsListener)

        binding.contentContainer.setOnApplyWindowInsetsListener(noOpInsetsListener)
        binding.contentContainer.spring(PaddingProperty.BOTTOM).apply {
            // Scroll to text that has focus
            addEndListener { _, _, _, _ -> (binding.contentContainer.innermostFocusedChild as? EditText)?.let { it.text = it.text } }
        }

        UiState::toolbarShows.distinct onChanged toolbarHider::set
        UiState::toolbarState.distinct onChanged toolbarHider.view::updatePartial
        UiState::toolbarMenuClickListener.distinct onChanged this::setMenuItemClickListener
        UiState::toolbarPosition.distinct onChanged { binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = it } }

        UiState::fabGlyphs.distinct onChanged this::setFabGlyphs
        UiState::fabState.distinct onChanged this::updateFabState
        UiState::fabClickListener.distinct onChanged this::setFabClickListener
        UiState::fabExtended.distinct onChanged fabExtensionAnimator::isExtended::set
        UiState::fabTransitionOptions.distinct onChanged this::setFabTransitionOptions

        UiState::snackbarText.distinct onChanged this::showSnackBar
        UiState::navBarColor.distinct onChanged this::setNavBarColor
        UiState::lightStatusBar.distinct onChanged this::setLightStatusBar
        UiState::fragmentContainerState.distinct onChanged this::updateFragmentContainer
        UiState::backgroundColor.distinct onChanged binding.contentRoot::animateBackground

        UiState::bottomNavPositionalState.distinct onChanged this::updateBottomNav
        UiState::snackbarPositionalState.distinct onChanged this::updateSnackbar
        { uiState: UiState -> uiState.systemUI.dynamic.snackbarHeight }.distinct onChanged { Log.i("TEST", "Snackbar height: $it")}
    }

    private fun updateFabState(state: FabPositionalState) {
        if (state.fabVisible) binding.fab.isVisible = true
        val fabTranslation = when {
            state.fabVisible -> {
                val navBarHeight = state.navBarSize
                val snackbarHeight = state.snackbarHeight
                val bottomNavHeight = uiSizes.bottomNavSize countIf state.bottomNavVisible
                val insetClearance = max(bottomNavHeight, state.keyboardSize)
                val totalBottomClearance = navBarHeight + insetClearance + snackbarHeight

                -totalBottomClearance.toFloat()
            }
            else -> binding.fab.height.toFloat() + binding.fab.paddingBottom
        }

        binding.fab.softSpring(SpringAnimation.TRANSLATION_Y)
                .withOneShotEndListener { binding.fab.isVisible = state.fabVisible } // Make the fab gone if hidden
                .animateToFinalPosition(fabTranslation)
    }

    private fun updateSnackbar(state: SnackbarPositionalState) {
        snackbar.view.doOnLayout {
            val bottomNavClearance = uiSizes.bottomNavSize countIf state.bottomNavVisible
            val navBarClearance = state.navBarSize countIf state.insetDescriptor.hasBottomInset
            var insetClearance = uiSizes.snackbarPadding

            insetClearance += if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                max(bottomNavClearance, state.keyboardSize)
            } else {
                max(bottomNavClearance + navBarClearance, state.keyboardSize)
            }

            it.softSpring(SpringAnimation.TRANSLATION_Y)
                    .animateToFinalPosition(-insetClearance.toFloat())
        }
    }

    private fun updateBottomNav(state: BottomNavPositionalState) {
        val navBarClearance = state.navBarSize countIf state.insetDescriptor.hasBottomInset
        binding.bottomNavigation.softSpring(SpringAnimation.TRANSLATION_Y)
                .animateToFinalPosition(if (state.bottomNavVisible) -navBarClearance.toFloat() else uiSizes.bottomNavSize.toFloat())
    }

    private fun updateFragmentContainer(state: FragmentContainerPositionalState) {
        val bottomNavHeight = uiSizes.bottomNavSize countIf state.bottomNavVisible
        val insetClearance = max(bottomNavHeight, state.keyboardSize)
        val navBarClearance = state.navBarSize countIf state.insetDescriptor.hasBottomInset
        val totalBottomClearance = insetClearance + navBarClearance

        val statusBarSize = state.statusBarSize countIf state.insetDescriptor.hasTopInset
        val toolbarHeight = uiSizes.toolbarSize countIf !state.toolbarOverlaps
        val topClearance = statusBarSize + toolbarHeight

        binding.contentContainer.apply {
            softSpring(PaddingProperty.TOP)
                    .animateToFinalPosition(topClearance.toFloat())

            softSpring(PaddingProperty.BOTTOM)
                    .animateToFinalPosition(totalBottomClearance.toFloat())
        }
    }

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

    private fun setFabGlyphs(fabGlyphState: Pair<Int, CharSequence>) = host.runOnUiThread {
        val (@DrawableRes icon: Int, title: CharSequence) = fabGlyphState
        fabExtensionAnimator.updateGlyphs(title, if (icon != 0) host.drawableAt(icon) else null)
    }

    private fun setFabClickListener(onClickListener: ((View) -> Unit)?) =
            binding.fab.setOnClickListener(onClickListener)

    private fun setFabTransitionOptions(options: (SpringAnimation.() -> Unit)?) {
        options?.let(fabExtensionAnimator::configureSpring)
    }

    private fun showSnackBar(message: CharSequence) = if (message.isNotBlank()) {
        snackbar.setText(message)
        snackbar.show()
    } else Unit

    companion object {
        const val ANIMATION_DURATION = 300
        private const val DEFAULT_SYSTEM_UI_FLAGS =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
    }

    /**
     * Maps slices of the ui state to the function that should be invoked when it changes
     */
    private infix fun <T> LiveData<T>.onChanged(consumer: (T) -> Unit) {
        distinctUntilChanged().observe(host, consumer)
    }

    private val <T : Any?> ((UiState) -> T).distinct get() = liveUiState.map(this).distinctUntilChanged()

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

private class UISizes(host: FragmentActivity) {
    val toolbarSize: Int = host.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin)
    val bottomNavSize: Int = host.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin)
    val snackbarPadding: Int = host.resources.getDimensionPixelSize(R.dimen.half_margin)
    val navBarHeightThreshold: Int = host.resources.getDimensionPixelSize(R.dimen.quintuple_margin)
}

private infix fun Int.countIf(condition: Boolean) = if (condition) this else 0
