package com.tunjid.androidx.uidrivers

import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Transition
import android.transition.TransitionSet
import androidx.core.transition.doOnCancel
import androidx.core.transition.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.tunjid.androidx.R
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator

const val BACKGROUND_TINT_DURATION = 1200L

fun FragmentTransaction.crossFade() = setCustomAnimations(
        android.R.anim.fade_in,
        android.R.anim.fade_out,
        android.R.anim.fade_in,
        android.R.anim.fade_out
)

fun MultiStackNavigator.materialFadeThroughTransition(): FragmentTransaction.(Int) -> Unit = fade@{ index ->
    val rootFragmentManager = current?.activity?.supportFragmentManager ?: return@fade

    rootFragmentManager.findFragmentByTag(activeIndex.toString())?.apply {
        enter = null
        if (exit !is MaterialFadeThrough) exit = MaterialFadeThrough.create(requireContext()).setDuration(300)
    }
    rootFragmentManager.findFragmentByTag(index.toString())?.apply {
        exit = null
        if (enter !is MaterialFadeThrough) enter = MaterialFadeThrough.create(requireContext()).setDuration(300)
    }
}

fun MultiStackNavigator.materialDepthAxisTransition(): FragmentTransaction.(Fragment) -> Unit = modifier@{ incomingFragment ->
    val current = current ?: return@modifier
    val context = current.requireContext()
    if (current is Navigator.TransactionModifier) current.augmentTransaction(this, incomingFragment)
    else {
        current.apply {
            enter = MaterialSharedAxis.create(context, MaterialSharedAxis.Z, false)
            exit = MaterialSharedAxis.create(context, MaterialSharedAxis.Z, true)
        }
        incomingFragment.apply {
            enter = MaterialSharedAxis.create(context, MaterialSharedAxis.Z, true)
            exit = MaterialSharedAxis.create(context, MaterialSharedAxis.Z, false)
        }
    }
}

fun FragmentTransaction.slide(toTheRight: Boolean) = setCustomAnimations(
        if (toTheRight) R.anim.slide_in_right else R.anim.slide_in_left,
        if (toTheRight) R.anim.slide_out_left else R.anim.slide_out_right,
        if (toTheRight) R.anim.slide_in_left else R.anim.slide_in_right,
        if (toTheRight) R.anim.slide_out_right else R.anim.slide_out_left
)

fun baseSharedTransition(): Transition = TransitionSet()
        .setOrdering(TransitionSet.ORDERING_TOGETHER)
        .addTransition(ChangeImageTransform())
        .addTransition(ChangeTransform())
        .addTransition(ChangeBounds())
        .setDuration(InsetLifecycleCallbacks.ANIMATION_DURATION.toLong())

private var Fragment.enter: Transition?
    get() = enterTransition as? Transition
    set(value) {
        enterTransition = value?.apply {
            doOnCancel { enterTransition = null }
            doOnEnd { enterTransition = null }
        }
    }

private var Fragment.exit: Transition?
    get() = exitTransition as? Transition
    set(value) {
        exitTransition = value?.apply {
            doOnCancel { exitTransition = null }
            doOnEnd { exitTransition = null }
        }
    }