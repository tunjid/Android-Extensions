package com.tunjid.androidbootstrap.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tunjid.androidbootstrap.PlaceHolder;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.ScanAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.recyclerview.ScrollManager;
import com.tunjid.androidbootstrap.viewholders.ScanViewHolder;
import com.tunjid.androidbootstrap.viewmodels.BleViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

public class BleScanFragment extends AppBaseFragment
        implements
        ScanAdapter.ScanAdapterListener {

    private static final int REQUEST_ENABLE_BT = 1;

    private boolean isScanning;

    private ScrollManager<PlaceHolder.State, ScanViewHolder> scrollManager;
    private BleViewModel viewModel;

    public static BleScanFragment newInstance() {
        BleScanFragment fragment = new BleScanFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(this).get(BleViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ble_scan, container, false);
        PlaceHolder placeHolder = new PlaceHolder(root.findViewById(R.id.placeholder_container));
        placeHolder.bind(new PlaceHolder.State(R.string.no_ble_devices, R.drawable.ic_bluetooth_24dp));

        scrollManager = ScrollManager.<PlaceHolder.State, ScanViewHolder>
                withRecyclerView(root.findViewById(R.id.list))
                .addDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
                .withAdapter(new ScanAdapter(this, viewModel.getScanResults()))
                .withPlaceholder(placeHolder)
                .withLinearLayoutManager()
                .build();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (viewModel.hasBle()) return;

        Activity activity = requireActivity();
        Toast.makeText(activity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        activity.onBackPressed();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_ble_scan, menu);

        menu.findItem(R.id.menu_stop).setVisible(isScanning);
        menu.findItem(R.id.menu_scan).setVisible(!isScanning);

        MenuItem refresh = menu.findItem(R.id.menu_refresh);

        refresh.setVisible(isScanning);
        if (isScanning) refresh.setActionView(R.layout.actionbar_indeterminate_progress);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                scanDevices(true);
                break;
            case R.id.menu_stop:
                scanDevices(false);
                break;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.stopScanning();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Ensures BT is enabled on the device.  If BT is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!viewModel.isBleOn()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        boolean noPermit = SDK_INT >= M && ActivityCompat.checkSelfPermission(requireActivity(),
                ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;

        if (noPermit) requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, REQUEST_ENABLE_BT);
        else scanDevices(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT: {
                // If request is cancelled, the result arrays are empty.
                boolean canScan = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (canScan) scanDevices(true);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            requireActivity().onBackPressed();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        scrollManager = null;
    }

    @Override
    public void onBluetoothDeviceClicked(final BluetoothDevice bluetoothDevice) {
        showSnackbar(snackBar -> snackBar.setText(bluetoothDevice.getAddress()));
    }

    private void scanDevices(boolean enable) {
        isScanning = enable;

        if (isScanning) disposables.add(viewModel.findDevices()
                .doOnSubscribe(__ -> requireActivity().invalidateOptionsMenu())
                .doFinally(this::onScanningStopped)
                .subscribe(scrollManager::onDiff, Throwable::printStackTrace));
        else viewModel.stopScanning();
    }

    private void onScanningStopped() {
        isScanning = false;
        requireActivity().invalidateOptionsMenu();
    }
}