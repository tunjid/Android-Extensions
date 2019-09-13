package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.chip.ChipGroup
import com.tunjid.androidbootstrap.*
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.core.components.MultiStackNavigator
import com.tunjid.androidbootstrap.core.components.args
import com.tunjid.androidbootstrap.core.components.multipleStackNavigator
import com.tunjid.androidbootstrap.view.util.InsetFlags


class MultipleStackFragment : AppBaseFragment(R.layout.fragment_multiple_stack), GlobalUiController {

    override val insetFlags: InsetFlags = InsetFlags.NONE

    override var uiState: UiState by activityGlobalUiController()

    private var transitionOption: Int = 0

    private val innerNavigator: MultiStackNavigator by multipleStackNavigator(
            R.id.inner_container,
            DESTINATIONS
    ) { InnerFragment.newInstance(resources.getResourceEntryName(it)) to it.toString() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        innerNavigator.stackTransactionModifier = { selectedId ->
            when (transitionOption) {
                R.id.slide -> slide(
                        DESTINATIONS.indexOf(selectedId) > DESTINATIONS.indexOf(innerNavigator.currentNavigator.containerId)
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
                fabShows = false,
                showsBottomNav = false,
                navBarColor = ContextCompat.getColor(requireContext(), R.color.transparent)
        )
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        view?.apply { transitionOption = findViewById<ChipGroup>(R.id.options).checkedChipId }
    }

    class InnerFragment : Fragment() {

        var name: String by args()

        companion object {
            fun newInstance(name: String): InnerFragment = InnerFragment().apply { this.name = name }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
                TextView(inflater.context).apply {
                    text = name
                    gravity = Gravity.CENTER
                    textSize = resources.getDimensionPixelSize(R.dimen.large_text).toFloat()
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                }
    }

    companion object {

        private val DESTINATIONS = intArrayOf(R.id.first, R.id.second, R.id.third)

        fun newInstance(): MultipleStackFragment = MultipleStackFragment().apply { arguments = Bundle() }
    }

}