package com.tunjid.androidx.baseclasses

import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.tunjid.androidx.R
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.activityNavigatorController
import com.tunjid.androidx.uidrivers.GlobalUiController
import com.tunjid.androidx.uidrivers.InsetProvider
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.activityGlobalUiController
import com.tunjid.androidx.view.util.InsetFlags

abstract class AppBaseFragment(
        @LayoutRes contentLayoutId: Int = 0
) : Fragment(contentLayoutId),
        InsetProvider,
        GlobalUiController,
        Navigator.TagProvider,
        Navigator.Controller {

    override val insetFlags: InsetFlags = InsetFlags.ALL

    override val stableTag: String = javaClass.simpleName

    override val navigator by activityNavigatorController<MultiStackNavigator>()

    override var uiState: UiState by activityGlobalUiController()

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_reset -> navigator.clearAll().let { true }
        else -> super.onOptionsItemSelected(item)
    }

}
