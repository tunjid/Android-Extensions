package com.tunjid.androidx.activities

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tunjid.androidx.R
import com.tunjid.androidx.fragments.RouteFragment
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.multiStackNavigationController
import com.tunjid.androidx.uidrivers.GlobalUiController
import com.tunjid.androidx.uidrivers.InsetLifecycleCallbacks
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.crossFade
import com.tunjid.androidx.uidrivers.globalUiDriver

class MainActivity : AppCompatActivity(R.layout.activity_main), GlobalUiController, Navigator.NavigationController {

    override val navigator: MultiStackNavigator by multiStackNavigationController(
            tabs.size,
            R.id.content_container
    ) { index -> RouteFragment.newInstance(index).let { it to it.stableTag } }

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
            navigator.stackSelectedListener = { menu.findItem(tabs[it])?.isChecked = true }
            navigator.transactionModifier = { incomingFragment ->
                val current = navigator.currentFragment
                if (current is Navigator.TransactionModifier) current.augmentTransaction(this, incomingFragment)
                else crossFade()
            }
            navigator.stackTransactionModifier = { crossFade() }

            setOnApplyWindowInsetsListener { _: View?, windowInsets: WindowInsets? -> windowInsets }
            setOnNavigationItemSelectedListener { navigator.show(tabs.indexOf(it.itemId)).let { true } }
            setOnNavigationItemReselectedListener { navigator.activeNavigator.clear() }
        }

        onBackPressedDispatcher.addCallback(this) { if (!navigator.pop()) finish() }
    }

    companion object {
        val tabs = intArrayOf(R.id.menu_navigation, R.id.menu_recyclerview, R.id.menu_communications, R.id.menu_misc)
    }

}
