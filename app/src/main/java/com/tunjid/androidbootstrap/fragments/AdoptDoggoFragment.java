package com.tunjid.androidbootstrap.fragments;

import android.os.Bundle;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.ImageListAdapter.ImageListAdapterListener;
import com.tunjid.androidbootstrap.adapters.ImageListAdapter.ImageViewHolder;
import com.tunjid.androidbootstrap.adapters.InputAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.model.Doggo;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdoptDoggoFragment extends AppBaseFragment
        implements ImageListAdapterListener {

    private static final String ARG_DOGGO = "doggo";
    private static final InsetFlags NO_TOP = InsetFlags.create(true, false, true, true);

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

    @Nullable
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_adopt_doggo, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.model_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new InputAdapter(Arrays.asList(getResources().getStringArray(R.array.adoption_items))));

        new ImageViewHolder(root, this).bind(getArguments().getParcelable(ARG_DOGGO));

        return root;
    }

    @Override public boolean showsToolBar() {
        return false;
    }

    @Override public InsetFlags insetFlags() {
        return NO_TOP;
    }

    private void prepareSharedElementTransition() {
        Transition baseTransition = baseTransition();
        Transition baseSharedTransition = baseSharedTransition();

        setEnterTransition(baseTransition);
        setExitTransition(baseTransition);
        setSharedElementEnterTransition(baseSharedTransition);
        setSharedElementReturnTransition(baseSharedTransition);
    }
}
