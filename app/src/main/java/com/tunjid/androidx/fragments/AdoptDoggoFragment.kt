package com.tunjid.androidx.fragments

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.transition.Fade
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.tunjid.androidx.R
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.FragmentSimpleListBinding
import com.tunjid.androidx.databinding.ViewholderDoggoAdoptBinding
import com.tunjid.androidx.databinding.ViewholderSimpleInputBinding
import com.tunjid.androidx.divider
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.typed
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.uidrivers.BACKGROUND_TINT_DURATION
import com.tunjid.androidx.uidrivers.activityGlobalUiController
import com.tunjid.androidx.uidrivers.baseSharedTransition
import com.tunjid.androidx.uidrivers.update
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.viewholders.DoggoBinder
import com.tunjid.androidx.viewholders.bind
import com.tunjid.androidx.viewholders.inputViewHolder

class AdoptDoggoFragment : Fragment(R.layout.fragment_simple_list) {

    var doggo: Doggo by args()

    private var uiState by activityGlobalUiController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        val initialUiState = uiState
        uiState = uiState.copy(
                toolBarMenu = 0,
                toolbarShows = true,
                toolbarOverlaps = true,
                toolbarTitle = doggo.name,
                fabText = getString(R.string.adopt),
                fabIcon = R.drawable.ic_hug_24dp,
                fabShows = true,
                lightStatusBar = false,
                showsBottomNav = true,
                insetFlags = InsetFlags.NO_TOP,
                fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
                fabClickListener = {
                    ::uiState.update { copy(snackbarText = getString(R.string.adopted_doggo, doggo.name)) }
                }
        )

        enterTransition = Fade()
        sharedElementEnterTransition = baseSharedTransition(initialUiState)
        sharedElementReturnTransition = baseSharedTransition(uiState)

        FragmentSimpleListBinding.bind(view).recyclerView.apply {
            layoutManager = verticalLayoutManager()
            adapter = adapterOf(
                    itemsSource = {
                        listOf(AdoptItem.Header) + listOf(*resources.getStringArray(R.array.adoption_items)).map(AdoptItem::Input)
                    },
                    viewHolderCreator = { parent, viewType ->
                        when (viewType) {
                            AdoptItem.Input::class.java.hashCode() -> parent.inputViewHolder()
                            AdoptItem.Header::class.java.hashCode() -> parent.headerHolder()
                            else -> throw IllegalArgumentException("Unknown view type")
                        }
                    },
                    viewHolderBinder = { viewHolder, item, _ ->
                        when (item) {
                            is AdoptItem.Input -> viewHolder.typed<ViewholderSimpleInputBinding>().bind(item.hint)
                            is AdoptItem.Header -> viewHolder.typed<ViewholderDoggoAdoptBinding>().doggoBinder.bind(doggo)
                        }
                    },
                    viewTypeFunction = { it.javaClass.hashCode() },
                    itemIdFunction = { it.hashCode().toLong() }
            )
            addItemDecoration(context.divider(DividerItemDecoration.HORIZONTAL))
        }
    }

    private fun ViewGroup.headerHolder() = viewHolderFrom(ViewholderDoggoAdoptBinding::inflate).apply {
        doggoBinder = object : DoggoBinder {
            override var doggo: Doggo? = null
            override val doggoName: TextView? get() = null
            override val thumbnail: ImageView get() = binding.doggoImage
            override val fullResolution: ImageView? get() = binding.fullSize
            override fun onDoggoThumbnailLoaded(doggo: Doggo) = startPostponedEnterTransition()
        }

        binding.doggoImage.tint(R.color.black_50) { color, imageView -> imageView.setColorFilter(color) }
        binding.fullSize.tint(R.color.black_50) { color, imageView -> imageView.setColorFilter(color) }
    }

    companion object {
        fun newInstance(doggo: Doggo): AdoptDoggoFragment = AdoptDoggoFragment().apply {
            this.doggo = doggo
        }
    }
}

var BindingViewHolder<ViewholderDoggoAdoptBinding>.doggoBinder by BindingViewHolder.Prop<DoggoBinder>()

private fun <T : View> T.tint(@ColorRes colorRes: Int, biConsumer: (Int, T) -> Unit) {
    val endColor = ContextCompat.getColor(context, colorRes)
    val startColor = Color.TRANSPARENT

    val animator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
    animator.duration = BACKGROUND_TINT_DURATION
    animator.addUpdateListener { animation ->
        (animation.animatedValue as? Int)?.let { biConsumer.invoke(it, this) }
    }
    animator.start()
}

private sealed class AdoptItem {
    object Header : AdoptItem()
    data class Input(val hint: CharSequence) : AdoptItem()
}