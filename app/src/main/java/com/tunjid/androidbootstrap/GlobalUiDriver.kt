package com.tunjid.androidbootstrap

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnLayout
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator
import com.tunjid.androidbootstrap.view.animator.ViewHider
import com.tunjid.androidbootstrap.view.util.update
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * An interface for classes that host a [com.tunjid.androidbootstrap.UiState], usually a [FragmentActivity].
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
        currentFragmentSource: () -> Fragment?
) = object : ReadWriteProperty<FragmentActivity, UiState> {

    private val driver = GlobalUiDriver(this@globalUiDriver, toolbarId, fabId, bottomNavId, navBackgroundId, currentFragmentSource)

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
 * Drives global UI that is common from screen to screen described by a [com.tunjid.androidbootstrap.UiState].
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
        private val getCurrentFragment: () -> Fragment?
) : GlobalUiController {

    // Lazy lookups because setContentView in the host will not have been called at delegation time.

    private val fab: MaterialButton by lazy {
        host.findViewById<MaterialButton>(fabId)
    }

    private val toolbar: Toolbar by lazy {
        host.window.statusBarColor = ContextCompat.getColor(host, R.color.transparent)
        host.window.decorView.systemUiVisibility = DEFAULT_SYSTEM_UI_FLAGS
        host.findViewById<Toolbar>(toolbarId).apply {
            setOnMenuItemClickListener(this@GlobalUiDriver::onMenuItemClicked)
        }
    }

    private val bottomNav: BottomNavigationView by lazy {
        host.findViewById<BottomNavigationView>(bottomNavId)
    }

    private val navBackgroundView: View by lazy {
        host.findViewById<View>(navBackgroundId)
    }

    private val toolbarHider: ViewHider by lazy {
        ViewHider.of(toolbar).setDirection(ViewHider.TOP).build()
    }

    private val fabHider: ViewHider by lazy {
        ViewHider.of(fab).setDirection(ViewHider.BOTTOM).addEndRunnable { fab.visibility = View.VISIBLE }.build()
    }

    private val bottomBarHider: ViewHider by lazy {
        val bottomBarSnapshot = host.findViewById<ImageView>(R.id.bottom_nav_snapshot)
        bottomNav.doOnLayout { bottomBarSnapshot.layoutParams.height = bottomNav.height }

        ViewHider.of(bottomBarSnapshot)
                .setDirection(ViewHider.BOTTOM)
                .addStartRunnable {
                    if (getCurrentFragment() == null) return@addStartRunnable
                    if (bottomNav.isVisible) bottomBarSnapshot.setImageBitmap(bottomNav.drawToBitmap(Bitmap.Config.ARGB_8888))
                    if (uiState.showsBottomNav) bottomNav.visibility = View.VISIBLE

                    bottomNav.isVisible = uiState.showsBottomNav
                }
                .build()
    }

    private val fabExtensionAnimator: FabExtensionAnimator by lazy {
        FabExtensionAnimator(fab).apply { isExtended = true }
    }

    private var state: UiState = UiState.freshState()

    override var uiState: UiState
        get() = state
        set(value) {
            val previous = state.copy()
            state = value
            previous.diff(
                    value,
                    this::toggleBottomNav,
                    this::toggleFab,
                    this::toggleToolbar,
                    this::setNavBarColor,
                    this::setFabIcon,
                    fabExtensionAnimator::setExtended,
                    this::updateMainToolBar,
                    this::setFabClickListener
            )

            bottomBarHider
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val isLight = ColorUtils.calculateLuminance(value.navBarColor) > 0.5
            val systemUiVisibility = if (isLight) DEFAULT_SYSTEM_UI_FLAGS or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            else DEFAULT_SYSTEM_UI_FLAGS

            host.window.decorView.systemUiVisibility = systemUiVisibility
            host.window.navigationBarColor = value.navBarColor
        }

    private fun onMenuItemClicked(item: MenuItem): Boolean {
        val fragment = getCurrentFragment()
        val selected = fragment != null && fragment.onOptionsItemSelected(item)

        return selected || host.onOptionsItemSelected(item)
    }

    private fun toggleToolbar(show: Boolean) =
            if (show) toolbarHider.show()
            else toolbarHider.hide()

    private fun toggleFab(show: Boolean) =
            if (show) fabHider.show()
            else fabHider.hide()

    private fun toggleBottomNav(show: Boolean) =
            if (show) bottomBarHider.show()
            else bottomBarHider.hide()

    private fun setNavBarColor(color: Int) {
        navBackgroundView.background = GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(color, Color.TRANSPARENT))
    }

    private fun updateMainToolBar(menu: Int, title: CharSequence) = toolbar.update(title, menu).also {
        getCurrentFragment()?.onPrepareOptionsMenu(toolbar.menu)
    }

    private fun setFabIcon(@DrawableRes icon: Int, title: CharSequence) = host.runOnUiThread {
        if (icon != 0 && title.isNotBlank()) fabExtensionAnimator.updateGlyphs(FabExtensionAnimator.newState(
                title,
                ContextCompat.getDrawable(host, icon)))
    }

    private fun setFabClickListener(onClickListener: View.OnClickListener?) =
            fab.setOnClickListener(onClickListener)

    companion object {
        private const val DEFAULT_SYSTEM_UI_FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
    }
}