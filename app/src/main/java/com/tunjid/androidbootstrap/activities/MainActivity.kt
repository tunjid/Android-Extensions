package com.tunjid.androidbootstrap.activities

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.core.components.MultiStackNavigator
import com.tunjid.androidbootstrap.core.components.StackNavigator
import com.tunjid.androidbootstrap.core.components.multipleStackNavigator
import com.tunjid.androidbootstrap.fragments.RouteFragment
import com.tunjid.androidbootstrap.uidrivers.*

class MainActivity : AppCompatActivity(R.layout.activity_main), GlobalUiController, StackNavigator.NavigationController {

    private val multiStackNavigator: MultiStackNavigator by multipleStackNavigator(
            R.id.content_container,
            intArrayOf(R.id.menu_core, R.id.menu_recyclerview, R.id.menu_communications)
    ) { id -> RouteFragment.newInstance(id).let { it to it.stableTag } }

    override val navigator: StackNavigator
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
        ), true)

        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            multiStackNavigator.stackSelectedListener = { menu.findItem(it)?.isChecked = true }
            multiStackNavigator.transactionModifier = { incomingFragment ->
                val current = navigator.currentFragment
                if (current is StackNavigator.TransactionModifier) current.augmentTransaction(this, incomingFragment)
                else crossFade()
            }
            multiStackNavigator.stackTransactionModifier = { crossFade() }
            setOnApplyWindowInsetsListener { _: View?, windowInsets: WindowInsets? -> windowInsets }
            setOnNavigationItemSelectedListener { multiStackNavigator.show(it.itemId).let { true } }
            setOnNavigationItemReselectedListener { multiStackNavigator.currentNavigator.clear() }
        }

        onBackPressedDispatcher.addCallback(this) { if (!multiStackNavigator.pop()) finish() }

        uiState = savedInstanceState?.getParcelable(UI_STATE) ?: UiState.freshState()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(UI_STATE, uiState)
        super.onSaveInstanceState(outState)
    }

    companion object {

        private const val UI_STATE = "APP_UI_STATE"
    }
}
