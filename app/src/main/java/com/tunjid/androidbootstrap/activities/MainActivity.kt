package com.tunjid.androidbootstrap.activities

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidbootstrap.*
import com.tunjid.androidbootstrap.core.components.FragmentStackNavigator
import com.tunjid.androidbootstrap.core.components.MultiStackNavigator
import com.tunjid.androidbootstrap.core.components.multiStackNavigatorFor
import com.tunjid.androidbootstrap.fragments.RouteFragment

class MainActivity : AppCompatActivity(R.layout.activity_main), GlobalUiController, FragmentStackNavigator.NavigationController {

    private lateinit var insetLifecycleCallbacks: InsetLifecycleCallbacks

    private val multiStackNavigator: MultiStackNavigator by multiStackNavigatorFor(
            R.id.content_container,
            R.menu.menu_navigation
    ) { id -> RouteFragment.newInstance(id).let { it to it.stableTag } }

    override val navigator: FragmentStackNavigator
        get() = multiStackNavigator.currentNavigator

    override var uiState: UiState by globalUiDriver { navigator.currentFragment }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.registerFragmentLifecycleCallbacks(InsetLifecycleCallbacks(
                this::navigator,
                findViewById(R.id.constraint_layout),
                findViewById(R.id.content_container),
                findViewById(R.id.coordinator_layout),
                findViewById(R.id.toolbar),
                findViewById(R.id.top_inset),
                findViewById(R.id.bottom_inset),
                findViewById(R.id.keyboard_padding)
        ).apply { insetLifecycleCallbacks = this }, true)


        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            multiStackNavigator.stackSelectedListener = { menu.findItem(it)?.isChecked = true }
            multiStackNavigator.transactionModifier = { incomingFragment ->
                val current = navigator.currentFragment
                if (current is FragmentStackNavigator.TransactionModifier) current.augmentTransaction(this, incomingFragment)
                else crossFade()
            }
            multiStackNavigator.stackTransactionModifier = { crossFade() }
            setOnApplyWindowInsetsListener { _: View?, windowInsets: WindowInsets? -> windowInsets }
            setOnNavigationItemSelectedListener { multiStackNavigator.show(it.itemId).let { true } }
        }

        onBackPressedDispatcher.addCallback(this) { if (!multiStackNavigator.pop()) finish() }

        uiState = savedInstanceState?.getParcelable(UI_STATE) ?: UiState.freshState()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(UI_STATE, uiState)
        super.onSaveInstanceState(outState)
    }

    fun FragmentTransaction.crossFade() = setCustomAnimations(
            android.R.anim.fade_in,
            android.R.anim.fade_out,
            android.R.anim.fade_in,
            android.R.anim.fade_out
    )

    fun showSnackBar(consumer: (Snackbar) -> Unit) = insetLifecycleCallbacks.showSnackBar(consumer)

    companion object {

        private const val UI_STATE = "APP_UI_STATE"
        const val ANIMATION_DURATION = 300

        var topInset: Int = 0
        var bottomInset: Int = 0
    }
}
