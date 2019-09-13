package com.tunjid.androidbootstrap

import androidx.fragment.app.FragmentTransaction

fun FragmentTransaction.crossFade() = setCustomAnimations(
        android.R.anim.fade_in,
        android.R.anim.fade_out,
        android.R.anim.fade_in,
        android.R.anim.fade_out
)

fun FragmentTransaction.slide(toTheRight: Boolean) = setCustomAnimations(
        if (toTheRight) R.anim.slide_in_right else R.anim.slide_in_left,
        if (toTheRight) R.anim.slide_out_left else R.anim.slide_out_right,
        if (toTheRight) R.anim.slide_in_left else R.anim.slide_in_right,
        if (toTheRight) R.anim.slide_out_right else R.anim.slide_out_left
)
