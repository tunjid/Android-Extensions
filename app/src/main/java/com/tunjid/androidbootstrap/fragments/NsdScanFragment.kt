package com.tunjid.androidbootstrap.fragments


import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import com.tunjid.androidbootstrap.PlaceHolder
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.NsdAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.recyclerview.ListManager
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.viewholders.NSDViewHolder
import com.tunjid.androidbootstrap.viewmodels.NsdViewModel

/**
 * A [Fragment] listing supported NSD servers
 */
class NsdScanFragment : AppBaseFragment(), NsdAdapter.ServiceClickedListener {

    private var isScanning: Boolean = false
    private lateinit var listManager: ListManager<NSDViewHolder, PlaceHolder.State>
    private lateinit var viewModel: NsdViewModel

    override val toolBarMenuRes: Int = R.menu.menu_nsd_scan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this).get(NsdViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_nsd_scan, container, false)
        val placeHolder = PlaceHolder(root.findViewById(R.id.placeholder_container))
        placeHolder.bind(PlaceHolder.State(R.string.no_nsd_devices, R.drawable.ic_signal_wifi__24dp))

        listManager = ListManagerBuilder<NSDViewHolder, PlaceHolder.State>()
                .withRecyclerView(root.findViewById(R.id.list))
                .addDecoration(DividerItemDecoration(requireActivity(), VERTICAL))
                .withAdapter(NsdAdapter(this, viewModel.services))
                .withPlaceholder(placeHolder)
                .withLinearLayoutManager()
                .build()

        return root
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

        menu.findItem(R.id.menu_stop)?.isVisible = isScanning
        menu.findItem(R.id.menu_scan)?.isVisible = !isScanning

        val refresh = menu.findItem(R.id.menu_refresh)

        refresh?.isVisible = isScanning
        if (isScanning) refresh?.setActionView(R.layout.actionbar_indeterminate_progress)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_scan -> {
                scanDevices(true)
                true
            }
            R.id.menu_stop -> {
                scanDevices(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onServiceClicked(serviceInfo: NsdServiceInfo) {}

    override fun isSelf(serviceInfo: NsdServiceInfo): Boolean {
        return false
    }

    private fun scanDevices(enable: Boolean) {
        isScanning = enable

        if (isScanning) disposables.add(viewModel.findDevices()
                .doOnSubscribe { requireActivity().invalidateOptionsMenu() }
                .doFinally(this::onScanningStopped)
                .subscribe(listManager::onDiff, Throwable::printStackTrace))
        else viewModel.stopScanning()
    }

    private fun onScanningStopped() {
        isScanning = false
        requireActivity().invalidateOptionsMenu()
    }

    companion object {
        fun newInstance(): NsdScanFragment = NsdScanFragment().apply { arguments = Bundle() }
    }
}
