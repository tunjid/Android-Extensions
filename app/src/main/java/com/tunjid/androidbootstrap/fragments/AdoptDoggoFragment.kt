package com.tunjid.androidbootstrap.fragments

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.tunjid.androidbootstrap.*
import com.tunjid.androidbootstrap.adapters.DoggoAdapter.ImageListAdapterListener
import com.tunjid.androidbootstrap.adapters.InputAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.core.components.args
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.uidrivers.GlobalUiController
import com.tunjid.androidbootstrap.uidrivers.UiState
import com.tunjid.androidbootstrap.uidrivers.activityGlobalUiController
import com.tunjid.androidbootstrap.view.util.InsetFlags
import com.tunjid.androidbootstrap.viewholders.DoggoViewHolder
import com.tunjid.androidbootstrap.viewholders.InputViewHolder

class AdoptDoggoFragment : AppBaseFragment(R.layout.fragment_adopt_doggo), GlobalUiController, ImageListAdapterListener {

    override var uiState: UiState by activityGlobalUiController()

    override val insetFlags: InsetFlags = InsetFlags.NO_TOP

    override val stableTag: String
        get() = super.stableTag + "-" + doggo.hashCode()

    var doggo: Doggo by args()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarShows = false,
                toolBarMenu = 0,
                fabText = getString(R.string.adopt),
                fabIcon = R.drawable.ic_hug_24dp,
                fabShows = true,
                showsBottomNav = true,
                fabExtended = !restoredFromBackStack(),
                navBarColor = ContextCompat.getColor(requireContext(), R.color.white_75),
                fabClickListener = View.OnClickListener {
                    showSnackbar { it.setText(getString(R.string.adopted_doggo, doggo.name)) }
                }
        )

        ListManagerBuilder<InputViewHolder, PlaceHolder.State>()
                .withRecyclerView(view.findViewById(R.id.model_list))
                .withLinearLayoutManager()
                .withAdapter(InputAdapter(listOf(*resources.getStringArray(R.array.adoption_items))))
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
        animator.duration = BACKGROUND_TINT_DURATION.toLong()
        animator.addUpdateListener { animation ->
            (animation.animatedValue as? Int)?.let { biConsumer.invoke(it, this) }
        }
        animator.start()
    }

    private fun prepareSharedElementTransition() {
        val baseSharedTransition = baseSharedTransition()

        sharedElementEnterTransition = baseSharedTransition
        sharedElementReturnTransition = baseSharedTransition
    }

    companion object {

        fun newInstance(doggo: Doggo): AdoptDoggoFragment = AdoptDoggoFragment().apply {
            this.doggo = doggo
            prepareSharedElementTransition()
        }
    }

}
