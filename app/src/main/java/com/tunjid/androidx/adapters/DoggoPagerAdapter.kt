package com.tunjid.androidx.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tunjid.androidx.fragments.DoggoFragment
import com.tunjid.androidx.model.Doggo

class DoggoPagerAdapter(private val doggos: List<Doggo>, fragment: Fragment)
    : FragmentStateAdapter(fragment.childFragmentManager, fragment.viewLifecycleOwner.lifecycle) {

    override fun getItemCount(): Int = this.doggos.size

    override fun getItemId(position: Int): Long = doggos[position].hashCode().toLong()

    override fun containsItem(itemId: Long): Boolean = doggos.map(Doggo::hashCode).contains(itemId.toInt())

    override fun createFragment(position: Int): Fragment = DoggoFragment.newInstance(doggos[position])
}
