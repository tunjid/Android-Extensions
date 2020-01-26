package com.tunjid.androidx.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.chip.ChipGroup
import com.tunjid.androidx.MutedColors
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.text.bold
import com.tunjid.androidx.core.text.click
import com.tunjid.androidx.core.text.color
import com.tunjid.androidx.core.text.formatSpanned
import com.tunjid.androidx.core.text.italic
import com.tunjid.androidx.core.text.scale
import com.tunjid.androidx.core.text.underline
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.addOnBackPressedCallback
import com.tunjid.androidx.navigation.childMultiStackNavigationController
import com.tunjid.androidx.uidrivers.crossFade
import com.tunjid.androidx.uidrivers.slide
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.viewmodels.routeName


class MultipleStacksFragment : AppBaseFragment(R.layout.fragment_multiple_stack) {

    override val insetFlags: InsetFlags = InsetFlags.NO_TOP

    private var transitionOption: Int = R.id.slide

    internal val innerNavigator: MultiStackNavigator by childMultiStackNavigationController(
            DESTINATIONS.size,
            R.id.inner_container
    ) { index ->
        MultipleStackChildFragment.newInstance(getChildName(index), 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback {
            isEnabled =
                    if (navigator.current !== this@MultipleStacksFragment) false
                    else innerNavigator.pop()

            if (!isEnabled) activity?.onBackPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabs = view.findViewById<ChipGroup>(R.id.tabs)

        innerNavigator.stackSelectedListener = { tabs.check(DESTINATIONS[it]) }
        innerNavigator.transactionModifier = { crossFade() }
        innerNavigator.stackTransactionModifier = { index ->
            when (transitionOption) {
                R.id.slide -> slide(index > innerNavigator.activeIndex)
                R.id.cross_fade -> crossFade()
            }
        }

        tabs.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) innerNavigator.show(DESTINATIONS.indexOf(checkedId))
        }

        view.findViewById<ChipGroup>(R.id.options).setOnCheckedChangeListener { _, checkedId ->
            transitionOption = checkedId
        }

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName.color(Color.WHITE),
                toolBarMenu = R.menu.menu_default,
                toolbarShows = true,
                fabText = getString(R.string.go_deeper),
                fabIcon = R.drawable.ic_bullseye_24dp,
                fabShows = true,
                fabClickListener = View.OnClickListener {
                    val current = innerNavigator.current as? MultipleStackChildFragment
                    if (current != null) innerNavigator.push(MultipleStackChildFragment.newInstance(current.name, current.depth + 1))
                },
                showsBottomNav = true,
                lightStatusBar = false,
                navBarColor = requireContext().colorAt(R.color.transparent)
        )
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        view?.apply { transitionOption = findViewById<ChipGroup>(R.id.options).checkedChipId }
    }

    private fun getChildName(index: Int) = resources.getResourceEntryName(DESTINATIONS[index])

    companion object {

        private val DESTINATIONS = intArrayOf(R.id.first, R.id.second, R.id.third)

        fun newInstance(): MultipleStacksFragment = MultipleStacksFragment().apply { arguments = Bundle() }
    }

}

class MultipleStackChildFragment : Fragment(), Navigator.TagProvider {

    override val stableTag: String
        get() = "${javaClass.simpleName}-$name-$depth"

    var name: String by args()

    var depth: Int by args()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = TextView(inflater.context).apply {
        text = resources.getString(R.string.triple_line_format).formatSpanned(
                name,
                resources.getQuantityString(R.plurals.stack_depth, depth, depth).scale(0.6F),
                resources.getString(R.string.clear).scale(0.6F)
                        .underline()
                        .italic()
                        .bold()
                        .click {
                            (parentFragment?.parentFragment as? MultipleStacksFragment)?.apply {
                                innerNavigator.clear()
                            }
                        }
        )
        gravity = Gravity.CENTER
        textSize = resources.getDimensionPixelSize(R.dimen.large_text).toFloat()
        movementMethod = LinkMovementMethod.getInstance()
        setBackgroundColor(MutedColors.colorAt(context.isDarkTheme, depth))
        setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    companion object {
        fun newInstance(name: String, depth: Int): MultipleStackChildFragment = MultipleStackChildFragment().apply {
            this.name = name
            this.depth = depth
        }
    }
}