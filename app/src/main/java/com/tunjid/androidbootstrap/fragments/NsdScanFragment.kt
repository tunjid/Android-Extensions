package com.tunjid.androidbootstrap.fragments


import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import com.tunjid.androidbootstrap.uidrivers.GlobalUiController
import com.tunjid.androidbootstrap.PlaceHolder
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.uidrivers.UiState
import com.tunjid.androidbootstrap.uidrivers.activityGlobalUiController
import com.tunjid.androidbootstrap.adapters.NsdAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.recyclerview.ListManager
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.viewholders.NSDViewHolder
import com.tunjid.androidbootstrap.viewmodels.NsdViewModel

/**
 * A [Fragment] listing supported NSD servers
 */
class NsdScanFragment : AppBaseFragment(R.layout.fragment_nsd_scan), GlobalUiController, NsdAdapter.ServiceClickedListener {

    override var uiState: UiState by activityGlobalUiController()

    private val viewModel by viewModels<NsdViewModel>()

    private lateinit var listManager: ListManager<NSDViewHolder, PlaceHolder.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel.scanChanges.observe(this) { listManager.onDiff(it) }
        viewModel.isScanning.observe(this) { uiState = uiState.copy(toolbarInvalidated = true) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.simpleName,
                toolbarShows = true,
                toolBarMenu = R.menu.menu_nsd_scan,
                fabShows = false,
                showsBottomNav = true,
                navBarColor = ContextCompat.getColor(requireContext(), R.color.white_75)
        )

        val placeHolder = PlaceHolder(view.findViewById(R.id.placeholder_container))
        placeHolder.bind(PlaceHolder.State(R.string.no_nsd_devices, R.drawable.ic_signal_wifi__24dp))

        listManager = ListManagerBuilder<NSDViewHolder, PlaceHolder.State>()
                .withRecyclerView(view.findViewById(R.id.list))
                .addDecoration(DividerItemDecoration(requireActivity(), VERTICAL))
                .withAdapter(NsdAdapter(this, viewModel.services))
                .withPlaceholder(placeHolder)
                .withLinearLayoutManager()
                .build()
    }

    override fun onResume() {
        super.onResume()
        scanDevices(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listManager.clear()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val currentlyScanning = viewModel.isScanning.value ?: false

        menu.findItem(R.id.menu_stop)?.isVisible = currentlyScanning
        menu.findItem(R.id.menu_scan)?.isVisible = !currentlyScanning

        val refresh = menu.findItem(R.id.menu_refresh)

        refresh?.isVisible = currentlyScanning
        if (currentlyScanning) refresh?.setActionView(R.layout.actionbar_indeterminate_progress)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_scan -> scanDevices(true).let { true }
        R.id.menu_stop -> scanDevices(false).let { true }
        else -> true
    }

    override fun onServiceClicked(serviceInfo: NsdServiceInfo) = Unit

    override fun isSelf(serviceInfo: NsdServiceInfo): Boolean = false

    private fun scanDevices(enable: Boolean) =
            if (enable) viewModel.findDevices()
            else viewModel.stopScanning()

    companion object {
        fun newInstance(): NsdScanFragment = NsdScanFragment().apply { arguments = Bundle() }
    }
}
