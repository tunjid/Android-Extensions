package com.tunjid.androidbootstrap.baseclasses

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Transition
import android.transition.TransitionSet
import android.view.View
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.UiState
import com.tunjid.androidbootstrap.activities.MainActivity
import com.tunjid.androidbootstrap.activities.MainActivity.Companion.ANIMATION_DURATION
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.view.util.InsetFlags
import io.reactivex.disposables.CompositeDisposable

abstract class AppBaseFragment : BaseFragment() {

    protected val disposables = CompositeDisposable()

    protected open val fabIconRes: Int
        @DrawableRes get() = R.drawable.ic_circle_24dp

    protected open val fabText: CharSequence
        get() = getString(R.string.app_name)

    open val toolBarMenuRes: Int
        @MenuRes get() = 0

    open val insetFlags: InsetFlags = InsetFlags.ALL

    open val showsFab: Boolean = false

    open val showsToolBar: Boolean = true

    protected open val title: String
        get() = this::class.java.simpleName

    protected var isFabExtended: Boolean
        get() = hostingActivity.isFabExtended
        set(extended) {
            hostingActivity.isFabExtended = extended
        }

    @get:ColorInt
    open val navBarColor: Int
        get() = ContextCompat.getColor(requireContext(), R.color.white_75)

    protected open val fabClickListener: View.OnClickListener
        get() = View.OnClickListener { }

    private val hostingActivity: MainActivity
        get() = requireActivity() as MainActivity

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    open fun togglePersistentUi() {
        hostingActivity.update(fromThis())
        if (!restoredFromBackStack()) isFabExtended = true
    }

    protected fun showSnackbar(consumer: (Snackbar) -> Unit) =
            hostingActivity.showSnackBar(consumer)

    protected fun baseSharedTransition(): Transition = TransitionSet()
            .setOrdering(TransitionSet.ORDERING_TOGETHER)
            .addTransition(ChangeImageTransform())
            .addTransition(ChangeTransform())
            .addTransition(ChangeBounds())
            .setDuration(ANIMATION_DURATION.toLong())

    protected fun <T : View> tintView(@ColorRes colorRes: Int, view: T, biConsumer: (Int, T) -> Unit) {
        val endColor = ContextCompat.getColor(requireContext(), colorRes)
        val startColor = Color.TRANSPARENT

        val animator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        animator.duration = BACKGROUND_TINT_DURATION.toLong()
        animator.addUpdateListener { animation ->
            (animation.animatedValue as? Int)?.let { biConsumer.invoke(it, view) }
        }
        animator.start()
    }

    @SuppressLint("CommitTransaction")
    override fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? =
            requireActivity().supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                            android.R.anim.fade_in, android.R.anim.fade_out)

    private fun fromThis(): UiState = UiState(
            this.fabIconRes,
            this.fabText,
            this.toolBarMenuRes,
            this.navBarColor,
            this.showsFab,
            this.showsToolBar,
            this.insetFlags,
            this.title,
            if (view == null) null else fabClickListener
    )

    companion object {
        const val BACKGROUND_TINT_DURATION = 1200
        val NO_BOTTOM: InsetFlags = InsetFlags.create(true, true, true, false)
    }

}
