package com.tunjid.viewpager2

import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tunjid.androidx.recyclerview.diff.DiffAdapterCallback

interface FragmentTab {
    fun title(res: Resources): CharSequence
    fun createFragment(): Fragment
}

// We use toString to avoid hash code collisions for data classes, which are calculated purely based
// on constructor arguments, not on class name. This means that different data classes with the same
// argument have colliding hash codes
val FragmentTab.itemId: Long get() = toString().hashCode().toLong()

class FragmentListAdapter<T : FragmentTab> : FragmentStateAdapter {

    private val resources: Resources

    private val differ = AsyncListDiffer(
        AdapterListUpdateCallback(this),
        AsyncDifferConfig.Builder(DiffAdapterCallback<T>()).build()
    )

    constructor(activity: FragmentActivity) : super(activity.supportFragmentManager, activity.lifecycle) {
        resources = activity.resources
    }

    constructor(fragment: Fragment) : super(fragment.childFragmentManager, fragment.viewLifecycleOwner.lifecycle) {
        resources = fragment.resources
    }

    private val tabs: List<T> get() = differ.currentList

    fun submitList(tabs: List<T>) = differ.submitList(tabs)

    fun getPageTitle(position: Int): CharSequence = tabs[position].title(resources)

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment = tabs[position].createFragment()

    override fun getItemId(position: Int): Long = tabs[position].itemId

    override fun containsItem(itemId: Long): Boolean = tabs.any { it.itemId == itemId }
}
