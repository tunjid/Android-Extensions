package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.resolveThemeColor
import com.tunjid.androidx.core.text.SpanBuilder
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.viewmodels.routeName

/**
 * Fragment showing the use of a SpanBuilder
 *
 * Created by tj.dahunsi on 5/6/17.
 */

class SpanbuilderFragment : AppBaseFragment(R.layout.fragment_spanbuilder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarShows = true,
                toolBarMenu = 0,
                fabShows = false,
                showsBottomNav = true,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().resolveThemeColor(R.attr.nav_bar_color)
        )

        val textView = view.findViewById<TextView>(R.id.text)
        val context = textView.context

        val text = SpanBuilder.of("This is a regular span")
                .prependSpace()
                .prepend(".")
                .prepend(1)
                .appendNewLine()
                .append(2)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is a colored span")
                        .color(context.resolveThemeColor(R.attr.prominent_text_color))
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
                        .resize(1.2f)
                        .build())
                .appendNewLine()
                .append(7)
                .append(".")
                .appendSpace()
                .append(SpanBuilder.of("This is a clickable span")
                        .click(textView,
                                { paint -> paint.isUnderlineText = true },
                                { uiState = uiState.copy(snackbarText = "Clicked text!") })
                        .build())
                .appendNewLine()
                .build()

        textView.text = text
    }

    companion object {
        fun newInstance(): SpanbuilderFragment = SpanbuilderFragment().apply { arguments = Bundle() }
    }
}
