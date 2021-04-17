package com.tunjid.androidx.tablists.doggo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.SharedElementCallback
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.transition.Fade
import androidx.transition.TransitionSet
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.core.delegates.viewLifecycle
import com.tunjid.androidx.databinding.FragmentDoggoListBinding
import com.tunjid.androidx.databinding.ViewholderDoggoListBinding
import com.tunjid.androidx.divider
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.activityNavigatorController
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.recyclerview.addScrollListener
import com.tunjid.androidx.recyclerview.gridLayoutManager
import com.tunjid.androidx.recyclerview.viewHolderForItemId
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderDelegate
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.updatePartial
import com.tunjid.androidx.view.util.hashTransitionName
import kotlin.math.abs

class DoggoListFragment : Fragment(R.layout.fragment_doggo_list),
    Navigator.TransactionModifier {

    private var isTopLevel by fragmentArgs<Boolean>()
    private val binding by viewLifecycle(FragmentDoggoListBinding::bind)
    private val navigator by activityNavigatorController<MultiStackNavigator>()

    private val transitionImage: ImageView?
        get() {
            val doggo = Doggo.transitionDoggo ?: return null
            val holder: BindingViewHolder<ViewholderDoggoListBinding> = binding.recyclerView.viewHolderForItemId(doggo.hashCode().toLong())
                ?: return null

            return holder.binding.doggoImage
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isTopLevel) uiState = UiState(
            toolbarTitle = this::class.java.routeName,
            toolbarMenuRes = 0,
            toolbarShows = true,
            toolbarOverlaps = false,
            fabIcon = R.drawable.ic_paw_24dp,
            fabText = getString(R.string.collapse_prompt),
            fabShows = true,
            showsBottomNav = true,
            insetFlags = InsetFlags.ALL,
            lightStatusBar = !requireContext().isDarkTheme,
            fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
            fabClickListener = { ::uiState.updatePartial { copy(fabExtended = !uiState.fabExtended) } }
        )

        binding.recyclerView.apply {
            layoutManager = gridLayoutManager(2)
            adapter = adapterOf(
                itemsSource = Doggo.Companion::doggos,
                viewHolderCreator = { parent, _ ->
                    parent.viewHolderFrom(ViewholderDoggoListBinding::inflate).apply {
                        doggoBinder = createDoggoBinder(
                            onThumbnailLoaded = { if (it == Doggo.transitionDoggo) view.doOnLayout { startPostponedEnterTransition() } },
                            onDoggoClicked = {
                                Doggo.transitionDoggo = it
                                navigator.push(DoggoPagerFragment.newInstance())
                            }
                        )
                    }
                },
                viewHolderBinder = { viewHolder, doggo, _ -> viewHolder.doggoBinder?.bind(doggo) },
                itemIdFunction = { it.hashCode().toLong() }
            )

            addScrollListener { _, dy -> if (abs(dy) > 4) uiState = uiState.copy(fabExtended = dy < 0) }
            addItemDecoration(context.divider(DividerItemDecoration.HORIZONTAL))
            addItemDecoration(context.divider(DividerItemDecoration.VERTICAL))
        }

        if (Doggo.transitionDoggo != null) postponeEnterTransition()

        scrollToPosition()
    }

    private fun scrollToPosition() = binding.recyclerView.apply {
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
        fun newInstance(isTopLevel: Boolean): DoggoListFragment = DoggoListFragment().apply { this.isTopLevel = isTopLevel }
    }
}

var BindingViewHolder<ViewholderDoggoListBinding>.doggoBinder by viewHolderDelegate<DoggoBinder?>()

fun BindingViewHolder<ViewholderDoggoListBinding>.createDoggoBinder(
    onThumbnailLoaded: (Doggo) -> Unit,
    onDoggoClicked: (Doggo) -> Unit
) = object : DoggoBinder {
    init {
        this@createDoggoBinder.itemView.setOnClickListener { doggo?.let(onDoggoClicked) }
    }

    override var doggo: Doggo? = null
    override val doggoName: TextView get() = binding.doggoName
    override val thumbnail: ImageView get() = binding.doggoImage
    override val fullResolution: ImageView? get() = null
    override fun onDoggoThumbnailLoaded(doggo: Doggo) = onThumbnailLoaded(doggo)
}