package com.tunjid.androidx.fragments

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.tunjid.androidx.R
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.FragmentAdoptDoggoBinding
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.verticalLayoutManager
import com.tunjid.androidx.uidrivers.BACKGROUND_TINT_DURATION
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.activityGlobalUiController
import com.tunjid.androidx.uidrivers.baseSharedTransition
import com.tunjid.androidx.uidrivers.update
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.inflate
import com.tunjid.androidx.viewholders.DoggoBinder
import com.tunjid.androidx.viewholders.InputViewHolder
import com.tunjid.androidx.viewholders.bind

class AdoptDoggoFragment : Fragment(R.layout.fragment_adopt_doggo) {

    var doggo: Doggo by args()

    private var uiState by activityGlobalUiController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        ).also(::prepareSharedElementTransition)

        val items = listOf(*resources.getStringArray(R.array.adoption_items))

        val binding = FragmentAdoptDoggoBinding.bind(view)

        binding.modelList.apply {
            layoutManager = verticalLayoutManager()
            adapter = adapterOf(
                    itemsSource = { items },
                    viewHolderCreator = { parent, _ -> InputViewHolder(parent.inflate(R.layout.viewholder_simple_input)) },
                    viewHolderBinder = { viewHolder, hint, _ -> viewHolder.bind(hint) },
                    itemIdFunction = { it.hashCode().toLong() },
                    onViewHolderRecycled = InputViewHolder::unbind,
                    onViewHolderDetached = InputViewHolder::unbind
            )
        }

        object : DoggoBinder {
            override var doggo: Doggo?
                get() = null
                set(_) = Unit
            override val doggoName: TextView get() = binding.doggoName
            override val thumbnail: ImageView get() = binding.doggoImage
            override val fullResolution: ImageView? get() = binding.fullSize
            override fun onDoggoThumbnailLoaded(doggo: Doggo) = Unit
        }.bind(doggo)

        binding.doggoImage.tint(R.color.black_50) { color, imageView -> this.setColorFilter(color, imageView) }
        binding.fullSize.tint(R.color.black_50) { color, imageView -> this.setColorFilter(color, imageView) }
    }

    private fun setColorFilter(color: Int, imageView: ImageView) = imageView.setColorFilter(color)

    private fun <T : View> T.tint(@ColorRes colorRes: Int, biConsumer: (Int, T) -> Unit) {
        val endColor = ContextCompat.getColor(requireContext(), colorRes)
        val startColor = Color.TRANSPARENT

        val animator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        animator.duration = BACKGROUND_TINT_DURATION
        animator.addUpdateListener { animation ->
            (animation.animatedValue as? Int)?.let { biConsumer.invoke(it, this) }
        }
        animator.start()
    }

    private fun prepareSharedElementTransition(after: UiState) {
        sharedElementEnterTransition = baseSharedTransition()
        sharedElementReturnTransition = baseSharedTransition(uiState, after)
    }

    companion object {

        fun newInstance(doggo: Doggo): AdoptDoggoFragment = AdoptDoggoFragment().apply {
            this.doggo = doggo
        }
    }
}
