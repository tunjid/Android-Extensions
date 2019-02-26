package com.tunjid.androidbootstrap.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.PlaceHolder;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.TileAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.recyclerview.ListManager;
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder;
import com.tunjid.androidbootstrap.viewholders.TileViewHolder;
import com.tunjid.androidbootstrap.viewmodels.EndlessTileViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import static com.tunjid.androidbootstrap.viewmodels.EndlessTileViewModel.NUM_TILES;

public class EndlessTileFragment extends AppBaseFragment {

    private EndlessTileViewModel viewModel;
    private ListManager<TileViewHolder, PlaceHolder.State> listManager;

    public static EndlessTileFragment newInstance() {
        EndlessTileFragment fragment = new EndlessTileFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(this).get(EndlessTileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_route, container, false);
        listManager = new ListManagerBuilder<TileViewHolder, PlaceHolder.State>()
                .withRecyclerView(root.findViewById(R.id.recycler_view))
                .withGridLayoutManager(3)
                .withAdapter(new TileAdapter(viewModel.getTiles(), tile -> showSnackbar(snackBar -> snackBar.setText(tile.toString()))))
                .withEndlessScrollCallback(NUM_TILES, __ -> disposables.add(viewModel.getMoreTiles()
                        .subscribe(listManager::onDiff, Throwable::printStackTrace)))
                .build();

        return root;
    }
}
