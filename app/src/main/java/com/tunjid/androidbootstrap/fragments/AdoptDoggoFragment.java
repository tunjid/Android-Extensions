package com.tunjid.androidbootstrap.fragments;

import android.os.Bundle;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tunjid.androidbootstrap.PlaceHolder;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.DoggoAdapter.ImageListAdapterListener;
import com.tunjid.androidbootstrap.adapters.InputAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator;
import com.tunjid.androidbootstrap.model.Doggo;
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder;
import com.tunjid.androidbootstrap.view.util.InsetFlags;
import com.tunjid.androidbootstrap.viewholders.DoggoViewHolder;
import com.tunjid.androidbootstrap.viewholders.InputViewHolder;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class AdoptDoggoFragment extends AppBaseFragment
        implements ImageListAdapterListener {

    static final String ARG_DOGGO = "doggo";

    private Doggo doggo;

    public static AdoptDoggoFragment newInstance(Doggo doggo) {
        AdoptDoggoFragment fragment = new AdoptDoggoFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_DOGGO, doggo);
        fragment.setArguments(args);
        fragment.prepareSharedElementTransition();

        return fragment;
    }

    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return super.getStableTag() + "-" + getArguments().getParcelable(ARG_DOGGO).hashCode();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doggo = getArguments().getParcelable(ARG_DOGGO);
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_adopt_doggo, container, false);

        new ListManagerBuilder<InputViewHolder, PlaceHolder.State>()
                .withRecyclerView(root.findViewById(R.id.model_list))
                .withLinearLayoutManager()
                .withAdapter(new InputAdapter(Arrays.asList(getResources().getStringArray(R.array.adoption_items))))
                .build();

        DoggoViewHolder viewHolder = new DoggoViewHolder(root, this);
        viewHolder.bind(doggo);

        tintView(R.color.black_50, viewHolder.thumbnail, this::setColorFilter);
        tintView(R.color.black_50, viewHolder.fullSize, this::setColorFilter);

        return root;
    }

    @Override
    public boolean showsToolBar() { return false; }

    @Override
    public boolean showsFab() { return true; }

    @Override
    public InsetFlags insetFlags() { return InsetFlags.NO_TOP; }

    @Override
    protected FabExtensionAnimator.GlyphState getFabState() {
        return FabExtensionAnimator.newState(getString(R.string.adopt), ContextCompat.getDrawable(requireContext(), R.drawable.ic_hug_24dp));
    }

    @Override
    protected View.OnClickListener getFabClickListener() {
        return view -> showSnackbar(snackBar -> snackBar.setText(getString(R.string.adopted_doggo, doggo.getName())));
    }

    private void setColorFilter(int color, ImageView imageView) { imageView.setColorFilter(color); }

    private void prepareSharedElementTransition() {
        Transition baseTransition = baseTransition();
        Transition baseSharedTransition = baseSharedTransition();

        setEnterTransition(baseTransition);
        setExitTransition(baseTransition);
        setSharedElementEnterTransition(baseSharedTransition);
        setSharedElementReturnTransition(baseSharedTransition);
    }
}
