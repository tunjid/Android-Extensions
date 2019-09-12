package com.tunjid.androidbootstrap.fragments

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.Fade
import android.transition.TransitionSet
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.ImageView
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidbootstrap.*
import com.tunjid.androidbootstrap.adapters.DoggoAdapter
import com.tunjid.androidbootstrap.adapters.DoggoAdapter.ImageListAdapterListener
import com.tunjid.androidbootstrap.adapters.withPaddedAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.core.components.FragmentStackNavigator
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.recyclerview.ListManager
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.view.util.InsetFlags
import com.tunjid.androidbootstrap.view.util.ViewUtil
import com.tunjid.androidbootstrap.viewholders.DoggoViewHolder
import java.util.Objects.requireNonNull
import kotlin.math.abs

class DoggoListFragment : AppBaseFragment(R.layout.fragment_doggo_list), GlobalUiController, ImageListAdapterListener {

    override var uiState: UiState by activityGlobalUiController()

    override val insetFlags: InsetFlags = InsetFlags.ALL

    private lateinit var listManager: ListManager<DoggoViewHolder, PlaceHolder.State>

    private val transitionImage: ImageView?
        get() {
            val doggo = Doggo.transitionDoggo ?: return null
            val holder = listManager.findViewHolderForItemId(doggo.hashCode().toLong())
                    ?: return null

            return holder.thumbnail
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.simpleName,
                toolBarMenu = 0,
                toolbarShows = true,
                fabIcon = R.drawable.ic_paw_24dp,
                fabText = getString(R.string.collapse_prompt),
                fabShows = true,
                showsBottomNav = true,
                fabExtended = !restoredFromBackStack(),
                navBarColor = ContextCompat.getColor(requireContext(), R.color.white_75),
                fabClickListener = View.OnClickListener { uiState = uiState.copy(fabExtended = !uiState.fabExtended) }
        )

        listManager = ListManagerBuilder<DoggoViewHolder, PlaceHolder.State>()
                .withRecyclerView(view.findViewById(R.id.recycler_view))
                .withPaddedAdapter(DoggoAdapter(
                        Doggo.doggos,
                        R.layout.viewholder_doggo_list,
                        { itemView, adapterListener -> DoggoViewHolder(itemView, adapterListener) },
                        this), 2)
                .addScrollListener { _, dy -> if (abs(dy) > 4) uiState = uiState.copy(fabExtended = dy < 0) }
                .addDecoration(getDivider(DividerItemDecoration.HORIZONTAL))
                .addDecoration(getDivider(DividerItemDecoration.VERTICAL))
                .withGridLayoutManager(2)
                .build()

        postponeEnterTransition()

        scrollToPosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listManager.clear()
    }

    override fun onDoggoClicked(doggo: Doggo) {
        Doggo.transitionDoggo = doggo
        navigator.show(DoggoPagerFragment.newInstance())
    }

    override fun onDoggoImageLoaded(doggo: Doggo) {
        if (doggo == Doggo.transitionDoggo) startPostponedEnterTransition()
    }

    private fun getDivider(orientation: Int): RecyclerView.ItemDecoration {
        val context = requireContext()
        val decoration = DividerItemDecoration(context, orientation)
        decoration.setDrawable(requireNonNull<Drawable>(getDrawable(context, R.drawable.bg_divider)))
        return decoration
    }

    private fun scrollToPosition() {
        listManager.withRecyclerView { recyclerView ->
            recyclerView.addOnLayoutChangeListener(object : OnLayoutChangeListener {
                override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                    recyclerView.removeOnLayoutChangeListener(this)
                    val last = Doggo.transitionDoggo ?: return

                    val index = Doggo.doggos.indexOf(last)
                    if (index < 0) return

                    val layoutManager = recyclerView.layoutManager ?: return

                    val viewAtPosition = layoutManager.findViewByPosition(index)
                    val shouldScroll = viewAtPosition == null || layoutManager.isViewPartiallyVisible(viewAtPosition, false, true)

                    if (shouldScroll) recyclerView.post { layoutManager.scrollToPosition(index) }
                }
            })
        }
    }

    @SuppressLint("CommitTransaction")
    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment){
        if (incomingFragment !is FragmentStackNavigator.TagProvider) return
        if (!incomingFragment.stableTag.contains(DoggoPagerFragment::class.java.simpleName)) return

        val doggo = Doggo.transitionDoggo
        val imageView = transitionImage
        if (doggo == null || imageView == null) return

        exitTransition = TransitionSet()
                .setDuration(375)
                .setStartDelay(25)
                .setInterpolator(FastOutSlowInInterpolator())
                .addTransition(Fade().addTarget(R.id.item_container))

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>?, sharedElements: MutableMap<String, View>?) {
                if (names == null || sharedElements == null) return

                val deferred = transitionImage
                if (deferred != null) sharedElements[names[0]] = deferred
            }
        })

        transaction
                .setReorderingAllowed(true)
                .addSharedElement(imageView, ViewUtil.transitionName(doggo, imageView))
    }

    companion object {
        fun newInstance(): DoggoListFragment = DoggoListFragment().apply { arguments = Bundle() }
    }
}
