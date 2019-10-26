package com.tunjid.androidx.fragments

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.core.text.*
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.text.click
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
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        view.findViewById<TextView>(R.id.text).apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = SpannableStringBuilder("1. This is a regular span.")
                    .append("\n")
                    .color(context.themeColorAt(R.attr.prominent_text_color)) {
                        append("2. This is a colored span")
                    }
                    .append("\n")
                    .italic {
                        append("3. This is an italicized span")
                    }
                    .append("\n")
                    .underline {
                        append("4. This is an underlined span")
                    }
                    .append("\n")
                    .bold {
                        append("5. This is a bold span")
                    }
                    .append("\n")
                    .scale(1.5F) {
                        append("6. This is a resized span")
                    }
                    .append("\n")
                    .append(
                            SpannableStringBuilder("7. This is a clickable span").click(
                                    { paint -> paint.isUnderlineText = true },
                                    { uiState = uiState.copy(snackbarText = "Clicked text!") })
                    )
        }
    }

    companion object {
        fun newInstance(): SpanbuilderFragment = SpanbuilderFragment().apply { arguments = Bundle() }
    }
}
