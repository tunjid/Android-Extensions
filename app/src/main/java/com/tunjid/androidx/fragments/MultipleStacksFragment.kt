package com.tunjid.androidx.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.transition.Transition
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.doOnDetach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.MaterialSharedAxis.X
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
import com.tunjid.androidx.databinding.FragmentMultipleStackBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.addOnBackPressedCallback
import com.tunjid.androidx.navigation.childMultiStackNavigationController
import com.tunjid.androidx.uidrivers.GlobalUiController
import com.tunjid.androidx.uidrivers.activityGlobalUiController
import com.tunjid.androidx.uidrivers.materialDepthAxisTransition
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.viewmodels.routeName


class MultipleStacksFragment : AppBaseFragment(R.layout.fragment_multiple_stack) {

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

        val binding = FragmentMultipleStackBinding.bind(view)

        innerNavigator.stackSelectedListener = { binding.tabs.check(DESTINATIONS[it]) }
        innerNavigator.transactionModifier = innerNavigator.materialDepthAxisTransition()
        innerNavigator.stackTransactionModifier = stackTransactionAnimator()

        binding.tabs.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && checkedId != View.NO_ID && !childFragmentManager.isStateSaved) innerNavigator.show(DESTINATIONS.indexOf(checkedId))
        }
        binding.tabs.children.filterIsInstance<MaterialButton>().forEach {
            it.setTextColor(ColorStateList(
                    arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
                    intArrayOf(Color.LTGRAY, Color.WHITE)
            ))
        }
        binding.tabs.doOnDetach {  innerNavigator.stackSelectedListener = null }
        binding.options.setOnCheckedChangeListener { _, checkedId ->
            transitionOption = checkedId
        }

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName.color(Color.WHITE),
                toolBarMenu = R.menu.menu_default,
                toolbarShows = true,
                toolbarOverlaps = false,
                fabText = getString(R.string.go_deeper),
                fabIcon = R.drawable.ic_bullseye_24dp,
                fabShows = true,
                insetFlags = InsetFlags.NO_TOP,
                fabClickListener = {
                    val current = innerNavigator.current as? MultipleStackChildFragment
                    if (current != null) innerNavigator.push(MultipleStackChildFragment.newInstance(current.name, current.depth + 1))
                },
                showsBottomNav = true,
                lightStatusBar = false,
                navBarColor = requireContext().colorAt(R.color.colorSurface)
        )
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        view?.apply { transitionOption = findViewById<ChipGroup>(R.id.options).checkedChipId }
    }

    override fun onPause() {
        super.onPause()
        uiState = uiState.copy(backgroundColor = Color.TRANSPARENT)
    }

    private fun getChildName(index: Int) = resources.getResourceEntryName(DESTINATIONS[index])

    @Suppress("USELESS_CAST")
    private fun stackTransactionAnimator(): FragmentTransaction.(Int) -> Unit = transition@{ toIndex ->
        val fromIndex = innerNavigator.activeIndex
        val isForward = toIndex > fromIndex
        val isSliding = transitionOption == R.id.slide

        val from = childFragmentManager.findFragmentByTag(fromIndex.toString()) ?: return@transition
        val to = childFragmentManager.findFragmentByTag(toIndex.toString()) ?: return@transition

        // Casting is necessary for over enthusiastic Kotlin compiler CHECKCAST generation
        val (enterFrom, exitFrom, enterTo, exitTo) = arrayOf(
                if (isSliding) MaterialSharedAxis.create(X, !isForward) else null,
                if (isSliding) MaterialSharedAxis.create(X, isForward) else MaterialFadeThrough.create() as Transition,
                if (isSliding) MaterialSharedAxis.create(X, isForward) else MaterialFadeThrough.create() as Transition,
                if (isSliding) MaterialSharedAxis.create(X, !isForward) else null
        )

        from.apply { enterTransition = enterFrom; exitTransition = exitFrom }
        to.apply { enterTransition = enterTo; exitTransition = exitTo }
    }

    companion object {

        private val DESTINATIONS = intArrayOf(R.id.first, R.id.second, R.id.third)

        fun newInstance(): MultipleStacksFragment = MultipleStacksFragment().apply { arguments = Bundle() }
    }

}

class MultipleStackChildFragment : Fragment(),
        GlobalUiController,
        Navigator.TagProvider {

    override val stableTag: String get() = "${javaClass.simpleName}-$name-$depth"

    override var uiState by activityGlobalUiController()

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
        setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    override fun onResume() {
        super.onResume()
        uiState = uiState.copy(backgroundColor = MutedColors.colorAt(requireContext().isDarkTheme, depth))
    }

    companion object {
        fun newInstance(name: String, depth: Int): MultipleStackChildFragment = MultipleStackChildFragment().apply {
            this.name = name
            this.depth = depth
        }
    }
}