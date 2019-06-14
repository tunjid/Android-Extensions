package com.tunjid.androidbootstrap.baseclasses

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.snackbar.Snackbar
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.activities.MainActivity
import com.tunjid.androidbootstrap.activities.MainActivity.Companion.ANIMATION_DURATION
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator.GlyphState
import com.tunjid.androidbootstrap.view.util.InsetFlags
import io.reactivex.disposables.CompositeDisposable

abstract class AppBaseFragment : BaseFragment() {

    companion object {
        private const val BACKGROUND_TINT_DURATION = 1200
        val NO_BOTTOM: InsetFlags = InsetFlags.create(true, true, true, false)
    }

    protected val disposables = CompositeDisposable()

    protected var isFabExtended: Boolean
        get() = hostingActivity.isFabExtended
        set(extended) {
            hostingActivity.isFabExtended = extended
        }

    @get:ColorInt
    open val navBarColor: Int
        get() = ContextCompat.getColor(requireContext(), R.color.white_75)

    protected open val title: String
        get() = javaClass.simpleName

    protected open val fabState: GlyphState
        get() = FabExtensionAnimator.newState(getText(R.string.app_name), getDrawable(requireContext(), R.drawable.ic_circle_24dp))

    protected open val fabClickListener: View.OnClickListener
        get() = View.OnClickListener { }

    private val hostingActivity: MainActivity
        get() = requireActivity() as MainActivity

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    open fun toggleFab(show: Boolean) {
        hostingActivity.toggleFab(show)
    }

    open fun toggleToolbar(show: Boolean) {
        hostingActivity.toggleToolbar(show)
    }

    open fun insetFlags(): InsetFlags {
        return InsetFlags.ALL
    }

    open fun showsFab(): Boolean {
        return false
    }

    open fun showsToolBar(): Boolean {
        return true
    }

    open fun togglePersistentUi() {
        toggleFab(showsFab())
        toggleToolbar(showsToolBar())
        if (!restoredFromBackStack()) isFabExtended = true

        val hostingActivity = hostingActivity
        hostingActivity.setTitle(title)
        hostingActivity.updateFab(fabState)
        hostingActivity.setFabClickListener(fabClickListener)
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
    override fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? {
        return requireActivity().supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
    }

}
