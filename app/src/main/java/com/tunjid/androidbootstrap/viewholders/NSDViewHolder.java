package com.tunjid.androidbootstrap.viewholders;

import android.net.nsd.NsdServiceInfo;
import android.view.View;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.adapters.NsdAdapter;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import androidx.core.content.ContextCompat;

public class NSDViewHolder extends InteractiveViewHolder<NsdAdapter.ServiceClickedListener>
        implements View.OnClickListener {

    private NsdServiceInfo serviceInfo;
    private TextView textView;

    public NSDViewHolder(View itemView) {
        super(itemView);
        textView = (TextView) itemView;
        itemView.setOnClickListener(this);
    }

    public void bind(NsdServiceInfo info, NsdAdapter.ServiceClickedListener listener) {
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
