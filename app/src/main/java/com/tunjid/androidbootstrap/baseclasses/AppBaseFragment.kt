package com.tunjid.androidbootstrap.baseclasses

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidbootstrap.activities.MainActivity
import com.tunjid.androidbootstrap.core.components.StackNavigator
import com.tunjid.androidbootstrap.uidrivers.InsetProvider
import com.tunjid.androidbootstrap.view.util.InsetFlags

abstract class AppBaseFragment(
        @LayoutRes contentLayoutId: Int = 0
) : Fragment(contentLayoutId),
        InsetProvider,
        StackNavigator.TagProvider {

    override val insetFlags: InsetFlags = InsetFlags.ALL

    override val stableTag: String
        get() = javaClass.simpleName

    private val hostingActivity: MainActivity
        get() = requireActivity() as MainActivity

    override fun onDestroyView() {
        super.onDestroyView()
        arguments?.putBoolean(VIEW_DESTROYED, true)
    }

    protected fun showSnackbar(consumer: (Snackbar) -> Unit) =
            hostingActivity.showSnackBar(consumer)

    /**
     * Checks whether this fragment was shown before and it's view subsequently
     * destroyed by placing it in the back stack
     */
    fun restoredFromBackStack(): Boolean {
        val args = arguments
        return (args != null && args.containsKey(VIEW_DESTROYED)
                && args.getBoolean(VIEW_DESTROYED))
    }

    companion object {
        const val BACKGROUND_TINT_DURATION = 1200
        private const val VIEW_DESTROYED = "com.tunjid.androidbootstrap.core.abstractclasses.basefragment.view.destroyed"

    }

}
