package com.tunjid.androidx.tablists.tables

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.core.delegates.viewLifecycle
import com.tunjid.androidx.databinding.FragmentSpreadsheetParentBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.mapDistinct
import com.tunjid.androidx.material.viewpager.configureWith
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.callback
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.viewpager2.FragmentListAdapter

class ViewPagerListAdapterFragment : Fragment(R.layout.fragment_spreadsheet_parent),
    Navigator.TransactionModifier {

    private var isTopLevel by fragmentArgs<Boolean>()
    private val binding by viewLifecycle(FragmentSpreadsheetParentBinding::bind)
    private val viewModel by viewModels<ViewPagerListAdapterViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = UiState(
            toolbarTitle = this::class.java.routeName,
            toolbarShows = true,
            toolbarMenuRes = R.menu.menu_viewpager,
            toolbarMenuClickListener = viewLifecycleOwner.callback {
                when (it.itemId) {
                    R.id.menu_shuffle -> viewModel.accept(Input.Shuffle)
                    R.id.menu_edit -> {
                        val state = viewModel.state.value
                        val allItems = state?.allItems ?: listOf()
                        val (items, checkedItems) = state.items(view.context.resources)
                        MaterialAlertDialogBuilder(view.context)
                            .setMultiChoiceItems(items, checkedItems) { _, index, isChecked ->
                                viewModel.accept(when (isChecked) {
                                    true -> Input.Add(allItems[index])
                                    false -> Input.Remove(allItems[index])
                                })
                            }
                            .show()
                    }
                }
            },
            showsBottomNav = false,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
        )

        val viewPager = binding.viewPager
        val pagerAdapter = FragmentListAdapter<RouteTab>(fragment = this)


        viewPager.adapter = pagerAdapter

        binding.tabs.configureWith(binding.viewPager) { tab, position ->
            tab.text = pagerAdapter.getPageTitle(position)
        }

        viewModel.state.apply {
            mapDistinct(State::visibleItems).observe(viewLifecycleOwner, pagerAdapter::submitList)
            mapDistinct(State::visibleItems).mapDistinct { it.size }.observe(viewLifecycleOwner) { size ->
                binding.tabs.tabMode = when (size) {
                    in 0..3 -> TabLayout.MODE_FIXED
                    else -> TabLayout.MODE_SCROLLABLE
                }
            }
        }
    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        val fragment = childFragmentManager.findFragmentByTag("f${binding.viewPager.adapter?.getItemId(binding.viewPager.currentItem)}")
        (fragment as? Navigator.TransactionModifier)?.augmentTransaction(transaction, incomingFragment)
    }

    companion object {
        fun newInstance(isTopLevel: Boolean): ViewPagerListAdapterFragment = ViewPagerListAdapterFragment().apply { this.isTopLevel = isTopLevel }
    }
}
