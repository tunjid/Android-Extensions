package com.tunjid.androidx.baseclasses

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.activityNavigationController
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
        Navigator.NavigationController {

    private var activityUiState by activityGlobalUiController()

    override val insetFlags: InsetFlags = InsetFlags.ALL

    override val stableTag: String = javaClass.simpleName

    override val navigator by activityNavigationController()

    override var uiState: UiState
        get() = activityUiState
        set(value) {
            if (navigator.currentFragment === this) activityUiState = value
        }

}
