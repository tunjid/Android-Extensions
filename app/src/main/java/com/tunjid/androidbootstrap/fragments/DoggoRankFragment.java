package com.tunjid.androidbootstrap.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.PlaceHolder;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.DoggoAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator;
import com.tunjid.androidbootstrap.recyclerview.ScrollManager;
import com.tunjid.androidbootstrap.recyclerview.ScrollManagerBuilder;
import com.tunjid.androidbootstrap.viewholders.DoggoRankViewHolder;
import com.tunjid.androidbootstrap.viewholders.DoggoViewHolder;
import com.tunjid.androidbootstrap.viewmodels.DoggoRankViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;

import static androidx.core.content.ContextCompat.getDrawable;
import static com.tunjid.androidbootstrap.recyclerview.ScrollManager.SWIPE_DRAG_ALL_DIRECTIONS;

public class DoggoRankFragment extends AppBaseFragment
        implements DoggoAdapter.ImageListAdapterListener {

    private DoggoRankViewModel viewModel;
    private ScrollManager<DoggoRankViewHolder, PlaceHolder.State> scrollManager;

    public static DoggoRankFragment newInstance() {
        DoggoRankFragment fragment = new DoggoRankFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = ViewModelProviders.of(this).get(DoggoRankViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_simple_list, container, false);
        PlaceHolder placeHolder = new PlaceHolder(root.findViewById(R.id.placeholder_container));

        scrollManager = new ScrollManagerBuilder<DoggoRankViewHolder, PlaceHolder.State>()
                .withRecyclerView(root.findViewById(R.id.recycler_view))
                .withAdapter(new DoggoAdapter<>(viewModel.getDoggos(), R.layout.viewholder_doggo_rank, DoggoRankViewHolder::new, this))
                .addScrollListener((dx, dy) -> { if (Math.abs(dy) > 4) setFabExtended(dy < 0); })
                .withPlaceholder(placeHolder)
                .withLinearLayoutManager()
                .withSwipeDragOptions(ScrollManager.<DoggoRankViewHolder>swipeDragOptionsBuilder()
                        .setMovementFlagsFunction(viewHolder -> SWIPE_DRAG_ALL_DIRECTIONS)
                        .setSwipeConsumer((holder, position) -> removeDoggo(holder))
                        .setDragHandleFunction(DoggoRankViewHolder::getDragView)
                        .setSwipeDragStartConsumer(this::onSwipeOrDragStarted)
                        .setSwipeDragEndConsumer(this::onSwipeOrDragEnded)
                        .setLongPressDragEnabledSupplier(() -> false)
                        .setItemViewSwipeSupplier(() -> true)
                        .setDragConsumer(this::moveDoggo)
                        .build())
                .build();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        disposables.add(viewModel.watchDoggos().subscribe(this::onDiff, Throwable::printStackTrace));
    }

    @Override
    public boolean showsFab() {
        return true;
    }

    @Override
    protected FabExtensionAnimator.GlyphState getFabState() {
        return FabExtensionAnimator.newState(getText(R.string.reset_doggos), getDrawable(requireContext(), R.drawable.ic_restore_24dp));
    }

    @Override
    protected View.OnClickListener getFabClickListener() {
        return view -> viewModel.resetList();
    }

    private void onDiff(DiffUtil.DiffResult diffResult) {
        scrollManager.onDiff(diffResult);
        togglePersistentUi();
    }

    private void moveDoggo(DoggoViewHolder start, DoggoViewHolder end) {
        int from = start.getAdapterPosition();
        int to = end.getAdapterPosition();

        viewModel.swap(from, to);
        scrollManager.notifyItemMoved(from, to);
        scrollManager.notifyItemChanged(from);
        scrollManager.notifyItemChanged(to);
    }

    private void removeDoggo(DoggoViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        Pair<Integer, Integer> minMax = viewModel.remove(position);

        scrollManager.notifyItemRemoved(position);
        // Only necessary to rebind views lower so they have the right position
        scrollManager.notifyItemRangeChanged(minMax.first, minMax.second);
    }

    private void onSwipeOrDragStarted(DoggoRankViewHolder holder, int actionState) {
        viewModel.onActionStarted(new Pair<>(holder.getItemId(), actionState));
    }

    private void onSwipeOrDragEnded(DoggoViewHolder viewHolder, int actionState) {
        String message = viewModel.onActionEnded(new Pair<>(viewHolder.getItemId(), actionState));
        if (!TextUtils.isEmpty(message)) showSnackbar(snackBar -> snackBar.setText(message));
    }
}
