package com.tunjid.androidx.fragments

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.PlaceHolder
import com.tunjid.androidx.R
import com.tunjid.androidx.communications.bluetooth.ScanResultCompat
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.FragmentBleScanBinding
import com.tunjid.androidx.databinding.ViewholderScanBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.activityNavigatorController
import com.tunjid.androidx.recyclerview.acceptDiff
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.setLoading
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.viewmodels.BleViewModel
import com.tunjid.androidx.viewmodels.routeName

class BleScanFragment : Fragment(R.layout.fragment_ble_scan) {

    private val viewModel by viewModels<BleViewModel>()
    private val navigator by activityNavigatorController<MultiStackNavigator>()

    private var recyclerView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarMenuRefresher = ::updateToolbarMenu,
                toolbarMenuClickListener = ::onMenuItemSelected,
                toolBarMenu = R.menu.menu_ble_scan,
                toolbarShows = true,
                fabShows = false,
                showsBottomNav = false,
                insetFlags = InsetFlags.ALL,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        val placeHolder = PlaceHolder(view.findViewById(R.id.placeholder_container))
        placeHolder.bind(PlaceHolder.State(R.string.no_ble_devices, R.drawable.ic_bluetooth_24dp))

        recyclerView = FragmentBleScanBinding.bind(view).list.apply {
            layoutManager = verticalLayoutManager()
            adapter = adapterOf(
                    itemsSource = viewModel::scanResults,
                    viewHolderCreator = { parent, _ ->
                        parent.viewHolderFrom(ViewholderScanBinding::inflate).apply {
                            itemView.setOnClickListener { onBluetoothDeviceClicked(result.device) }
                        }
                    },
                    viewHolderBinder = { viewHolder, scanResult, _ -> viewHolder.bind(scanResult) }
            )

            addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))

            viewModel.devices.observe(viewLifecycleOwner) {
                placeHolder.toggle(viewModel.scanResults.isEmpty())
                acceptDiff(it)
            }
            viewModel.isScanning.observe(viewLifecycleOwner) { uiState = uiState.copy(toolbarInvalidated = true) }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (viewModel.hasBle()) return

        uiState = uiState.copy(snackbarText = getString(R.string.ble_not_supported))
        navigator.pop()
    }

    private fun updateToolbarMenu(menu: Menu) {
        val currentlyScanning = viewModel.isScanning.value ?: false

        menu.findItem(R.id.menu_stop)?.isVisible = currentlyScanning
        menu.findItem(R.id.menu_scan)?.isVisible = !currentlyScanning

        val refresh = menu.findItem(R.id.menu_refresh)

        refresh?.isVisible = currentlyScanning
        if (currentlyScanning) refresh?.setLoading(requireContext().themeColorAt(R.attr.prominent_text_color))
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopScanning()
    }

    override fun onResume() {
        super.onResume()

        // Ensures BT is enabled on the device.  If BT is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!viewModel.isBleOn) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        val noPermit = SDK_INT >= M && ActivityCompat.checkSelfPermission(requireActivity(),
                ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED

        if (noPermit) requestPermissions(arrayOf(ACCESS_COARSE_LOCATION), REQUEST_ENABLE_BT)
        else scanDevices(true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) = when (requestCode) {
        REQUEST_ENABLE_BT -> {
            // If request is cancelled, the result arrays are empty.
            val canScan = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (canScan) scanDevices(true)
            Unit
        }
        else -> Unit
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            activity?.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
    }

    private fun onMenuItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_scan -> scanDevices(true)
        R.id.menu_stop -> scanDevices(false)
        else -> super.onOptionsItemSelected(item).let { }
    }

    private fun onBluetoothDeviceClicked(bluetoothDevice: BluetoothDevice) {
        uiState = uiState.copy(snackbarText = bluetoothDevice.address)
    }

    private fun scanDevices(enable: Boolean) =
            if (enable) viewModel.findDevices()
            else viewModel.stopScanning()


    companion object {
        private const val REQUEST_ENABLE_BT = 1

        fun newInstance(): BleScanFragment = BleScanFragment().apply { arguments = Bundle() }
    }

}

private var BindingViewHolder<ViewholderScanBinding>.result by BindingViewHolder.Prop<ScanResultCompat>()

fun BindingViewHolder<ViewholderScanBinding>.bind(result: ScanResultCompat) {
    this.result = result
    if (result.scanRecord != null) binding.apply {
        deviceName.text = result.scanRecord!!.deviceName
        deviceAddress.text = result.device.address
    }
}
