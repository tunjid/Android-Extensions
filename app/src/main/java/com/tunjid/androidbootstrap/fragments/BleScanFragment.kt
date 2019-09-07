package com.tunjid.androidbootstrap.fragments

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import com.tunjid.androidbootstrap.GlobalUiController
import com.tunjid.androidbootstrap.PlaceHolder
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.UiState
import com.tunjid.androidbootstrap.activityGlobalUiController
import com.tunjid.androidbootstrap.adapters.ScanAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.recyclerview.ListManager
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.viewholders.ScanViewHolder
import com.tunjid.androidbootstrap.viewmodels.BleViewModel

class BleScanFragment : AppBaseFragment(), GlobalUiController, ScanAdapter.ScanAdapterListener {

    override var uiState: UiState by activityGlobalUiController()

    private var isScanning: Boolean = false

    private lateinit var listManager: ListManager<ScanViewHolder, PlaceHolder.State>
    private lateinit var viewModel: BleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this).get(BleViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        uiState = uiState.copy(
                toolbarTitle = this::class.java.simpleName,
                toolBarMenu = R.menu.menu_ble_scan,
                showsToolbar = false,
                showsFab = false,
                navBarColor = ContextCompat.getColor(requireContext(), R.color.white_75)
        )

        val root = inflater.inflate(R.layout.fragment_ble_scan, container, false)
        val placeHolder = PlaceHolder(root.findViewById(R.id.placeholder_container))
        placeHolder.bind(PlaceHolder.State(R.string.no_ble_devices, R.drawable.ic_bluetooth_24dp))

        listManager = ListManagerBuilder<ScanViewHolder, PlaceHolder.State>()
                .withRecyclerView(root.findViewById(R.id.list))
                .addDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
                .withAdapter(ScanAdapter(this, viewModel.scanResults))
                .withPlaceholder(placeHolder)
                .withLinearLayoutManager()
                .build()

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (viewModel.hasBle()) return

        val activity = requireActivity()
        Toast.makeText(activity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
        activity.onBackPressed()
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
        when (item.itemId) {
            R.id.menu_scan -> scanDevices(true)
            R.id.menu_stop -> scanDevices(false)
        }
        return true
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                // If request is cancelled, the result arrays are empty.
                val canScan = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (canScan) scanDevices(true)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listManager.clear()
    }

    override fun onBluetoothDeviceClicked(bluetoothDevice: BluetoothDevice) {
        showSnackbar { snackBar -> snackBar.setText(bluetoothDevice.address) }
    }

    private fun scanDevices(enable: Boolean) {
        isScanning = enable

        if (isScanning) disposables.add(viewModel.findDevices()
                .doOnSubscribe { requireActivity().invalidateOptionsMenu() }
                .doFinally { this.onScanningStopped() }
                .subscribe(listManager::onDiff, Throwable::printStackTrace))
        else viewModel.stopScanning()
    }

    private fun onScanningStopped() {
        isScanning = false
        requireActivity().invalidateOptionsMenu()
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1

        fun newInstance(): BleScanFragment = BleScanFragment().apply { arguments = Bundle() }
    }

}