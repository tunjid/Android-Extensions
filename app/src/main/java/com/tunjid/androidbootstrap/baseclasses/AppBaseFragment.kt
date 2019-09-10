package com.tunjid.androidbootstrap.baseclasses

import android.annotation.SuppressLint
import android.transition.*
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidbootstrap.activities.MainActivity
import com.tunjid.androidbootstrap.activities.MainActivity.Companion.ANIMATION_DURATION
import com.tunjid.androidbootstrap.core.components.FragmentStackNavigator
import com.tunjid.androidbootstrap.view.util.InsetFlags

abstract class AppBaseFragment(
        @LayoutRes contentLayoutId: Int = 0
) : Fragment(contentLayoutId),
        FragmentStackNavigator.FragmentTagProvider,
        FragmentStackNavigator.FragmentTransactionProvider {

    open val insetFlags: InsetFlags = InsetFlags.ALL

    override val stableTag: String
        get() = javaClass.simpleName

    private val hostingActivity: MainActivity
        get() = requireActivity() as MainActivity

    val transitionFragmentManager: FragmentManager
        get() = parentFragment?.childFragmentManager ?: requireActivity().supportFragmentManager

    override fun onDestroyView() {
        super.onDestroyView()
        arguments?.putBoolean(VIEW_DESTROYED, true)
    }

    fun showFragment(fragment: AppBaseFragment): Boolean =
            hostingActivity.currentStackNavigator?.show(fragment, provideFragmentTransaction(fragment))
                    ?: false

    protected fun showSnackbar(consumer: (Snackbar) -> Unit) =
            hostingActivity.showSnackBar(consumer)

    protected fun baseSharedTransition(): Transition = TransitionSet()
            .setOrdering(TransitionSet.ORDERING_TOGETHER)
            .addTransition(ChangeImageTransform())
            .addTransition(ChangeTransform())
            .addTransition(ChangeBounds())
            .setDuration(ANIMATION_DURATION.toLong())

    @SuppressLint("CommitTransaction")
    override fun provideFragmentTransaction(fragmentTo: Fragment): FragmentTransaction? =
            transitionFragmentManager.beginTransaction().setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            )

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

        val NO_BOTTOM: InsetFlags = InsetFlags(hasLeftInset = true, hasTopInset = true, hasRightInset = true, hasBottomInset = false)
    }

}
