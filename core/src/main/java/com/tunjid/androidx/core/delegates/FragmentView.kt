package com.tunjid.androidx.core.delegates

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T : ViewBinding> viewLifecycle(factory: (View) -> T): ReadOnlyProperty<Fragment, T> =
    FragmentViewBindingDelegate(factory)

private class FragmentViewBindingDelegate<T : ViewBinding>(private val factory: (View) -> T) : ReadOnlyProperty<Fragment, T> {
    private var backingValue: T? = null

    override operator fun getValue(thisRef: Fragment, property: KProperty<*>): T = when (val binding = backingValue) {
        null -> {
            thisRef.requireFragmentManager().registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewDestroyed(fm: FragmentManager, fragment: Fragment) {
                    if (fragment != thisRef) return
                    backingValue = null
                    fm.unregisterFragmentLifecycleCallbacks(this)
                }
            }, false)
            factory(thisRef.requireView()).apply { backingValue = binding }
        }
        else -> binding
    }
}