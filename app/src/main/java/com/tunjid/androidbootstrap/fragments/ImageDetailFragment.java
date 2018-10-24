package com.tunjid.androidbootstrap.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.ImageListAdapter;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.model.Doggo;

import java.util.Arrays;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.ColorFilterTransformation;

/**
 * Fragment showing a static list of images
 * <p>
 * Created by tj.dahunsi on 5/6/17.
 */

public class ImageDetailFragment extends AppBaseFragment {

    public static final String ARG_DOGGO = "doggo";

    public static ImageDetailFragment newInstance(Doggo doggo) {
        ImageDetailFragment fragment = new ImageDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_DOGGO, doggo);
        fragment.setArguments(args);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition baseTransition = new Fade();
            Transition baseSharedTransition = getTransition();

            baseTransition.excludeTarget(R.id.image, true);

            fragment.setEnterTransition(baseTransition);
            fragment.setExitTransition(baseTransition);
            fragment.setSharedElementEnterTransition(baseSharedTransition);
            fragment.setSharedElementReturnTransition(baseSharedTransition);
        }
        return fragment;
    }

    @Override
    public String getStableTag() {
        assert getArguments() != null;
        return super.getStableTag() + "-" + getArguments().getParcelable(ARG_DOGGO);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_detail, container, false);
        ImageView blurredBackground = rootView.findViewById(R.id.blurred_background);

        assert getArguments() != null;
        Doggo doggo = getArguments().getParcelable(ARG_DOGGO);

        if (doggo != null) {
            Picasso.with(inflater.getContext())
                    .load(doggo.getImageRes())
                    .fit()
                    .centerCrop()
                    .transform(Arrays.asList(new BlurTransformation(inflater.getContext(), 20),
                            new ColorFilterTransformation(Color.parseColor("#C8000000"))))
                    .into(blurredBackground);
        }

        ImageListAdapter.ImageViewHolder holder = new ImageListAdapter.ImageViewHolder(rootView, null);
        holder.textView.setVisibility(View.GONE);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) rootView);

        constraintSet.centerHorizontally(holder.itemView.getId(), blurredBackground.getId());
        constraintSet.centerVertically(holder.itemView.getId(), blurredBackground.getId());
        constraintSet.applyTo((ConstraintLayout) rootView);

        holder.bind(doggo);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toggleToolbar(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        toggleToolbar(true);
    }

    static Transition getTransition() {
        TransitionSet result = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            result = new TransitionSet();

            result.setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform());
        }
        return result;
    }
}
