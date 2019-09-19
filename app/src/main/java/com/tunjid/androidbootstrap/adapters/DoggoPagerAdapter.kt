package com.tunjid.androidbootstrap.adapters

import com.tunjid.androidbootstrap.fragments.DoggoFragment
import com.tunjid.androidbootstrap.model.Doggo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class DoggoPagerAdapter(private val doggos: List<Doggo>, fm: FragmentManager)
    : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = DoggoFragment.newInstance(doggos[position])

    override fun getCount(): Int = this.doggos.size

    override fun getPageTitle(position: Int): CharSequence? = position.toString()
}
