package com.tunjid.androidbootstrap.baseclasses

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.tunjid.androidbootstrap.core.components.Navigator
import com.tunjid.androidbootstrap.uidrivers.InsetProvider
import com.tunjid.androidbootstrap.view.util.InsetFlags

abstract class AppBaseFragment(
        @LayoutRes contentLayoutId: Int = 0
) : Fragment(contentLayoutId),
        InsetProvider,
        Navigator.TagProvider {

    override val insetFlags: InsetFlags = InsetFlags.ALL

    override val stableTag: String = javaClass.simpleName

}
