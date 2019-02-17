package com.tunjid.androidbootstrap.fragments;


import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.PlaceHolder;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.NsdAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.recyclerview.ScrollManager;
import com.tunjid.androidbootstrap.recyclerview.ScrollManagerBuilder;
import com.tunjid.androidbootstrap.viewholders.NSDViewHolder;
import com.tunjid.androidbootstrap.viewmodels.NsdViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;

/**
 * A {@link Fragment} listing supported NSD servers
 */
public class NsdScanFragment extends AppBaseFragment
        implements
        NsdAdapter.ServiceClickedListener {

    private boolean isScanning;

    private ScrollManager<NSDViewHolder, PlaceHolder.State> scrollManager;
    private NsdViewModel viewModel;

    public NsdScanFragment() {
        // Required empty public constructor
    }

    public static NsdScanFragment newInstance() {
        NsdScanFragment fragment = new NsdScanFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(this).get(NsdViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_nsd_scan, container, false);
        PlaceHolder placeHolder = new PlaceHolder(root.findViewById(R.id.placeholder_container));
        placeHolder.bind(new PlaceHolder.State(R.string.no_nsd_devices, R.drawable.ic_signal_wifi__24dp));

        scrollManager = new ScrollManagerBuilder<NSDViewHolder, PlaceHolder.State>()
                .withRecyclerView(root.findViewById(R.id.list))
                .addDecoration(new DividerItemDecoration(requireActivity(), VERTICAL))
                .withAdapter(new NsdAdapter(this, viewModel.getServices()))
                .withPlaceholder(placeHolder)
                .withLinearLayoutManager()
                .build();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        scanDevices(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        scrollManager = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_nsd_scan, menu);

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
                return true;
            case R.id.menu_stop:
                scanDevices(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceClicked(NsdServiceInfo serviceInfo) {
    }

    @Override
    public boolean isSelf(NsdServiceInfo serviceInfo) {
        return false;
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
