package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.recyclerview.RecyclerViewMultiScroller
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.InsetFlags.Companion.NO_BOTTOM
import com.tunjid.androidx.viewholders.SpreadsheetRowViewHolder
import com.tunjid.androidx.viewmodels.SpreadsheetViewModel
import com.tunjid.androidx.viewmodels.routeName

class SpreadsheetFragment : AppBaseFragment(R.layout.fragment_route) {

    private val viewModel by viewModels<SpreadsheetViewModel>()

    override val insetFlags: InsetFlags = NO_BOTTOM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarShows = true,
                toolBarMenu = 0,
                fabShows = false,
                showsBottomNav = false,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            val viewPool = RecyclerView.RecycledViewPool()
            val scroller = RecyclerViewMultiScroller()

            layoutManager = verticalLayoutManager()
            adapter = adapterOf(
                    itemsSource = viewModel::rows,
                    viewHolderCreator = { parent, _ -> SpreadsheetRowViewHolder(parent, scroller, viewPool)},
                    viewHolderBinder = { viewHolder, tile, _ -> viewHolder.bind(tile) },
                    itemIdFunction = { it.index.toLong() }
            )
        }

    }

    companion object {
        fun newInstance(): SpreadsheetFragment = SpreadsheetFragment().apply { arguments = Bundle() }
    }
}
