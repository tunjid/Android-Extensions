package com.tunjid.androidx.material.viewpager

import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

fun TabLayout.configureWith(
    viewPager: ViewPager2,
    autoRefresh: Boolean = true,
    smoothScroll: Boolean = true,
    tabConfigurationStrategy: (tab: TabLayout.Tab, position: Int) -> Unit) {
    val mediator = TabLayoutMediator(this, viewPager, autoRefresh, smoothScroll, tabConfigurationStrategy)
    mediator.attach()
    viewPager.doOnAttach { it.doOnDetach { mediator.detach() } }
}