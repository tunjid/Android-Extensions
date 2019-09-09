package com.tunjid.androidbootstrap.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidbootstrap.GlobalUiController
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.UiState
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.core.components.FragmentStateViewModel
import com.tunjid.androidbootstrap.core.components.fragmentStateViewModelFactory
import com.tunjid.androidbootstrap.fragments.RouteFragment
import com.tunjid.androidbootstrap.globalUiDriver
import com.tunjid.androidbootstrap.view.util.ViewUtil.getLayoutParams

class MainActivity : AppCompatActivity(R.layout.activity_main), GlobalUiController {

    private var insetsApplied: Boolean = false
    private var leftInset: Int = 0
    private var rightInset: Int = 0

    private lateinit var topInsetView: View
    private lateinit var bottomInsetView: View
    private lateinit var keyboardPadding: View
    private lateinit var navBackgroundView: View

    private lateinit var toolbar: Toolbar
    private lateinit var fab: MaterialButton
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var coordinatorLayout: CoordinatorLayout

    override var uiState: UiState by globalUiDriver()

    val fragmentStateViewModel: FragmentStateViewModel by fragmentStateViewModelFactory(R.id.main_fragment_container)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uiState = savedInstanceState?.getParcelable(UI_STATE) ?: UiState.freshState()
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {

            override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) =
                    adjustInsetForFragment(f)

            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) =
                    onFragmentViewCreated(v, f)
        }, false)

        if (savedInstanceState == null) RouteFragment.newInstance().apply { fragmentStateViewModel.showFragment(this, stableTag) }
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)

        fab = findViewById(R.id.fab)
        toolbar = findViewById(R.id.toolbar)
        topInsetView = findViewById(R.id.top_inset)
        bottomInsetView = findViewById(R.id.bottom_inset)
        keyboardPadding = findViewById(R.id.keyboard_padding)
        navBackgroundView = findViewById(R.id.nav_background)
        constraintLayout = findViewById(R.id.constraint_layout)
        coordinatorLayout = findViewById(R.id.coordinator_layout)

        toolbar.title = getString(R.string.app_name)

        setOnApplyWindowInsetsListener(this.constraintLayout) { _, insets -> consumeSystemInsets(insets) }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(UI_STATE, uiState)
        super.onSaveInstanceState(outState)
    }

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
        toolbar.postDelayed(ANIMATION_DURATION.toLong()) {
            fragmentStateViewModel.currentFragment?.onPrepareOptionsMenu(toolbar.menu)
        }
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
        return parent.id != fragmentStateViewModel.idResource
    }

    private fun consumeSystemInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        if (this.insetsApplied) return insets

        topInset = insets.systemWindowInsetTop
        leftInset = insets.systemWindowInsetLeft
        rightInset = insets.systemWindowInsetRight
        bottomInset = insets.systemWindowInsetBottom

        topInsetView.layoutParams.height = topInset
        bottomInsetView.layoutParams.height = bottomInset

        adjustInsetForFragment(fragmentStateViewModel.currentFragment)

        this.insetsApplied = true
        return insets
    }

    private fun consumeFragmentInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        getLayoutParams(keyboardPadding).height = insets.systemWindowInsetBottom - bottomInset
        return insets
    }

    @SuppressLint("InlinedApi")
    private fun adjustInsetForFragment(fragment: Fragment?) {
        if (fragment !is AppBaseFragment) return

        val insetFlags = fragment.insetFlags
        getLayoutParams(toolbar).topMargin = if (insetFlags.hasTopInset()) 0 else topInset
        getLayoutParams(coordinatorLayout).bottomMargin = if (insetFlags.hasBottomInset()) 0 else bottomInset

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
    }

    private fun onFragmentViewCreated(v: View, f: Fragment) {
        if (isNotInMainFragmentContainer(v)) return

        val fragment = f as AppBaseFragment
        if (fragment.restoredFromBackStack()) adjustInsetForFragment(f)

        setOnApplyWindowInsetsListener(v) { _, insets -> consumeFragmentInsets(insets) }
    }

    companion object {

        private const val UI_STATE = "APP_UI_STATE"
        const val ANIMATION_DURATION = 300

        var topInset: Int = 0
        var bottomInset: Int = 0
    }
}
