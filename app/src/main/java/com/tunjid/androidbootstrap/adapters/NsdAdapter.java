package com.tunjid.androidbootstrap.adapters;

import android.net.nsd.NsdServiceInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Adapter for showing open NSD services
 * <p>
 * Created by tj.dahunsi on 2/4/17.
 */

public class NsdAdapter extends InteractiveAdapter<NsdAdapter.NSDViewHolder,
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

    static class NSDViewHolder extends InteractiveViewHolder<ServiceClickedListener>
            implements View.OnClickListener {

        NsdServiceInfo serviceInfo;
        TextView textView;

        NSDViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
            itemView.setOnClickListener(this);
        }

        void bind(NsdServiceInfo info, ServiceClickedListener listener) {
            serviceInfo = info;
            adapterListener = listener;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(info.getServiceName()).append("\n")
                    .append(info.getHost() != null ? info.getHost().getHostAddress() : "");

            boolean isSelf = adapterListener.isSelf(info);

            if (isSelf) stringBuilder.append(" (SELF)");

            int color = ContextCompat.getColor(itemView.getContext(), isSelf
                    ? R.color.dark_grey
                    : R.color.colorPrimary);

            textView.setTextColor(color);
            textView.setText(stringBuilder.toString());
        }

        @Override
        public void onClick(View v) {
            adapterListener.onServiceClicked(serviceInfo);
        }
    }
}
