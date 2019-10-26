package com.tunjid.androidx.fragments

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.Fade
import android.transition.TransitionSet
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.ImageView
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.PlaceHolder
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.DoggoAdapter
import com.tunjid.androidx.adapters.DoggoAdapter.ImageListAdapterListener
import com.tunjid.androidx.adapters.withPaddedAdapter
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.graphics.drawable.withTint
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.recyclerview.ListManager
import com.tunjid.androidx.recyclerview.ListManagerBuilder
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.hashTransitionName
import com.tunjid.androidx.viewholders.DoggoViewHolder
import com.tunjid.androidx.viewmodels.routeName
import java.util.Objects.requireNonNull
import kotlin.math.abs

class DoggoListFragment : AppBaseFragment(R.layout.fragment_doggo_list),
        ImageListAdapterListener,
        Navigator.TransactionModifier {

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
                toolbarTitle = this::class.java.routeName,
                toolBarMenu = 0,
                toolbarShows = true,
                fabIcon = R.drawable.ic_paw_24dp,
                fabText = getString(R.string.collapse_prompt),
                fabShows = true,
                showsBottomNav = true,
                lightStatusBar = !requireContext().isDarkTheme,
                fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
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
        navigator.push(DoggoPagerFragment.newInstance())
    }

    override fun onDoggoImageLoaded(doggo: Doggo) {
        if (doggo == Doggo.transitionDoggo) startPostponedEnterTransition()
    }

    private fun getDivider(orientation: Int): RecyclerView.ItemDecoration = requireContext().run {
        val decoration = DividerItemDecoration(this, orientation)
        decoration.setDrawable(requireNonNull<Drawable>(getDrawable(this, R.drawable.bg_divider).withTint(themeColorAt(R.attr.colorSurface))))
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
    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        if (incomingFragment !is DoggoPagerFragment) return

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
                .addSharedElement(imageView, imageView.hashTransitionName(doggo))
    }

    companion object {
        fun newInstance(): DoggoListFragment = DoggoListFragment().apply { arguments = Bundle() }
    }
}
