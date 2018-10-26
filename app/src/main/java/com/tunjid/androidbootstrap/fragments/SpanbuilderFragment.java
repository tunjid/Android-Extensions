package com.tunjid.androidbootstrap.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spanbuilder, container, false);

        TextView textView = rootView.findViewById(R.id.text);
        Context context = textView.getContext();

        CharSequence text = SpanBuilder.of("This is a regular span")
                .prependSpace()
                .prepend(".")
                .prepend(1)
                .appendNewLine()
                .append(2)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is a colored span")
                        .color(context, R.color.colorPrimaryDark)
                        .build())
                .appendNewLine()
                .append(3)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is an italicized span")
                        .italic()
                        .build())
                .appendNewLine()
                .append(4)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is an underlined span")
                        .underline()
                        .build())
                .appendNewLine()
                .append(5)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is a bold span")
                        .bold()
                        .build())
                .appendNewLine()
                .append(6)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is a resized span")
                        .resize(1.2F)
                        .build())
                .appendNewLine()
                .append(7)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is a clickable span")
                        .click(textView, () -> Snackbar.make(textView, "Clicked text!", Snackbar.LENGTH_SHORT).show())
                        .build())
                .appendNewLine()
                .build();

        textView.setText(text);

        return rootView;
    }
}
