package com.tunjid.androidx.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Pair
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.databinding.ViewholderDoggoRankBinding
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.recyclerview.*
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.hashTransitionName
import com.tunjid.androidx.viewholders.DoggoBinder
import com.tunjid.androidx.viewholders.bind
import com.tunjid.androidx.viewmodels.DoggoRankViewModel
import com.tunjid.androidx.viewmodels.routeName
import kotlin.math.abs

class DoggoRankFragment : AppBaseFragment(R.layout.fragment_simple_list),
        Navigator.TransactionModifier {

    private val viewModel by viewModels<DoggoRankViewModel>()

    private var recyclerView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolBarMenu = 0,
                toolbarShows = true,
                toolbarOverlaps = false,
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
            layoutManager = verticalLayoutManager()
            adapter = adapterOf(
                    itemsSource = viewModel::doggos,
                    viewHolderCreator = { parent, _ ->
                        parent.viewHolderFrom(ViewholderDoggoRankBinding::inflate).apply {
                            doggoBinder = createDoggoBinder(
                                    onThumbnailLoaded = { if (it == Doggo.transitionDoggo) startPostponedEnterTransition() },
                                    onDoggoClicked = {
                                        Doggo.transitionDoggo = it
                                        navigator.push(AdoptDoggoFragment.newInstance(it))
                                    }
                            )
                        }
                    },
                    viewHolderBinder = { viewHolder, doggo, _ ->
                        viewHolder.doggoBinder?.bind(doggo)
                        viewHolder.binding.doggoRank.text = (viewHolder.adapterPosition + 1).toString()
                    },
                    itemIdFunction = { it.hashCode().toLong() }
            )
            addScrollListener { _, dy -> if (abs(dy) > 4) uiState = uiState.copy(fabExtended = dy < 0) }
            setSwipeDragOptions<BindingViewHolder<ViewholderDoggoRankBinding>>(
                    itemViewSwipeSupplier = { true },
                    longPressDragSupplier = { true },
                    swipeConsumer = { holder, _ -> removeDoggo(holder) },
                    dragConsumer = ::moveDoggo,
                    dragHandleFunction = { it.binding.dragHandle },
                    swipeDragStartConsumer = { holder, actionState -> onSwipeOrDragStarted(holder, actionState) },
                    swipeDragEndConsumer = { viewHolder, actionState -> onSwipeOrDragEnded(viewHolder, actionState) }
            )

            viewModel.watchDoggos().observe(viewLifecycleOwner, this::acceptDiff)
        }

        postponeEnterTransition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
    }

    @SuppressLint("CommitTransaction")
    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        if (incomingFragment !is AdoptDoggoFragment) return

        val doggo = incomingFragment.doggo
        val holder: BindingViewHolder<ViewholderDoggoRankBinding> = recyclerView?.viewHolderForItemId(doggo.hashCode().toLong())
                ?: return

        val binding = holder.binding
        transaction
                .setReorderingAllowed(true)
                .addSharedElement(binding.doggoImage, binding.doggoImage.hashTransitionName(doggo))
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
        fun newInstance(): DoggoRankFragment = DoggoRankFragment().apply { arguments = Bundle() }
    }
}

var BindingViewHolder<ViewholderDoggoRankBinding>.doggoBinder by BindingViewHolder.Prop<DoggoBinder?>()

fun BindingViewHolder<ViewholderDoggoRankBinding>.createDoggoBinder(
        onThumbnailLoaded: (Doggo) -> Unit,
        onDoggoClicked: (Doggo) -> Unit
) = object : DoggoBinder {
    init {
        itemView.setOnClickListener { doggo?.let(onDoggoClicked) }
    }

    override var doggo: Doggo? = null
    override val doggoName: TextView get() = binding.doggoName
    override val thumbnail: ImageView get() = binding.doggoImage
    override val fullResolution: ImageView? get() = null
    override fun onDoggoThumbnailLoaded(doggo: Doggo) = onThumbnailLoaded(doggo)
}