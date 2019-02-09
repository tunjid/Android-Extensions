package com.tunjid.androidbootstrap.adapters;

import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.functions.BiFunction;
import com.tunjid.androidbootstrap.model.Doggo;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.viewholders.DoggoViewHolder;

import java.util.List;

import androidx.annotation.NonNull;

public class DoggoAdapter<T extends DoggoViewHolder> extends InteractiveAdapter<T, DoggoAdapter.ImageListAdapterListener> {

    private final List<Doggo> doggos;
    private final int layoutRes;
    private final BiFunction<View, DoggoAdapter.ImageListAdapterListener, T> viewHolderFactory;

    public DoggoAdapter(List<Doggo> doggos,
                        int layoutRes,
                        BiFunction<View, DoggoAdapter.ImageListAdapterListener, T> viewHolderFactory,
                        ImageListAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.doggos = doggos;
        this.layoutRes = layoutRes;
        this.viewHolderFactory = viewHolderFactory;
    }

    @NonNull
    @Override
    public T onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = getItemView(layoutRes, parent);
        return viewHolderFactory.apply(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull T holder, int recyclerViewPosition) {
        final Doggo item = doggos.get(recyclerViewPosition);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return doggos.size();
    }

    @Override
    public long getItemId(int position) {
        return doggos.get(position).hashCode();
    }


    public interface ImageListAdapterListener extends InteractiveAdapter.AdapterListener {
        default void onDoggoClicked(Doggo doggo) { }

        default void onDoggoImageLoaded(Doggo doggo) { }
    }

}
