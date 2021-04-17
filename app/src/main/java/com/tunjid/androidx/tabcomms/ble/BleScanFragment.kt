package com.tunjid.androidx.tabcomms.ble

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
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.databinding.FragmentBleScanBinding
import com.tunjid.androidx.databinding.ViewholderScanBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.map
import com.tunjid.androidx.mapDistinct
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.activityNavigatorController
import com.tunjid.androidx.recyclerview.listAdapterOf
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderDelegate
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.setLoading
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.uidrivers.uiState

class BleScanFragment : Fragment(R.layout.fragment_ble_scan) {

    private var isTopLevel by fragmentArgs<Boolean>()
    private val viewModel by viewModels<BleViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isTopLevel) uiState = uiState.copy(
            toolbarTitle = this::class.java.routeName,
            toolbarMenuRefresher = ::updateToolbarMenu,
            toolbarMenuClickListener = ::onMenuItemSelected,
            toolbarMenuRes = R.menu.menu_ble_scan,
            toolbarShows = true,
            fabShows = false,
            showsBottomNav = false,
            insetFlags = InsetFlags.ALL,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        val placeHolder = PlaceHolder(view.findViewById(R.id.placeholder_container))
        placeHolder.bind(PlaceHolder.State(R.string.no_ble_devices, R.drawable.ic_bluetooth_24dp))

        FragmentBleScanBinding.bind(view).list.apply {
            val listAdapter = listAdapterOf(
                initialItems = viewModel.state.value?.items ?: listOf(),
                viewHolderCreator = { parent, _ ->
                    parent.viewHolderFrom(ViewholderScanBinding::inflate).apply {
                        itemView.setOnClickListener { onBluetoothDeviceClicked(result.device) }
                    }
                },
                viewHolderBinder = { viewHolder, scanResult, _ -> viewHolder.bind(scanResult) }
            )

            layoutManager = verticalLayoutManager()
            adapter = listAdapter

            addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))

            viewModel.state.apply {
                mapDistinct(BleState::items).observe(viewLifecycleOwner, listAdapter::submitList)
                mapDistinct(BleState::items).map(List<ScanResultCompat>::isNotEmpty).observe(viewLifecycleOwner, placeHolder::toggle)
                mapDistinct(BleState::turnOn).observe(viewLifecycleOwner) { turnOn ->
                    if (turnOn) startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
                }
            }
        }
    }

    private fun updateToolbarMenu(menu: Menu) {
        viewModel.state.mapDistinct(BleState::isScanning).observe(viewLifecycleOwner) { isScanning ->
            menu.findItem(R.id.menu_stop)?.isVisible = isScanning
            menu.findItem(R.id.menu_scan)?.isVisible = !isScanning

            val refresh = menu.findItem(R.id.menu_refresh)

            refresh?.isVisible = isScanning
            if (isScanning) refresh?.setLoading(requireContext().themeColorAt(R.attr.prominent_text_color))
        }
    }

    override fun onResume() {
        super.onResume()
        val noPermit = SDK_INT >= M && ActivityCompat.checkSelfPermission(requireActivity(),
            ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED

        if (noPermit) requestPermissions(arrayOf(ACCESS_COARSE_LOCATION), REQUEST_ENABLE_BT)
        else {
            viewModel.accept(BleInput.Permission(hasPermission = true))
            viewModel.accept(BleInput.StartScanning)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) = when (requestCode) {
        REQUEST_ENABLE_BT -> {
            // If request is cancelled, the result arrays are empty.
            val canScan = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            viewModel.accept(BleInput.Permission(hasPermission = canScan))
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

    private fun onMenuItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_scan -> viewModel.accept(BleInput.StartScanning)
        R.id.menu_stop -> viewModel.accept(BleInput.StopScanning)
        else -> super.onOptionsItemSelected(item).let { }
    }

    private fun onBluetoothDeviceClicked(bluetoothDevice: BluetoothDevice) {
        uiState = uiState.copy(snackbarText = bluetoothDevice.address)
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1

        fun newInstance(isTopLevel: Boolean): BleScanFragment = BleScanFragment().apply { this.isTopLevel = isTopLevel }
    }

}

private var BindingViewHolder<ViewholderScanBinding>.result by viewHolderDelegate<ScanResultCompat>()

fun BindingViewHolder<ViewholderScanBinding>.bind(result: ScanResultCompat) {
    this.result = result
    if (result.scanRecord != null) binding.apply {
        deviceName.text = result.scanRecord!!.deviceName
        deviceAddress.text = result.device.address
    }
}
