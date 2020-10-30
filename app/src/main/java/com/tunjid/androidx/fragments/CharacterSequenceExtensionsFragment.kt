package com.tunjid.androidx.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.text.*
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.viewmodels.routeName

/**
 * Fragment showing the [CharSequence] extension functions
 *
 * Created by tj.dahunsi on 5/6/17.
 */

class CharacterSequenceExtensionsFragment : Fragment(R.layout.fragment_spanbuilder) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarOverlaps = false,
                toolbarShows = true,
                toolbarMenuRes = 0,
                fabShows = false,
                showsBottomNav = true,
                insetFlags = InsetFlags.ALL,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        view.findViewById<TextView>(R.id.text).apply {
            val color = context.themeColorAt(R.attr.prominent_text_color)
            movementMethod = LinkMovementMethod.getInstance()
            text = SpannableStringBuilder()
                    .append("\n")
                    .append(
                            "Spans ".bold() +
                                    "in ".color(Color.RED) +
                                    "Android ".color(Color.GREEN).bold() +
                                    "_".applyStyles(
                                            ImageSpan(
                                                    requireContext(),
                                                    R.drawable.ic_android_24dp,
                                                    DynamicDrawableSpan.ALIGN_BASELINE
                                            )
                                    ) +
                                    " made " +
                                    "easy!".bold().italic().underline().scale(1.2f)
                    )
                    .append("\n")
                    .append("\n")
                    .append("1. This is a regular span.")
                    .append("\n")
                    .append("\n")
                    .append("2. This is a colored span".color(color))
                    .append("\n")
                    .append("\n")
                    .append("3. This is an italicized span".italic())
                    .append("\n")
                    .append("\n")
                    .append("4. This is an underlined span".underline())
                    .append("\n")
                    .append("\n")
                    .append("5. This is a bold span".bold())
                    .append("\n")
                    .append("\n")
                    .append("6. This is a resized span".scale(1.5F))
                    .append("\n")
                    .append("\n")
                    .append("7. This is a span scaled in the X".scaleX(1.5F))
                    .append("\n")
                    .append("\n")
                    .append("8. This is a strike through span".strikeThrough())
                    .append("\n")
                    .append("\n")
                    .append("9. This is a background colored span".backgroundColor(context.themeColorAt(R.attr.prominent_text_color)))
                    .append("\n")
                    .append("\n")
                    .append(
                            SpannableStringBuilder("10. This span has a " as CharSequence
                                    + "subscript".subScript().scale(0.6F)
                                    + ","
                                    + "superscript".superScript().scale(0.6F)
                                    + " and a |"
                                    + "Baseline shift".shiftBaseline(0.5f).scale(0.5F)
                                    + "| all in one"
                            )
                                    .append("\n")
                                    .append("\n")
                                    .append("11. This is a clickable span".click(
                                            { paint -> paint.isUnderlineText = true },
                                            { uiState = uiState.copy(snackbarText = "Clicked text!") })
                                    )
                                    .append("\n")
                                    .append("\n")
                                    .append(
                                            "Hi! I am bold, italicized and underlined with a fluent api"
                                                    .bold().italic().underline()
                                    )
                                    .append("\n")
                                    .append("\n")
                                    .append(
                                            "Hi! I am bold, italicized and underlined but slightly more efficient".applyStyles(
                                                    StyleSpan(Typeface.BOLD),
                                                    StyleSpan(Typeface.ITALIC),
                                                    UnderlineSpan())
                                    )
                                    .append("\n")
                                    .append("\n")
                                    .append(
                                            "Please accept the %1\$s and %2\$s before continuing".formatSpanned(
                                                    "terms".underline().click { uiState = uiState.copy(snackbarText = "Clicked Terms") },
                                                    "conditions".underline().click { uiState = uiState.copy(snackbarText = "Clicked Conditions") })
                                    )
                                    .append("\n")
                                    .append("\n")
                                    .append(
                                            "This".bold() +
                                                    " last " +
                                                    "paragraph".italic() +
                                                    " is a " +
                                                    "flex".underline() +
                                                    " to show the " +
                                                    "plus".bold().italic().color(color).scale(1.8f) +
                                                    " operator overload".color(color)
                                    )
                                    .append("\n")
                                    .append("\n")
                    )

        }
    }

    companion object {
        fun newInstance(): CharacterSequenceExtensionsFragment = CharacterSequenceExtensionsFragment().apply { arguments = Bundle() }
    }
}
