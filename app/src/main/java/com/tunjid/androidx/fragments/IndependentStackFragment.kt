package com.tunjid.androidx.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.text.SpanBuilder
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.StackNavigator
import com.tunjid.androidx.navigation.childStackNavigationController


class IndependentStackFragment : AppBaseFragment(R.layout.fragment_independent_stack) {

    private var backPressedCallback: OnBackPressedCallback? = null

    private val containerIds = intArrayOf(R.id.quad_1, R.id.quad_2, R.id.quad_3, R.id.quad_4)
    private val navigators = mutableMapOf<Int, StackNavigator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        backPressedCallback = activity?.onBackPressedDispatcher?.addCallback(this) {
            isEnabled =
                    if (navigator.currentFragment !== this@IndependentStackFragment) false
                    else navigators.values.asSequence().map { it.pop() }.firstOrNull { it } ?: false

            if (!isEnabled) activity?.onBackPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) for (it in containerIds)
            navigatorFor(it).apply { show(IndependentStackChildFragment.newInstance(resources.getResourceEntryName(containerId), 1)) }

        uiState = uiState.copy(
                toolbarTitle = this::class.java.simpleName,
                toolBarMenu = 0,
                toolbarShows = true,
                fabShows = false,
                fabClickListener = View.OnClickListener {},
                showsBottomNav = true,
                navBarColor = ContextCompat.getColor(requireContext(), R.color.transparent)
        )
    }

    internal fun navigatorFor(id: Int) = navigators.getOrPut(id) {
        val stackNavigator by childStackNavigationController(id)
        stackNavigator
    }

    companion object {
        fun newInstance(): IndependentStackFragment = IndependentStackFragment().apply { arguments = Bundle() }
    }

}

class IndependentStackChildFragment : Fragment(), Navigator.TagProvider {

    override val stableTag: String
        get() = "${javaClass.simpleName}-$name-$depth"

    private var name: String by args()

    var depth: Int by args()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = TextView(inflater.context).apply {
        val spacing = context.resources.getDimensionPixelSize(R.dimen.single_margin)

        gravity = Gravity.CENTER

        textSize = resources.getDimensionPixelSize(R.dimen.small_text).toFloat()

        text = SpanBuilder.of(name)
                .appendNewLine()
                .append(SpanBuilder.of(resources.getQuantityString(R.plurals.stack_depth, depth, depth))
                        .resize(0.6F)
                        .build())
                .build()

        background = MaterialShapeDrawable.createWithElevationOverlay(context, elevation).apply {
            setTint(ContextCompat.getColor(context, R.color.colorPrimary))
            strokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            strokeWidth = context.resources.getDimensionPixelSize(R.dimen.eigth_margin).toFloat()
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setAllCorners(CornerFamily.ROUNDED, spacing)
                    .build()
        }

        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            gravity = Gravity.CENTER
            leftMargin = spacing
            topMargin = spacing
            rightMargin = spacing
            bottomMargin = spacing
        }

        setPadding(spacing)
        setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        setOnClickListener {
            val parent = parentFragment as? IndependentStackFragment
            parent?.navigatorFor(this@IndependentStackChildFragment.id)?.show(newInstance(name, depth + 1))
        }
    }

    companion object {
        fun newInstance(name: String, depth: Int): IndependentStackChildFragment = IndependentStackChildFragment().apply {
            this.name = name
            this.depth = depth
        }
    }
}