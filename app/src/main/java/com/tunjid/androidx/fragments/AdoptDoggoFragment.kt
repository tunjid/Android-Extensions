package com.tunjid.androidx.fragments

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.tunjid.androidx.PlaceHolder
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.ImageListAdapterListener
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.recyclerview.ListManagerBuilder
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.uidrivers.BACKGROUND_TINT_DURATION
import com.tunjid.androidx.uidrivers.baseSharedTransition
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.inflate
import com.tunjid.androidx.viewholders.DoggoViewHolder
import com.tunjid.androidx.viewholders.InputViewHolder

class AdoptDoggoFragment : AppBaseFragment(R.layout.fragment_adopt_doggo),
        ImageListAdapterListener {

    override val insetFlags: InsetFlags = InsetFlags.NO_TOP

    override val stableTag: String
        get() = "${super.stableTag}-${doggo.hashCode()}"

    var doggo: Doggo by args()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarShows = false,
                toolBarMenu = 0,
                fabText = getString(R.string.adopt),
                fabIcon = R.drawable.ic_hug_24dp,
                fabShows = true,
                lightStatusBar = false,
                showsBottomNav = true,
                fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
                fabClickListener = View.OnClickListener {
                    uiState = uiState.copy(snackbarText = getString(R.string.adopted_doggo, doggo.name))
                }
        )

        val items = listOf(*resources.getStringArray(R.array.adoption_items))

        ListManagerBuilder<InputViewHolder, PlaceHolder.State>()
                .withRecyclerView(view.findViewById(R.id.model_list))
                .withLinearLayoutManager()
                .withAdapter(
                        adapterOf(
                                itemsSource = { items },
                                viewHolderCreator = { parent, _ -> InputViewHolder(parent.inflate(R.layout.viewholder_simple_input)) },
                                viewHolderBinder = { viewHolder, hint, _ -> viewHolder.bind(hint) },
                                itemIdFunction = { it.hashCode().toLong() }
                        )
                )
                .build()

        val viewHolder = DoggoViewHolder(view, this)
        viewHolder.bind(doggo)

        viewHolder.thumbnail.tint(R.color.black_50) { color, imageView -> this.setColorFilter(color, imageView) }
        viewHolder.fullSize?.let { viewHolder.fullSize.tint(R.color.black_50) { color, imageView -> this.setColorFilter(color, imageView) } }
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

    private fun prepareSharedElementTransition() {
        sharedElementEnterTransition = baseSharedTransition()
        sharedElementReturnTransition = baseSharedTransition()
    }

    companion object {

        fun newInstance(doggo: Doggo): AdoptDoggoFragment = AdoptDoggoFragment().apply {
            this.doggo = doggo
            prepareSharedElementTransition()
        }
    }

}
