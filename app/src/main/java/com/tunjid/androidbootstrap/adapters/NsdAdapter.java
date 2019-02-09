package com.tunjid.androidbootstrap.adapters;

import android.net.nsd.NsdServiceInfo;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.viewholders.NSDViewHolder;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Adapter for showing open NSD services
 * <p>
 * Created by tj.dahunsi on 2/4/17.
 */

public class NsdAdapter extends InteractiveAdapter<NSDViewHolder,
        NsdAdapter.ServiceClickedListener> {

    private List<NsdServiceInfo> infoList;

    public NsdAdapter(NsdAdapter.ServiceClickedListener listener, List<NsdServiceInfo> list) {
        super(listener);
        this.infoList = list;
    }

    @NonNull
    @Override
    public NSDViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NSDViewHolder(getItemView(R.layout.viewholder_nsd_list, parent));
    }

    @Override
    public void onBindViewHolder(@NonNull NSDViewHolder holder, int position) {
        holder.bind(infoList.get(position), adapterListener);
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    public interface ServiceClickedListener extends InteractiveAdapter.AdapterListener {
        void onServiceClicked(NsdServiceInfo serviceInfo);

        boolean isSelf(NsdServiceInfo serviceInfo);
    }

}
