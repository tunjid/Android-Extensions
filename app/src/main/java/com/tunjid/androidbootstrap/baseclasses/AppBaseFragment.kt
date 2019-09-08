package com.tunjid.androidbootstrap.baseclasses

import android.annotation.SuppressLint
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Transition
import android.transition.TransitionSet
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidbootstrap.activities.MainActivity
import com.tunjid.androidbootstrap.activities.MainActivity.Companion.ANIMATION_DURATION
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.view.util.InsetFlags

abstract class AppBaseFragment(@LayoutRes contentLayoutId: Int = 0) : BaseFragment(contentLayoutId) {

    open val insetFlags: InsetFlags = InsetFlags.ALL

    private val hostingActivity: MainActivity
        get() = requireActivity() as MainActivity

    protected fun showSnackbar(consumer: (Snackbar) -> Unit) =
            hostingActivity.showSnackBar(consumer)

    protected fun baseSharedTransition(): Transition = TransitionSet()
            .setOrdering(TransitionSet.ORDERING_TOGETHER)
            .addTransition(ChangeImageTransform())
            .addTransition(ChangeTransform())
            .addTransition(ChangeBounds())
            .setDuration(ANIMATION_DURATION.toLong())

    @SuppressLint("CommitTransaction")
    override fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? =
            requireActivity().supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                            android.R.anim.fade_in, android.R.anim.fade_out)

    companion object {
        const val BACKGROUND_TINT_DURATION = 1200
        val NO_BOTTOM: InsetFlags = InsetFlags.create(true, true, true, false)
    }

}
