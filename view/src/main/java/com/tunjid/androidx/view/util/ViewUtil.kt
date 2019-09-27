package com.tunjid.androidx.view.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.annotation.LayoutRes

val View.marginLayoutParams: ViewGroup.MarginLayoutParams
    get() = layoutParams as ViewGroup.MarginLayoutParams

fun ViewGroup.inflate(@LayoutRes res: Int): View =
        LayoutInflater.from(context).inflate(res, this, false)

fun View.hashTransitionName(`object`: Any): String =
        `object`.hashCode().toString() + "-" + id