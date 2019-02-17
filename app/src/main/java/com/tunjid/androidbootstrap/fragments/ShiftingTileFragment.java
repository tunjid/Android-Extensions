package com.tunjid.androidbootstrap.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.PlaceHolder;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.TileAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator;
import com.tunjid.androidbootstrap.recyclerview.ListManager;
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder;
import com.tunjid.androidbootstrap.viewholders.TileViewHolder;
import com.tunjid.androidbootstrap.viewmodels.ShiftingTileViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import static androidx.core.content.ContextCompat.getDrawable;

public class ShiftingTileFragment extends AppBaseFragment {

    private ShiftingTileViewModel viewModel;
    private ListManager<TileViewHolder, PlaceHolder.State> listManager;

    public static ShiftingTileFragment newInstance() {
        ShiftingTileFragment fragment = new ShiftingTileFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(this).get(ShiftingTileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_route, container, false);
        listManager = new ListManagerBuilder<TileViewHolder, PlaceHolder.State>()
                .withRecyclerView(root.findViewById(R.id.recycler_view))
                .withGridLayoutManager(4)
                .withAdapter(new TileAdapter(viewModel.getTiles(), tile -> {}))
                .build();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        disposables.add(viewModel.watchTiles().subscribe(listManager::onDiff, Throwable::printStackTrace));
    }

    @Override
    public boolean showsFab() {
        return true;
    }

    @Override
    protected FabExtensionAnimator.GlyphState getFabState() {
        return viewModel.changes()
                ? FabExtensionAnimator.newState(getText(R.string.static_tiles), getDrawable(requireContext(), R.drawable.ic_grid_24dp))
                : FabExtensionAnimator.newState(getText(R.string.dynamic_tiles), getDrawable(requireContext(), R.drawable.ic_blur_24dp));
    }

    @Override
    protected View.OnClickListener getFabClickListener() {
        return view -> {
            viewModel.toggleChanges();
            togglePersistentUi();
        };
    }
}
