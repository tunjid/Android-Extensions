package com.tunjid.androidx.baseclasses

import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.tunjid.androidx.R
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.activityNavigatorController
import com.tunjid.androidx.uidrivers.GlobalUiController
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.activityGlobalUiController

abstract class AppBaseFragment(
        @LayoutRes contentLayoutId: Int = 0
) : Fragment(contentLayoutId),
        GlobalUiController,
        Navigator.TagProvider,
        Navigator.Controller {

    override val stableTag: String = javaClass.simpleName

    override val navigator by activityNavigatorController<MultiStackNavigator>()

    override var uiState: UiState by activityGlobalUiController()

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_reset -> navigator.clearAll().let { true }
        else -> super.onOptionsItemSelected(item)
    }
}
