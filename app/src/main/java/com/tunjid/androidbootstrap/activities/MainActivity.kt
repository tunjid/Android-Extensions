package com.tunjid.androidbootstrap.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.transitionseverywhere.ChangeText
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity
import com.tunjid.androidbootstrap.fragments.RouteFragment
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator.GlyphState
import com.tunjid.androidbootstrap.view.animator.ViewHider
import com.tunjid.androidbootstrap.view.util.ViewUtil
import com.tunjid.androidbootstrap.view.util.ViewUtil.getLayoutParams

class MainActivity : BaseActivity() {

    companion object {
        private const val DEFAULT_SYSTEM_UI_FLAGS = SYSTEM_UI_FLAG_LAYOUT_STABLE or
                SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

        const val ANIMATION_DURATION = 300
        var topInset: Int = 0
        var bottomInset: Int = 0
    }

    private var insetsApplied: Boolean = false
    private var leftInset: Int = 0
    private var rightInset: Int = 0

    private lateinit var fabHider: ViewHider
    private lateinit var toolbarHider: ViewHider
    private lateinit var fabExtensionAnimator: FabExtensionAnimator

    private lateinit var topInsetView: View
    private lateinit var bottomInsetView: View
    private lateinit var keyboardPadding: View

    private lateinit var toolbar: Toolbar
    private lateinit var fab: MaterialButton
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var coordinatorLayout: CoordinatorLayout

    private val fragmentViewCreatedCallback: FragmentManager.FragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
            adjustInsetForFragment(f) // Called when showing a fragment the first time only
        }

        override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
            if (isNotInMainFragmentContainer(v)) return

            val fragment = f as AppBaseFragment
            if (fragment.restoredFromBackStack()) adjustInsetForFragment(f)

            fragment.togglePersistentUi()
            setOnApplyWindowInsetsListener(v) { _, insets -> consumeFragmentInsets(insets) }
        }
    }

    var isFabExtended: Boolean
        get() = fabExtensionAnimator.isExtended
        set(extended) {
            fabExtensionAnimator.isExtended = extended
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentViewCreatedCallback, false)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) showFragment(RouteFragment.newInstance())
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)

        fab = findViewById(R.id.fab)
        toolbar = findViewById(R.id.toolbar)
        topInsetView = findViewById(R.id.top_inset)
        bottomInsetView = findViewById(R.id.bottom_inset)
        keyboardPadding = findViewById(R.id.keyboard_padding)
        constraintLayout = findViewById(R.id.constraint_layout)
        coordinatorLayout = findViewById(R.id.coordinator_layout)

        toolbarHider = ViewHider.of(toolbar).setDirection(ViewHider.TOP).build()
        fabHider = ViewHider.of(fab).setDirection(ViewHider.BOTTOM).build()
        fabExtensionAnimator = FabExtensionAnimator(fab)
        fabExtensionAnimator.isExtended = true

        window.decorView.systemUiVisibility = DEFAULT_SYSTEM_UI_FLAGS

        setSupportActionBar(this.toolbar)
        setOnApplyWindowInsetsListener(this.constraintLayout) { _, insets -> consumeSystemInsets(insets) }
    }

    fun toggleToolbar(show: Boolean) {
        if (show) this.toolbarHider.show()
        else this.toolbarHider.hide()
    }

    fun toggleFab(show: Boolean) {
        if (show) this.fabHider.show()
        else this.fabHider.hide()
    }

    fun updateFab(glyphState: GlyphState) {
        fabExtensionAnimator.updateGlyphs(glyphState)
    }

    fun setFabClickListener(onClickListener: OnClickListener) {
        fab.setOnClickListener(onClickListener)
    }

    fun setTitle(title: String) {
        TransitionManager.beginDelayedTransition(toolbar, ChangeText()
                .setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_OUT_IN))
        toolbar.title = title
    }

    fun showSnackBar(consumer: (Snackbar) -> Unit) {
        val snackbar = Snackbar.make(coordinatorLayout, "", Snackbar.LENGTH_SHORT)

        // Necessary to remove snackbar padding for keyboard on older versions of Android
        setOnApplyWindowInsetsListener(snackbar.view) { _, insets -> insets }
        consumer.invoke(snackbar)
        snackbar.show()
    }

    private fun isNotInMainFragmentContainer(view: View): Boolean {
        val parent = view.parent as View
        return parent.id != R.id.main_fragment_container
    }

    private fun consumeSystemInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        if (this.insetsApplied) return insets

        topInset = insets.systemWindowInsetTop
        leftInset = insets.systemWindowInsetLeft
        rightInset = insets.systemWindowInsetRight
        bottomInset = insets.systemWindowInsetBottom

        ViewUtil.getLayoutParams(this.topInsetView).height = topInset
        ViewUtil.getLayoutParams(this.bottomInsetView).height = bottomInset

        adjustInsetForFragment(currentFragment)

        this.insetsApplied = true
        return insets
    }

    private fun consumeFragmentInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        getLayoutParams(keyboardPadding).height = insets.systemWindowInsetBottom - bottomInset
        return insets
    }

    @SuppressLint("InlinedApi")
    private fun adjustInsetForFragment(fragment: Fragment) {
        if (fragment !is AppBaseFragment) return

        val insetFlags = fragment.insetFlags()
        ViewUtil.getLayoutParams(toolbar).topMargin = if (insetFlags.hasTopInset()) 0 else topInset
        ViewUtil.getLayoutParams(coordinatorLayout).bottomMargin = if (insetFlags.hasBottomInset()) 0 else bottomInset

        TransitionManager.beginDelayedTransition(constraintLayout, AutoTransition()
                .setDuration(ANIMATION_DURATION.toLong())
                .addTarget(R.id.main_fragment_container)
                .addTarget(R.id.coordinator_layout)
        )

        topInsetView.visibility = if (insetFlags.hasTopInset()) VISIBLE else GONE
        bottomInsetView.visibility = if (insetFlags.hasBottomInset()) VISIBLE else GONE

        constraintLayout.setPadding(
                if (insetFlags.hasLeftInset()) this.leftInset else 0,
                0,
                if (insetFlags.hasRightInset()) this.rightInset else 0,
                0)

        if (SDK_INT < O) return

        val isLight = ColorUtils.calculateLuminance(fragment.navBarColor) > 0.5
        val systemUiVisibility = if (isLight) DEFAULT_SYSTEM_UI_FLAGS or SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        else DEFAULT_SYSTEM_UI_FLAGS

        window.decorView.systemUiVisibility = systemUiVisibility
        window.navigationBarColor = fragment.navBarColor
    }

}
