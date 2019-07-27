package com.tunjid.androidbootstrap.view.util

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar

var TOOLBAR_ANIM_DELAY = 200L

var Toolbar.animDuration: Long
    get() = TOOLBAR_ANIM_DELAY
    set(value) { TOOLBAR_ANIM_DELAY = value }

fun Toolbar.update(title: CharSequence, @MenuRes menu: Int = 0) {
    if (visibility != View.VISIBLE) {
        setTitle(title)
        replaceMenu(menu)
    } else for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child is ImageView) continue

        child.animate().alpha(0F).setDuration(animDuration).withEndAction {
            if (child is TextView) setTitle(title)
            else if (child is ActionMenuView) replaceMenu(menu)

            child.animate()
                    .setDuration(animDuration)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .alpha(1F)
                    .start()
        }.start()
    }
}

fun Toolbar.replaceMenu(menu: Int) {
    this.menu.clear()
    if (menu != 0) inflateMenu(menu)
}