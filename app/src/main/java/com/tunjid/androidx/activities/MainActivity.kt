package com.tunjid.androidx.activities

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.tunjid.androidx.R
import com.tunjid.androidx.databinding.ActivityMainBinding
import com.tunjid.androidx.fragments.RouteFragment
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.multiStackNavigationController
import com.tunjid.androidx.uidrivers.GlobalUiController
import com.tunjid.androidx.uidrivers.GlobalUiDriver
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.materialDepthAxisTransition
import com.tunjid.androidx.uidrivers.materialFadeThroughTransition
import leakcanary.AppWatcher
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), GlobalUiController, Navigator.Controller {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val globalUiDriver by lazy { GlobalUiDriver(this, binding, navigator) }

    override val navigator: MultiStackNavigator by multiStackNavigationController(
            tabs.size,
            R.id.content_container,
            RouteFragment.Companion::newInstance
    )

    override var uiState: UiState
        get() = globalUiDriver.uiState
        set(value) {
            globalUiDriver.uiState = value
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        AppWatcher.config = AppWatcher.config.copy(watchDurationMillis = TimeUnit.SECONDS.toMillis(8))

        // Add this before on create to make sure fragment callbacks are added after.
        // This makes Fragment back pressed callbacks take higher precedence.
        onBackPressedDispatcher.addCallback(this) { if (!navigator.pop()) finish() }

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.bottomNavigation.apply {
            navigator.stackSelectedListener = { menu.findItem(tabs[it])?.isChecked = true }
            navigator.stackTransactionModifier = navigator.materialFadeThroughTransition()
            navigator.transactionModifier = navigator.materialDepthAxisTransition()

            // Swallow insets, don't allow default behavior
            setOnApplyWindowInsetsListener { _: View?, windowInsets: WindowInsets? -> windowInsets }
            setOnNavigationItemSelectedListener { navigator.show(tabs.indexOf(it.itemId)).let { true } }
            setOnNavigationItemReselectedListener { navigator.activeNavigator.clear() }
        }
    }

    companion object {
        val tabs = intArrayOf(R.id.menu_navigation, R.id.menu_recyclerview, R.id.menu_communications, R.id.menu_misc)
    }
}
