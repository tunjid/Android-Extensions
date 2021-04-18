package com.tunjid.androidx.tablists.doggo

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.transition.ChangeBounds
import androidx.transition.ChangeImageTransform
import androidx.transition.ChangeTransform
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.tunjid.androidx.R
import com.tunjid.androidx.core.components.doOnEveryEvent
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.core.delegates.viewLifecycle
import com.tunjid.androidx.databinding.FragmentDoggoListBinding
import com.tunjid.androidx.databinding.ViewholderDoggoRankBinding
import com.tunjid.androidx.divider
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.mapDistinct
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.activityNavigatorController
import com.tunjid.androidx.recyclerview.*
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderDelegate
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.callback
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.updatePartial
import com.tunjid.androidx.view.util.hashTransitionName
import kotlin.math.abs
import kotlinx.parcelize.Parcelize

@Parcelize
data class RankArgs(
    val isRanking: Boolean,
    val isTopLevel: Boolean,
) : Parcelable

class DoggoRankFragment : Fragment(R.layout.fragment_doggo_list),
    Navigator.TransactionModifier {

    private var args by fragmentArgs<RankArgs>()
    private var isRanking
        get() = args.isRanking
        set(value) {
            args = args.copy(isRanking = value)
        }

    private val viewModel by viewModels<DoggoRankViewModel>()
    private val navigator by activityNavigatorController<MultiStackNavigator>()

    private val binding by viewLifecycle(FragmentDoggoListBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.isTopLevel) uiState = UiState(
            toolbarTitle = this::class.java.routeName,
            toolbarMenuRes = R.menu.menu_doggo,
            toolbarShows = true,
            toolbarOverlaps = false,
            toolbarMenuRefresher = viewLifecycleOwner.callback {
                it.findItem(R.id.menu_sort)?.isVisible = !isRanking
                it.findItem(R.id.menu_browse)?.isVisible = isRanking
            },
            toolbarMenuClickListener = viewLifecycleOwner.callback {
                when (it.itemId) {
                    R.id.menu_browse -> isRanking = false
                    R.id.menu_sort -> isRanking = true
                }
                binding.recyclerView.notifyDataSetChanged()
                ::uiState.updatePartial { copy(toolbarInvalidated = true) }
            },
            fabText = getString(R.string.reset_doggos),
            fabIcon = R.drawable.ic_restore_24dp,
            fabShows = true,
            showsBottomNav = true,
            insetFlags = InsetFlags.ALL,
            lightStatusBar = !requireContext().isDarkTheme,
            fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
            fabClickListener = viewLifecycleOwner.callback { viewModel.accept(RankAction.Reset) }
        )
        else viewLifecycleOwner.lifecycle.doOnEveryEvent(Lifecycle.Event.ON_RESUME) {
            ::uiState.updatePartial {
                copy(
                    fabText = getString(R.string.reset_doggos),
                    fabIcon = R.drawable.ic_restore_24dp,
                    fabShows = true,
                    fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
                    fabClickListener = viewLifecycleOwner.callback { viewModel.accept(RankAction.Reset) }
                )
            }
        }

        val listAdapter = listAdapterOf(
            initialItems = viewModel.state.value?.doggos ?: listOf(),
            viewHolderCreator = { parent, _ -> rankingViewHolder(parent) },
            viewHolderBinder = { viewHolder, indexedDoggo, _ -> viewHolder.bind(isRanking, indexedDoggo) },
            itemIdFunction = { it.diffId.hashCode().toLong() }
        )

        binding.recyclerView.apply {
            layoutManager = gridLayoutManager(spanCount = 2) { if (isRanking) 2 else 1 }
            adapter = listAdapter

            addScrollListener { _, dy -> if (abs(dy) > 4) uiState = uiState.copy(fabExtended = dy < 0) }
            addItemDecoration(context.divider(DividerItemDecoration.HORIZONTAL))
            addItemDecoration(context.divider(DividerItemDecoration.VERTICAL))
            setSwipeDragOptions(
                itemViewSwipeSupplier = { isRanking && args.isTopLevel },
                longPressDragSupplier = { isRanking },
                swipeConsumer = { holder, _ -> removeDoggo(holder) },
                dragConsumer = ::moveDoggo,
                dragHandleFunction = { it.binding.innerConstraintLayout.dragHandle },
                swipeDragStartConsumer = { holder, actionState -> onSwipeOrDragStarted(holder, actionState) },
                swipeDragEndConsumer = { viewHolder, actionState -> onSwipeOrDragEnded(viewHolder, actionState) }
            )
        }

        viewModel.state.apply {
            mapDistinct(RankState::doggos)
                .observe(viewLifecycleOwner, listAdapter::submitList)
            mapDistinct { it.viewHash to it.text }
                .observe(viewLifecycleOwner) { (viewId, text) ->
                    if (text != null && viewId == System.identityHashCode(view))
                        ::uiState.updatePartial { copy(snackbarText = text) }
                }
        }

        if (Doggo.transitionDoggo != null) postponeEnterTransition()
    }

    override fun onResume() {
        super.onResume()
        viewModel.accept(RankAction.ViewHash(System.identityHashCode(binding.root)))

    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        if (incomingFragment !is AdoptDoggoFragment) return

        val doggo = incomingFragment.doggo
        val holder: BindingViewHolder<ViewholderDoggoRankBinding> = binding.recyclerView.viewHolderForItemId(doggo.diffId.hashCode().toLong())
            ?: return

        val binding = holder.binding
        transaction
            .setReorderingAllowed(true)
            .addSharedElement(
                binding.innerConstraintLayout.doggoImage,
                binding.innerConstraintLayout.doggoImage.hashTransitionName(doggo)
            )
    }

    private fun rankingViewHolder(
        parent: ViewGroup
    ) = parent.viewHolderFrom(ViewholderDoggoRankBinding::inflate).apply {
        doggoBinder = object : DoggoBinder {
            init {
                itemView.setOnClickListener {
                    val doggo = doggo ?: return@setOnClickListener
                    Doggo.transitionDoggo = doggo
                    navigator.push(AdoptDoggoFragment.newInstance(doggo))
                }
            }

            override var doggo: Doggo? = null
            override val doggoName: TextView get() = binding.innerConstraintLayout.doggoName
            override val thumbnail: ImageView get() = binding.innerConstraintLayout.doggoImage
            override val fullResolution: ImageView? get() = null
            override fun onDoggoThumbnailLoaded(doggo: Doggo) {
                if (doggo == Doggo.transitionDoggo) startPostponedEnterTransition()
            }
        }
    }

    private fun moveDoggo(start: BindingViewHolder<ViewholderDoggoRankBinding>, end: BindingViewHolder<ViewholderDoggoRankBinding>) {
        val from = start.doggoBinder.doggo ?: return
        val to = end.doggoBinder.doggo ?: return

        viewModel.accept(RankAction.Edit.Swap(from, to))
    }

    private fun removeDoggo(viewHolder: BindingViewHolder<ViewholderDoggoRankBinding>) {
        viewHolder.doggoBinder.doggo
            ?.let(RankAction.Edit::Remove)
            ?.let(viewModel::accept)
    }

    private fun onSwipeOrDragStarted(holder: BindingViewHolder<ViewholderDoggoRankBinding>, actionState: Int) = holder.doggoBinder.doggo?.let {
        viewModel.accept(RankAction.SwipeDragStarted(it, actionState))
    }

    private fun onSwipeOrDragEnded(viewHolder: BindingViewHolder<ViewholderDoggoRankBinding>, actionState: Int) = viewHolder.doggoBinder.doggo?.let {
        viewModel.accept(RankAction.SwipeDragEnded(it, actionState))
    }

    companion object {
        fun newInstance(args: RankArgs): DoggoRankFragment = DoggoRankFragment().apply { this.args = args }
    }
}

var BindingViewHolder<ViewholderDoggoRankBinding>.doggoBinder by viewHolderDelegate<DoggoBinder>()

private fun BindingViewHolder<ViewholderDoggoRankBinding>.bind(isRanking: Boolean, indexedDoggo: IndexedDoggo) {
    doggoBinder.bind(indexedDoggo.doggo)

    val layoutParams = binding.innerConstraintLayout.doggoImage.layoutParams as? ConstraintLayout.LayoutParams
        ?: return
    val currentlyInRanking = layoutParams.matchConstraintPercentWidth != 1f
    val context = binding.root.context

    if (isRanking != currentlyInRanking) ConstraintSet().run {
        TransitionManager.beginDelayedTransition(
            binding.itemContainer,
            TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .addTransition(TransitionSet()
                    .setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .addTransition(ChangeImageTransform())
                    .addTransition(ChangeTransform())
                    .addTransition(ChangeBounds())
                    .addTarget(binding.innerConstraintLayout.doggoImage)
                )
                .addTransition(ChangeBounds()
                    .addTarget(binding.innerConstraintLayout.innerConstraintLayout)
                    .addTarget(binding.innerConstraintLayout.doggoName)
                    .addTarget(binding.innerConstraintLayout.doggoRank)
                )
                .setDuration(250)
        )
        clone(context, if (isRanking) R.layout.viewholder_doggo_rank_sort else R.layout.viewholder_doggo_rank_browse)
        applyTo(binding.innerConstraintLayout.innerConstraintLayout)
    }

    val third = context.resources.getDimensionPixelSize(R.dimen.third_margin)
    val single = context.resources.getDimensionPixelSize(R.dimen.single_margin)

    binding.itemContainer.apply {
        if (isRanking) updateLayoutParams<ViewGroup.MarginLayoutParams> { leftMargin = single; topMargin = third; rightMargin = single; bottomMargin = third }
        else updateLayoutParams<ViewGroup.MarginLayoutParams> { leftMargin = 0; topMargin = 0; rightMargin = 0; bottomMargin = 0 }
    }

    binding.innerConstraintLayout.doggoRank.text = (adapterPosition + 1).toString()
}