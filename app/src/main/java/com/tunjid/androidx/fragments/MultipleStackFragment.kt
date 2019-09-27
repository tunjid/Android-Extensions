package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.chip.ChipGroup
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.text.SpanBuilder
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.childMultiStackNavigator
import com.tunjid.androidx.uidrivers.*
import com.tunjid.androidx.view.util.InsetFlags


class MultipleStackFragment : AppBaseFragment(R.layout.fragment_multiple_stack), GlobalUiController {

    override val insetFlags: InsetFlags = InsetFlags.NO_TOP

    override var uiState: UiState by activityGlobalUiController()

    private var transitionOption: Int = R.id.slide

    private val innerNavigator: MultiStackNavigator by childMultiStackNavigator(
            R.id.inner_container,
            DESTINATIONS
    ) { InnerFragment.newInstance(resources.getResourceEntryName(it), 1) to it.toString() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        innerNavigator.transactionModifier = { crossFade() }
        innerNavigator.stackTransactionModifier = { selectedId ->
            when (transitionOption) {
                R.id.slide -> slide(
                        DESTINATIONS.indexOf(selectedId) > DESTINATIONS.indexOf(innerNavigator.activeNavigator.containerId)
                )
                R.id.cross_fade -> crossFade()
            }
        }

        view.findViewById<ChipGroup>(R.id.tabs).setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) innerNavigator.show(checkedId)
        }

        view.findViewById<ChipGroup>(R.id.options).setOnCheckedChangeListener { _, checkedId ->
            transitionOption = checkedId
        }

        uiState = uiState.copy(
                toolbarTitle = this::class.java.simpleName,
                toolBarMenu = 0,
                toolbarShows = true,
                fabText = getString(R.string.go_deeper),
                fabIcon = R.drawable.ic_bullseye_24dp,
                fabShows = true,
                fabClickListener = View.OnClickListener {
                    val current = innerNavigator.currentFragment as? InnerFragment
                    if (current != null) innerNavigator.show(InnerFragment.newInstance(current.name, current.depth + 1))
                },
                showsBottomNav = true,
                navBarColor = ContextCompat.getColor(requireContext(), R.color.transparent)
        )
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        view?.apply { transitionOption = findViewById<ChipGroup>(R.id.options).checkedChipId }
    }

    class InnerFragment : Fragment(), Navigator.TagProvider {

        override val stableTag: String
            get() = "${javaClass.simpleName}-$name-$depth"

        var name: String by args()

        var depth: Int by args()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = TextView(inflater.context).apply {
            text = SpanBuilder.of(name)
                    .appendNewLine()
                    .append(SpanBuilder.of(resources.getQuantityString(R.plurals.stack_depth, depth, depth))
                            .resize(0.6F)
                            .build())
                    .build()
            gravity = Gravity.CENTER
            textSize = resources.getDimensionPixelSize(R.dimen.large_text).toFloat()
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }

        companion object {
            fun newInstance(name: String, depth: Int): InnerFragment = InnerFragment().apply {
                this.name = name
                this.depth = depth
            }
        }
    }

    companion object {

        private val DESTINATIONS = intArrayOf(R.id.first, R.id.second, R.id.third)

        fun newInstance(): MultipleStackFragment = MultipleStackFragment().apply { arguments = Bundle() }
    }

}