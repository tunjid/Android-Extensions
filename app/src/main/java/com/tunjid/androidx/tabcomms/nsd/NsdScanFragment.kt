package com.tunjid.androidx.tabcomms.nsd


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import com.tunjid.androidx.PlaceHolder
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.databinding.FragmentNsdScanBinding
import com.tunjid.androidx.databinding.ViewholderNsdListBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.mapDistinct
import com.tunjid.androidx.recyclerview.listAdapterOf
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderDelegate
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.setLoading
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.uidrivers.callback
import com.tunjid.androidx.uidrivers.uiState

/**
 * A [Fragment] listing supported NSD servers
 */
class NsdScanFragment : Fragment(R.layout.fragment_nsd_scan) {

    private var isTopLevel by fragmentArgs<Boolean>()
    private val viewModel by viewModels<NsdViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isTopLevel) uiState = uiState.copy(
            toolbarTitle = this::class.java.routeName,
            toolbarMenuRefresher = viewLifecycleOwner.callback(::updateToolbarMenu),
            toolbarMenuClickListener = viewLifecycleOwner.callback(::onMenuItemSelected),
            toolbarMenuRes = R.menu.menu_nsd_scan,
            toolbarShows = true,
            fabShows = false,
            showsBottomNav = true,
            insetFlags = InsetFlags.ALL,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        val binding = FragmentNsdScanBinding.bind(view)
        val placeHolder = PlaceHolder(binding.placeholderContainer.root)
        placeHolder.bind(PlaceHolder.State(R.string.no_nsd_devices, R.drawable.ic_signal_wifi__24dp))

        binding.list.apply {
            val listAdapter = listAdapterOf(
                initialItems = viewModel.state.value?.items ?: listOf(),
                viewHolderCreator = { parent, _ -> parent.viewHolderFrom(ViewholderNsdListBinding::inflate) },
                viewHolderBinder = { viewHolder, service, _ -> viewHolder.bind(service) }
            )

            layoutManager = verticalLayoutManager()
            adapter = listAdapter

            addItemDecoration(DividerItemDecoration(requireActivity(), VERTICAL))

            viewModel.state.apply {
                mapDistinct(NSDState::items).observe(viewLifecycleOwner, listAdapter::submitList)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        scanDevices(true)
    }

    private fun updateToolbarMenu(menu: Menu) {
        viewModel.state.mapDistinct(NSDState::isScanning).observe(viewLifecycleOwner) { isScanning ->
            menu.findItem(R.id.menu_stop)?.isVisible = isScanning
            menu.findItem(R.id.menu_scan)?.isVisible = !isScanning

            val refresh = menu.findItem(R.id.menu_refresh)

            refresh?.isVisible = isScanning
            if (isScanning) refresh?.setLoading(requireContext().themeColorAt(R.attr.prominent_text_color))
        }
    }

    private fun onMenuItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_scan -> scanDevices(true)
        R.id.menu_stop -> scanDevices(false)
        else -> Unit
    }

    private fun scanDevices(enable: Boolean) =
        if (enable) viewModel.accept(Input.StartScanning)
        else viewModel.accept(Input.StopScanning)

    companion object {
        fun newInstance(isTopLevel: Boolean): NsdScanFragment = NsdScanFragment().apply { this.isTopLevel = isTopLevel }
    }
}

private var BindingViewHolder<ViewholderNsdListBinding>.item by viewHolderDelegate<NsdItem>()

fun BindingViewHolder<ViewholderNsdListBinding>.bind(item: NsdItem) {
    this.item = item

    val stringBuilder = StringBuilder()
    stringBuilder.append(item.info.serviceName).append("\n")
        .append(if (item.info.host != null) item.info.host.hostAddress else "")

    val color = itemView.context.themeColorAt(R.attr.prominent_text_color)

    binding.text.setTextColor(color)
    binding.text.text = stringBuilder.toString()
}