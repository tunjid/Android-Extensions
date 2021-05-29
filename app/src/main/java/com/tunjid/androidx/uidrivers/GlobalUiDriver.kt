package com.tunjid.androidx.uidrivers

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.FragmentActivity
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
import com.tunjid.androidx.view.util.viewDelegate
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

    private val uiSizes = UISizes(host)
    private val fabExtensionAnimator = FabExtensionAnimator(binding.fab)
    private val toolbarHider = ViewHider.of(binding.toolbar).setDirection(ViewHider.TOP).build()
    private val insetsController = WindowInsetsControllerCompat(host.window, binding.root)
    private val noOpInsetsListener = View.OnApplyWindowInsetsListener { _, insets -> insets }
    private val rootInsetsListener = View.OnApplyWindowInsetsListener { _, insets ->
        liveUiState.value = uiState.reduceSystemInsets(WindowInsetsCompat.toWindowInsetsCompat(insets), uiSizes.navBarHeightThreshold)
        // Consume insets so other views will not see them.
        insets.consumeSystemWindowInsets()
    }
    override val liveUiState = MutableLiveData<UiState>()

    override var uiState: UiState
        get() = liveUiState.value ?: UiState()
        set(value) {
            val updated = value.copy(
                systemUI = value.systemUI.filterNoOp(uiState.systemUI),
                fabClickListener = value.fabClickListener,
                fabTransitionOptions = value.fabTransitionOptions,
                toolbarMenuRefresher = value.toolbarMenuRefresher,
                toolbarMenuClickListener = value.toolbarMenuClickListener
            )
            liveUiState.value = updated
            liveUiState.value = updated.copy(toolbarInvalidated = false) // Reset after firing once
        }

    init {
        host.window.assumeControl()
        binding.root.setOnApplyWindowInsetsListener(rootInsetsListener)

        binding.toolbar.setNavigationOnClickListener { navigator.pop() }
        binding.toolbar.setOnApplyWindowInsetsListener(noOpInsetsListener)

        binding.bottomNavigation.doOnLayout { updateBottomNav(this@GlobalUiDriver.uiState.bottomNavPositionalState) }
        binding.bottomNavigation.setOnApplyWindowInsetsListener(noOpInsetsListener)

        binding.contentContainer.setOnApplyWindowInsetsListener(noOpInsetsListener)
        binding.contentContainer.spring(PaddingProperty.BOTTOM).apply {
            // Scroll to text that has focus
            addEndListener { _, _, _, _ -> (binding.contentContainer.innermostFocusedChild as? EditText)?.let { it.text = it.text } }
        }

        UiState::toolbarShows.distinct onChanged toolbarHider::set
        UiState::toolbarTitle.distinct onChanged toolbarHider.view::updateTitle
        UiState::toolbarMenuState.distinct onChanged toolbarHider.view::updatePartial
        UiState::toolbarMenuClickListener.distinct onChanged this::setMenuItemClickListener
        UiState::toolbarPosition.distinct onChanged { binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = it } }

        UiState::fabGlyphs.distinct onChanged this::setFabGlyphs
        UiState::fabState.distinct onChanged this::updateFabState
        UiState::fabClickListener.distinct onChanged this::setFabClickListener
        UiState::fabExtended.distinct onChanged fabExtensionAnimator::isExtended::set
        UiState::fabTransitionOptions.distinct onChanged this::setFabTransitionOptions

        UiState::snackbarText.distinct onChanged this::showSnackBar
        UiState::navBarColor.distinct onChanged this::setNavBarColor
        UiState::statusBarColor.distinct onChanged host.window::setStatusBarColor
        UiState::lightStatusBar.distinct onChanged this::setLightStatusBar
        UiState::fragmentContainerState.distinct onChanged this::updateFragmentContainer
        UiState::backgroundColor.distinct onChanged binding.contentRoot::animateBackground
        UiState::isImmersive.distinct onChanged this::updateImmersivity

        UiState::bottomNavPositionalState.distinct onChanged this::updateBottomNav
        UiState::snackbarPositionalState.distinct onChanged this::updateSnackbar
    }

    private fun updateFabState(state: FabPositionalState) {
        if (state.fabVisible) binding.fab.isVisible = true
        val fabTranslation = when {
            state.fabVisible -> {
                val navBarHeight = state.navBarSize
                val snackbarHeight = if (state.snackbarHeight == 0) 0 else state.snackbarHeight + uiSizes.snackbarPadding
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
            val insetClearance =
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) max(bottomNavClearance, state.keyboardSize)
                else max(bottomNavClearance + navBarClearance, state.keyboardSize)

            it.softSpring(SpringAnimation.TRANSLATION_Y)
                .animateToFinalPosition(-insetClearance.toFloat())
        }
    }

    private fun updateBottomNav(state: BottomNavPositionalState) {
        binding.bottomNavigation.softSpring(PaddingProperty.BOTTOM)
            .animateToFinalPosition(state.navBarSize.toFloat())
        binding.bottomNavigation.softSpring(SpringAnimation.TRANSLATION_Y)
            .animateToFinalPosition(if (state.bottomNavVisible) 0F else uiSizes.bottomNavSize.plus(state.navBarSize).toFloat())
    }

    private fun updateFragmentContainer(state: FragmentContainerPositionalState) {
        val bottomNavHeight = uiSizes.bottomNavSize countIf state.bottomNavVisible
        val insetClearance = max(bottomNavHeight, state.keyboardSize)
        val navBarClearance = state.navBarSize countIf state.insetDescriptor.hasBottomInset
        val totalBottomClearance = insetClearance + navBarClearance

        val statusBarSize = state.statusBarSize countIf state.insetDescriptor.hasTopInset
        val toolbarHeight = uiSizes.toolbarSize countIf !state.toolbarOverlaps
        val topClearance = statusBarSize + toolbarHeight

        binding.contentContainer
            .softSpring(PaddingProperty.TOP)
            .animateToFinalPosition(topClearance.toFloat())

        binding.contentContainer
            .softSpring(PaddingProperty.BOTTOM)
            .animateToFinalPosition(totalBottomClearance.toFloat())
    }

    private fun setMenuItemClickListener(item: ((MenuItem) -> Unit)?) =
        binding.toolbar.setOnMenuItemClickListener {
            item?.invoke(it)?.let { true } ?: host.onOptionsItemSelected(it)
        }

    private fun setNavBarColor(color: Int) {
        binding.navBackground.background = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            intArrayOf(color, Color.TRANSPARENT))

        insetsController.isAppearanceLightNavigationBars = color.isBrightColor
    }

    private fun updateImmersivity(isImmersive: Boolean) {
        val systemBarsSetter = if (isImmersive) insetsController::hide else insetsController::show
        systemBarsSetter.invoke(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun setLightStatusBar(lightStatusBar: Boolean) = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> insetsController.isAppearanceLightStatusBars = lightStatusBar
        else -> host.window.statusBarColor = host.colorAt(if (lightStatusBar) R.color.transparent else R.color.black_50)
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
    }

    /**
     * Maps slices of the ui state to the function that should be invoked when it changes
     */
    private infix fun <T> LiveData<T>.onChanged(consumer: (T) -> Unit) {
        distinctUntilChanged().observe(host, consumer)
    }

    private val <T : Any?> ((UiState) -> T).distinct get() = liveUiState.map(this).distinctUntilChanged()
}

private var View.backgroundAnimator by viewDelegate<ValueAnimator?>()

@SuppressLint("Recycle")
private fun View.animateBackground(@ColorInt to: Int) {
    val animator = backgroundAnimator ?: ValueAnimator().apply {
        setTag(R.id.doggo_image, this)
        setIntValues(Color.TRANSPARENT)
        setEvaluator(ArgbEvaluator())
        addUpdateListener { setBackgroundColor(it.animatedValue as Int) }
        backgroundAnimator = this
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

private class UISizes(host: FragmentActivity) {
    val toolbarSize: Int = host.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin)
    val bottomNavSize: Int = host.resources.getDimensionPixelSize(R.dimen.triple_and_half_margin)
    val snackbarPadding: Int = host.resources.getDimensionPixelSize(R.dimen.half_margin)
    val navBarHeightThreshold: Int = host.resources.getDimensionPixelSize(R.dimen.quintuple_margin)
}

private infix fun Int.countIf(condition: Boolean) = if (condition) this else 0

private fun Window.assumeControl() {
    val context = decorView.context
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val windowAttributes = attributes
        windowAttributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        attributes = windowAttributes
    }
    WindowCompat.setDecorFitsSystemWindows(this, false)
    navigationBarColor = context.colorAt(R.color.transparent)
    statusBarColor = context.colorAt(R.color.transparent)
}
