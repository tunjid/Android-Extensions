package com.tunjid.androidbootstrap.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.ImageListAdapter;
import com.tunjid.androidbootstrap.adapters.ImageListAdapter.ImageListAdapterListener;
import com.tunjid.androidbootstrap.adapters.ImageListAdapter.ImageViewHolder;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.model.Doggo;
import com.tunjid.androidbootstrap.view.animator.FabExtensionAnimator;
import com.tunjid.androidbootstrap.view.util.ViewUtil;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.SharedElementCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;

public class DoggoListFragment extends AppBaseFragment
        implements ImageListAdapterListener {

    private RecyclerView recyclerView;

    public static DoggoListFragment newInstance() {
        DoggoListFragment fragment = new DoggoListFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route, container, false);
        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new ImageListAdapter(Doggo.doggos, this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (Math.abs(dy) > 4) setFabExtended(dy > 0);
            }
        });
        postponeEnterTransition();
        return rootView;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scrollToPosition();
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.recyclerView = null;
    }

    @Override
    public boolean showsFab() { return true; }

    @Override
    protected FabExtensionAnimator.GlyphState getFabState() {
        return FabExtensionAnimator.newState(getText(R.string.collapse_prompt), ContextCompat.getDrawable(requireContext(), R.drawable.ic_paw_24dp));
    }

    @Override
    protected View.OnClickListener getFabClickListener() {
        return view -> setFabExtended(!isFabExtended());
    }

    public void onDoggoClicked(Doggo doggo) {
        Doggo.setTransitionDoggo(doggo);
        showFragment(DoggoPagerFragment.newInstance());
    }

    private void scrollToPosition() {
        recyclerView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                recyclerView.removeOnLayoutChangeListener(this);
                Doggo last = Doggo.getTransitionDoggo();
                if (last == null) return;

                int index = Doggo.doggos.indexOf(last);
                if (index < 0) return;

                LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                View viewAtPosition = layoutManager.findViewByPosition(index);
                boolean shouldScroll = viewAtPosition == null || layoutManager.isViewPartiallyVisible(viewAtPosition, false, true);

                if (shouldScroll) layoutManager.scrollToPosition(index);
            }
        });
    }

    @Nullable
    @SuppressLint({"CommitTransaction"})
    public FragmentTransaction provideFragmentTransaction(BaseFragment to) {
        if (!to.getStableTag().contains(DoggoPagerFragment.class.getSimpleName())) return null;

        Doggo doggo = Doggo.getTransitionDoggo();
        ImageView imageView = getTransitionImage();
        if (doggo == null || imageView == null) return null;

        setExitTransition(new TransitionSet()
                .setDuration(375)
                .setStartDelay(25)
                .setInterpolator(new FastOutSlowInInterpolator())
                .addTransition(new Fade().addTarget(R.id.doggo_image)));

        setExitSharedElementCallback(new SharedElementCallback() {
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                ImageView deferred = getTransitionImage();
                if (deferred != null) sharedElements.put(names.get(0), deferred);
            }
        });

        return requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .addSharedElement(imageView, ViewUtil.transitionName(doggo, imageView));
    }

    @Nullable
    private ImageView getTransitionImage() {
        Doggo doggo = Doggo.getTransitionDoggo();
        if (doggo == null) return null;

        ImageViewHolder holder = (ImageViewHolder) this.recyclerView.findViewHolderForItemId(doggo.hashCode());
        if (holder == null) return null;

        return holder.thumbnail;
    }
}
