package com.tunjid.androidx.tablists.tiles

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.tunjid.androidx.R
import com.tunjid.androidx.core.components.doOnEveryEvent
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.databinding.FragmentRouteBinding
import com.tunjid.androidx.databinding.ViewholderTileBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.map
import com.tunjid.androidx.mapDistinct
import com.tunjid.androidx.recyclerview.gridLayoutManager
import com.tunjid.androidx.recyclerview.listAdapterOf
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.uidrivers.InsetFlags.Companion.NO_BOTTOM
import com.tunjid.androidx.uidrivers.SpringItemAnimator
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.callback
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.updatePartial

class ShiftingTilesFragment : Fragment(R.layout.fragment_route) {

    private var isTopLevel by fragmentArgs<Boolean>()
    private val viewModel by viewModels<ShiftingTileViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isTopLevel) uiState = UiState(
            toolbarTitle = this::class.java.routeName,
            toolbarShows = true,
            toolbarMenuRes = 0,
            fabShows = true,
            showsBottomNav = false,
            insetFlags = NO_BOTTOM,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
            fabClickListener = viewLifecycleOwner.callback { viewModel.toggleChanges() }
        )
        else viewLifecycleOwner.lifecycle.doOnEveryEvent(Lifecycle.Event.ON_RESUME) {
            ::uiState.updatePartial {
                copy(
                    fabShows = true,
                    fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
                    fabClickListener = viewLifecycleOwner.callback { viewModel.toggleChanges() }
                )
            }
        }

        val tileAdapter = listAdapterOf(
            initialItems = viewModel.state.value?.tiles ?: listOf(),
            viewHolderCreator = { parent, _ ->
                parent.tileViewHolder().apply {
                    itemView.setOnClickListener { uiState = uiState.copy(snackbarText = tile.diffId) }
                }
            },
            viewHolderBinder = { viewHolder, tile, _ -> viewHolder.bind(tile) },
            itemIdFunction = { it.hashCode().toLong() },
            onViewHolderRecycled = BindingViewHolder<ViewholderTileBinding>::unbind,
            onViewHolderDetached = BindingViewHolder<ViewholderTileBinding>::unbind
        )

        FragmentRouteBinding.bind(view).recyclerView.apply {
            adapter = tileAdapter
            layoutManager = gridLayoutManager(4)
            itemAnimator = SpringItemAnimator()
        }

        viewModel.state.apply {
            mapDistinct(ShiftingState::tiles).observe(viewLifecycleOwner, tileAdapter::submitList)
            mapDistinct(ShiftingState::fabIconRes).observe(viewLifecycleOwner) { ::uiState.updatePartial { copy(fabIcon = it) } }
            mapDistinct(ShiftingState::fabText).map(::getString).observe(viewLifecycleOwner) { ::uiState.updatePartial { copy(fabText = it) } }
        }
    }

    companion object {
        fun newInstance(isTopLevel: Boolean): ShiftingTilesFragment = ShiftingTilesFragment().apply { this.isTopLevel = isTopLevel }
    }
}
