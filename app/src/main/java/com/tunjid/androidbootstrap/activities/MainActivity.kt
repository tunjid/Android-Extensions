package com.tunjid.androidbootstrap.activities

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.core.components.MultiStackNavigator
import com.tunjid.androidbootstrap.core.components.Navigator
import com.tunjid.androidbootstrap.core.components.multiStackNavigator
import com.tunjid.androidbootstrap.fragments.RouteFragment
import com.tunjid.androidbootstrap.uidrivers.GlobalUiController
import com.tunjid.androidbootstrap.uidrivers.InsetLifecycleCallbacks
import com.tunjid.androidbootstrap.uidrivers.UiState
import com.tunjid.androidbootstrap.uidrivers.crossFade
import com.tunjid.androidbootstrap.uidrivers.globalUiDriver

class MainActivity : AppCompatActivity(R.layout.activity_main), GlobalUiController, Navigator.NavigationController {

    override val navigator: MultiStackNavigator by multiStackNavigator(
            R.id.content_container,
            intArrayOf(R.id.menu_core, R.id.menu_recyclerview, R.id.menu_communications)
    ) { id -> RouteFragment.newInstance(id).let { it to it.stableTag } }

    override var uiState: UiState by globalUiDriver { navigator.currentFragment }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.registerFragmentLifecycleCallbacks(InsetLifecycleCallbacks(
                this.navigator::activeNavigator,
                findViewById(R.id.constraint_layout),
                findViewById(R.id.content_container),
                findViewById(R.id.coordinator_layout),
                findViewById(R.id.toolbar),
                findViewById(R.id.top_inset),
                findViewById(R.id.bottom_inset),
                findViewById(R.id.keyboard_padding)
        ), true)

        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            navigator.stackSelectedListener = { menu.findItem(it)?.isChecked = true }
            navigator.transactionModifier = { incomingFragment ->
                val current = navigator.currentFragment
                if (current is Navigator.TransactionModifier) current.augmentTransaction(this, incomingFragment)
                else crossFade()
            }
            navigator.stackTransactionModifier = { crossFade() }
            setOnApplyWindowInsetsListener { _: View?, windowInsets: WindowInsets? -> windowInsets }
            setOnNavigationItemSelectedListener { navigator.show(it.itemId).let { true } }
            setOnNavigationItemReselectedListener { navigator.activeNavigator.clear() }
        }

        onBackPressedDispatcher.addCallback(this) { if (!navigator.pop()) finish() }
    }

}
