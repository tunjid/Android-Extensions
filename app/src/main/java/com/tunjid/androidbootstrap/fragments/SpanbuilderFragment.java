package com.tunjid.androidbootstrap.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment;
import com.tunjid.androidbootstrap.core.text.SpanBuilder;

/**
 * Fragment showing a static list of images
 * <p>
 * Created by tj.dahunsi on 5/6/17.
 */

public class SpanbuilderFragment extends AppBaseFragment {

    public static SpanbuilderFragment newInstance() {
        SpanbuilderFragment fragment = new SpanbuilderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spanbuilder, container, false);

        TextView textView = rootView.findViewById(R.id.text);
        Context context = textView.getContext();

        CharSequence text = new SpanBuilder(context, "This is a regular span")
                .prependSpace()
                .prependCharsequence(".")
                .prependNumber(1)
                .appendNewLine()
                .appendNumber(2)
                .appendCharsequence(".")
                .appendSpace()
                .appendCharsequence(new SpanBuilder(context, "This is a colored span")
                        .color(R.color.colorPrimaryDark)
                        .build())
                .appendNewLine()
                .appendNumber(3)
                .appendCharsequence(".")
                .appendSpace()
                .appendCharsequence(new SpanBuilder(context, "This is an italicized span")
                        .italic()
                        .build())
                .appendNewLine()
                .appendNumber(4)
                .appendCharsequence(".")
                .appendSpace()
                .appendCharsequence(new SpanBuilder(context, "This is an undelined span")
                        .underline()
                        .build())
                .appendNewLine()
                .appendNumber(5)
                .appendCharsequence(".")
                .appendSpace()
                .appendCharsequence(new SpanBuilder(context, "This is a bold span")
                        .bold()
                        .build())
                .appendNewLine()
                .appendNumber(6)
                .appendCharsequence(".")
                .appendSpace()
                .appendCharsequence(new SpanBuilder(context, "This is a resized span")
                        .resize(1.2F)
                        .build())
                .prependNewLine()
                .build();

        textView.setText(text);

        return rootView;
    }
}
