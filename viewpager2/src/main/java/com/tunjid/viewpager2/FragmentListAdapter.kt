package com.tunjid.viewpager2

import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tunjid.androidx.recyclerview.diff.DiffAdapterCallback

/**
 * Represents a type that maps to a `Fragment`. You want to use a data class for this, or at the
 * very least a class that has stable implementations of [Any.equals] and [Any.hashCode].
 */
interface FragmentTab {
    fun title(res: Resources): CharSequence
    fun createFragment(): Fragment
}

// Uniquely identify tabs by hashing fully qualified name along with their contents
private val FragmentTab.itemId: Long get() = "${this.javaClass.name}-${hashCode()}".hashCode().toLong()

fun <T : FragmentTab> Fragment.fragmentListAdapterOf(
    initialTabs: List<T>? = null,
    lifecycle: Lifecycle = viewLifecycleOwner.lifecycle
) = FragmentListAdapter(
    fragmentManager = childFragmentManager,
    lifecycle = lifecycle,
    initialTabs = initialTabs,
    resources = this.resources
)

@Suppress("unused")
fun <T : FragmentTab> FragmentActivity.fragmentListAdapterOf(
    initialTabs: List<T>? = null,
    lifecycle: Lifecycle = this.lifecycle
) = FragmentListAdapter(
    fragmentManager = supportFragmentManager,
    lifecycle = lifecycle,
    initialTabs = initialTabs,
    resources = this.resources
)

/**
 * A [FragmentStateAdapter] that uses diff util to efficiently dispatch changes to the underlying
 * adapter
 */
class FragmentListAdapter<T : FragmentTab> internal constructor(
    initialTabs: List<T>? = null,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val resources: Resources
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val differ = AsyncListDiffer(
        AdapterListUpdateCallback(this),
        AsyncDifferConfig.Builder(DiffAdapterCallback<T>()).build()
    )

    init {
        initialTabs?.let(this::submitList)
    }

    private val tabs: List<T> get() = differ.currentList

    fun submitList(tabs: List<T>) = differ.submitList(tabs)

    fun getPageTitle(position: Int): CharSequence = tabs[position].title(resources)

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment = tabs[position].createFragment()

    override fun getItemId(position: Int): Long = tabs[position].itemId

    override fun containsItem(itemId: Long): Boolean = tabs.any { it.itemId == itemId }
}
