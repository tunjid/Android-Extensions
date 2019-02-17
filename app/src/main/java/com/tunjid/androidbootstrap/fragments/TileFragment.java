package com.tunjid.androidbootstrap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.PlaceHolder;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.TileAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator;
import com.tunjid.androidbootstrap.recyclerview.ScrollManager;
import com.tunjid.androidbootstrap.recyclerview.ScrollManagerBuilder;
import com.tunjid.androidbootstrap.viewholders.TileViewHolder;
import com.tunjid.androidbootstrap.viewmodels.TileViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import static androidx.core.content.ContextCompat.getDrawable;

public class TileFragment extends AppBaseFragment {

    private TileViewModel viewModel;
    private ScrollManager<TileViewHolder, PlaceHolder.State> scrollManager;

    public static TileFragment newInstance() {
        TileFragment fragment = new TileFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(this).get(TileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_route, container, false);
        scrollManager = new ScrollManagerBuilder<TileViewHolder, PlaceHolder.State>()
                .withRecyclerView(root.findViewById(R.id.recycler_view))
                .withGridLayoutManager(4)
                .withAdapter(new TileAdapter(viewModel.getTiles(), tile -> {}))
                .build();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        disposables.add(viewModel.watchTiles().subscribe(scrollManager::onDiff, Throwable::printStackTrace));
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
