package com.tunjid.androidx.tablists.doggo

import android.os.Bundle
import android.text.TextUtils
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.ChangeImageTransform
import androidx.transition.ChangeTransform
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.tunjid.androidx.R
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.ViewholderDoggoRankBinding
import com.tunjid.androidx.divider
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.activityNavigatorController
import com.tunjid.androidx.recyclerview.*
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderDelegate
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.uidrivers.SpringItemAnimator
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.updatePartial
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.view.util.hashTransitionName
import com.tunjid.androidx.viewholders.DoggoBinder
import com.tunjid.androidx.viewholders.bind
import com.tunjid.androidx.viewmodels.routeName
import kotlin.math.abs

class DoggoRankFragment : Fragment(R.layout.fragment_doggo_list),
        Navigator.TransactionModifier {

    private var isRanking by fragmentArgs<Boolean>()
    private val viewModel by viewModels<DoggoRankViewModel>()
    private val navigator by activityNavigatorController<MultiStackNavigator>()

    private var recyclerView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarMenuRes = R.menu.menu_doggo,
                toolbarShows = true,
                toolbarOverlaps = false,
                toolbarMenuRefresher = {
                    it.findItem(R.id.menu_sort)?.isVisible = !isRanking
                    it.findItem(R.id.menu_browse)?.isVisible = isRanking
                },
                toolbarMenuClickListener = {
                    when (it.itemId) {
                        R.id.menu_browse -> isRanking = false
                        R.id.menu_sort -> isRanking = true
                    }
                    recyclerView?.notifyDataSetChanged()
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
                fabClickListener = { viewModel.resetList() }
        )

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            itemAnimator = SpringItemAnimator(stiffness = SpringForce.STIFFNESS_LOW)
            layoutManager = gridLayoutManager(2) { if (isRanking) 2 else 1 }
            adapter = adapterOf(
                    itemsSource = viewModel::doggos,
                    viewHolderCreator = { parent, _ -> rankingViewHolder(parent) },
                    viewHolderBinder = { viewHolder, doggo, _ -> viewHolder.bind(isRanking, doggo) },
                    itemIdFunction = { it.hashCode().toLong() }
            )
            addScrollListener { _, dy -> if (abs(dy) > 4) uiState = uiState.copy(fabExtended = dy < 0) }
            addItemDecoration(context.divider(DividerItemDecoration.HORIZONTAL))
            addItemDecoration(context.divider(DividerItemDecoration.VERTICAL))
            setSwipeDragOptions<BindingViewHolder<ViewholderDoggoRankBinding>>(
                    itemViewSwipeSupplier = { isRanking },
                    longPressDragSupplier = { isRanking },
                    swipeConsumer = { holder, _ -> removeDoggo(holder) },
                    dragConsumer = ::moveDoggo,
                    dragHandleFunction = { it.binding.innerConstraintLayout.dragHandle },
                    swipeDragStartConsumer = { holder, actionState -> onSwipeOrDragStarted(holder, actionState) },
                    swipeDragEndConsumer = { viewHolder, actionState -> onSwipeOrDragEnded(viewHolder, actionState) }
            )

            viewModel.watchDoggos().observe(viewLifecycleOwner, this::acceptDiff)
        }

        if (Doggo.transitionDoggo != null) postponeEnterTransition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        if (incomingFragment !is AdoptDoggoFragment) return

        val doggo = incomingFragment.doggo
        val holder: BindingViewHolder<ViewholderDoggoRankBinding> = recyclerView?.viewHolderForItemId(doggo.hashCode().toLong())
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

    private fun moveDoggo(start: BindingViewHolder<*>, end: BindingViewHolder<*>) {
        val from = start.adapterPosition
        val to = end.adapterPosition

        viewModel.swap(from, to)
        recyclerView?.notifyItemMoved(from, to)
        recyclerView?.notifyItemChanged(from)
        recyclerView?.notifyItemChanged(to)
    }

    private fun removeDoggo(viewHolder: BindingViewHolder<*>) {
        val position = viewHolder.adapterPosition
        val minMax = viewModel.remove(position)

        recyclerView?.notifyItemRemoved(position)
        // Only necessary to rebind views lower so they have the right position
        recyclerView?.notifyItemRangeChanged(minMax.first, minMax.second)
    }

    private fun onSwipeOrDragStarted(holder: BindingViewHolder<*>, actionState: Int) =
            viewModel.onActionStarted(Pair(holder.itemId, actionState))

    private fun onSwipeOrDragEnded(viewHolder: BindingViewHolder<*>, actionState: Int) {
        val message = viewModel.onActionEnded(Pair(viewHolder.itemId, actionState))
        if (!TextUtils.isEmpty(message)) uiState = uiState.copy(snackbarText = message)

    }

    companion object {
        fun newInstance(): DoggoRankFragment = DoggoRankFragment().apply { this.isRanking = true }
    }
}

var BindingViewHolder<ViewholderDoggoRankBinding>.doggoBinder by viewHolderDelegate<DoggoBinder?>()

private fun BindingViewHolder<ViewholderDoggoRankBinding>.bind(isRanking: Boolean, doggo: Doggo) {
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

    doggoBinder?.bind(doggo)
    binding.innerConstraintLayout.doggoRank.text = (adapterPosition + 1).toString()
}