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
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.DoggoInteractionListener
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.graphics.drawable.withTint
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.addScrollListener
import com.tunjid.androidx.recyclerview.gridLayoutManager
import com.tunjid.androidx.recyclerview.viewHolderForItemId
import com.tunjid.androidx.uidrivers.InsetLifecycleCallbacks
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.hashTransitionName
import com.tunjid.androidx.view.util.inflate
import com.tunjid.androidx.viewholders.DoggoViewHolder
import com.tunjid.androidx.viewmodels.routeName
import java.util.Objects.requireNonNull
import kotlin.math.abs

class DoggoListFragment : AppBaseFragment(R.layout.fragment_doggo_list),
        DoggoInteractionListener,
        Navigator.TransactionModifier {

    override val insetFlags: InsetFlags = InsetFlags.ALL

    private var recyclerView: RecyclerView? = null

    private val transitionImage: ImageView?
        get() {
            val doggo = Doggo.transitionDoggo ?: return null
            val holder = recyclerView?.viewHolderForItemId<DoggoViewHolder>(doggo.hashCode().toLong())
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

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = gridLayoutManager(2)
            adapter = adapterOf(
                    itemsSource = Doggo.Companion::doggos,
                    viewHolderCreator = { parent, _ -> DoggoViewHolder(parent.inflate(R.layout.viewholder_doggo_list), this@DoggoListFragment) },
                    viewHolderBinder = { viewHolder, doggo, _ -> viewHolder.bind(doggo) },
                    itemIdFunction = { it.hashCode().toLong() }
            )

            addScrollListener { _, dy -> if (abs(dy) > 4) uiState = uiState.copy(fabExtended = dy < 0) }
            addItemDecoration(getDivider(DividerItemDecoration.HORIZONTAL))
            addItemDecoration(getDivider(DividerItemDecoration.VERTICAL))
            updatePadding(bottom = InsetLifecycleCallbacks.bottomInset)
        }

        postponeEnterTransition()

        scrollToPosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
    }

    override fun onDoggoClicked(doggo: Doggo) {
        Doggo.transitionDoggo = doggo
        navigator.push(DoggoPagerFragment.newInstance())
    }

    override fun onDoggoImageLoaded(doggo: Doggo) {
        if (doggo == Doggo.transitionDoggo) view?.doOnLayout { startPostponedEnterTransition() }
    }

    private fun getDivider(orientation: Int): RecyclerView.ItemDecoration = requireContext().run {
        val decoration = DividerItemDecoration(this, orientation)
        decoration.setDrawable(requireNonNull<Drawable>(getDrawable(this, R.drawable.bg_divider)?.withTint(themeColorAt(R.attr.colorSurface))))
        return decoration
    }

    private fun scrollToPosition() = recyclerView?.apply {
        addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                removeOnLayoutChangeListener(this)
                val last = Doggo.transitionDoggo ?: return

                val index = Doggo.doggos.indexOf(last)
                if (index < 0) return

                val layoutManager = layoutManager ?: return

                val viewAtPosition = layoutManager.findViewByPosition(index)
                val shouldScroll = viewAtPosition == null || layoutManager.isViewPartiallyVisible(viewAtPosition, false, true)

                if (shouldScroll) post { layoutManager.scrollToPosition(index) }
            }
        })
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
