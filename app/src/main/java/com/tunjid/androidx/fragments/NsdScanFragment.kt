package com.tunjid.androidx.fragments


import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.PlaceHolder
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.ViewholderNsdListBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.recyclerview.acceptDiff
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.setLoading
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.viewmodels.NsdViewModel
import com.tunjid.androidx.viewmodels.routeName

/**
 * A [Fragment] listing supported NSD servers
 */
class NsdScanFragment : Fragment(R.layout.fragment_nsd_scan) {

    private val viewModel by viewModels<NsdViewModel>()

    private var recyclerView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = UiState(
                toolbarTitle = this::class.java.routeName,
                toolbarMenuRefresher = ::updateToolbarMenu,
                toolbarMenuClickListener = ::onMenuItemSelected,
                toolbarMenuRes = R.menu.menu_nsd_scan,
                toolbarShows = true,
                fabShows = false,
                showsBottomNav = true,
                insetFlags = InsetFlags.ALL,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        val placeHolder = PlaceHolder(view.findViewById(R.id.placeholder_container))
        placeHolder.bind(PlaceHolder.State(R.string.no_nsd_devices, R.drawable.ic_signal_wifi__24dp))

        recyclerView = view.findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = verticalLayoutManager()
            adapter = adapterOf(
                    itemsSource = viewModel::services,
                    viewHolderCreator = { parent, _ -> parent.viewHolderFrom(ViewholderNsdListBinding::inflate) },
                    viewHolderBinder = { viewHolder, service, _ -> viewHolder.bind(service) }
            )

            addItemDecoration(DividerItemDecoration(requireActivity(), VERTICAL))

            viewModel.scanChanges.observe(viewLifecycleOwner) {
                placeHolder.toggle(viewModel.services.isEmpty())
                acceptDiff(it)
            }
            viewModel.isScanning.observe(viewLifecycleOwner) { uiState = uiState.copy(toolbarInvalidated = true) }
        }
    }

    override fun onResume() {
        super.onResume()
        scanDevices(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
    }

    private fun updateToolbarMenu(menu: Menu) {
        val currentlyScanning = viewModel.isScanning.value ?: false

        menu.findItem(R.id.menu_stop)?.isVisible = currentlyScanning
        menu.findItem(R.id.menu_scan)?.isVisible = !currentlyScanning

        val refresh = menu.findItem(R.id.menu_refresh)

        refresh?.isVisible = currentlyScanning
        if (currentlyScanning) refresh?.setLoading(requireContext().themeColorAt(R.attr.prominent_text_color))
    }

    private fun onMenuItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_scan -> scanDevices(true)
        R.id.menu_stop -> scanDevices(false)
        else -> super.onOptionsItemSelected(item).let { }
    }

    private fun scanDevices(enable: Boolean) =
            if (enable) viewModel.findDevices()
            else viewModel.stopScanning()

    companion object {
        fun newInstance(): NsdScanFragment = NsdScanFragment().apply { arguments = Bundle() }
    }
}

private var BindingViewHolder<ViewholderNsdListBinding>.serviceInfo by BindingViewHolder.Prop<NsdServiceInfo>()

fun BindingViewHolder<ViewholderNsdListBinding>.bind(info: NsdServiceInfo) {
    serviceInfo = info

    val stringBuilder = StringBuilder()
    stringBuilder.append(info.serviceName).append("\n")
            .append(if (info.host != null) info.host.hostAddress else "")

    val color = itemView.context.themeColorAt(R.attr.prominent_text_color)

    binding.text.setTextColor(color)
    binding.text.text = stringBuilder.toString()
}