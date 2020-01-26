package com.tunjid.androidx.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Pair
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.DoggoInteractionListener
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.recyclerview.*
import com.tunjid.androidx.uidrivers.InsetLifecycleCallbacks
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.hashTransitionName
import com.tunjid.androidx.view.util.inflate
import com.tunjid.androidx.viewholders.DoggoRankViewHolder
import com.tunjid.androidx.viewholders.DoggoViewHolder
import com.tunjid.androidx.viewmodels.DoggoRankViewModel
import com.tunjid.androidx.viewmodels.routeName
import kotlin.math.abs

class DoggoRankFragment : AppBaseFragment(R.layout.fragment_simple_list),
        Navigator.TransactionModifier,
        DoggoInteractionListener {

    override val insetFlags: InsetFlags = InsetFlags.ALL

    private val viewModel by viewModels<DoggoRankViewModel>()

    private var recyclerView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolBarMenu = 0,
                toolbarShows = true,
                fabText = getString(R.string.reset_doggos),
                fabIcon = R.drawable.ic_restore_24dp,
                fabShows = true,
                showsBottomNav = true,
                lightStatusBar = !requireContext().isDarkTheme,
                fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
                fabClickListener = View.OnClickListener { viewModel.resetList() }
        )

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            updatePadding(bottom = InsetLifecycleCallbacks.bottomInset)

            layoutManager = verticalLayoutManager()
            adapter = adapterOf(
                    itemsSource = viewModel::doggos,
                    viewHolderCreator = { parent, _ -> DoggoRankViewHolder(parent.inflate(R.layout.viewholder_doggo_rank), this@DoggoRankFragment) },
                    viewHolderBinder = { viewHolder, doggo, _ -> viewHolder.bind(doggo) },
                    itemIdFunction = { it.hashCode().toLong() }
            )
            addScrollListener { _, dy -> if (abs(dy) > 4) uiState = uiState.copy(fabExtended = dy < 0) }
            setSwipeDragOptions(
                    itemViewSwipeSupplier = { true },
                    longPressDragSupplier = { true },
                    swipeConsumer = { holder, _ -> removeDoggo(holder) },
                    dragConsumer = ::moveDoggo,
                    dragHandleFunction = DoggoRankViewHolder::dragView,
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

    override fun onDoggoClicked(doggo: Doggo) {
        Doggo.transitionDoggo = doggo
        navigator.push(AdoptDoggoFragment.newInstance(doggo))
    }

    override fun onDoggoImageLoaded(doggo: Doggo) {
        if (doggo == Doggo.transitionDoggo) startPostponedEnterTransition()
    }

    @SuppressLint("CommitTransaction")
    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        if (incomingFragment !is AdoptDoggoFragment) return

        val doggo = incomingFragment.doggo
        val holder = recyclerView?.viewHolderForItemId<DoggoRankViewHolder>(doggo.hashCode().toLong())
                ?: return

        transaction.addSharedElement(holder.thumbnail, holder.thumbnail.hashTransitionName(doggo))
    }

    private fun moveDoggo(start: DoggoViewHolder, end: DoggoViewHolder) {
        val from = start.adapterPosition
        val to = end.adapterPosition

        viewModel.swap(from, to)
        recyclerView?.notifyItemMoved(from, to)
        recyclerView?.notifyItemChanged(from)
        recyclerView?.notifyItemChanged(to)
    }

    private fun removeDoggo(viewHolder: DoggoViewHolder) {
        val position = viewHolder.adapterPosition
        val minMax = viewModel.remove(position)

        recyclerView?.notifyItemRemoved(position)
        // Only necessary to rebind views lower so they have the right position
        recyclerView?.notifyItemRangeChanged(minMax.first, minMax.second)
    }

    private fun onSwipeOrDragStarted(holder: DoggoRankViewHolder, actionState: Int) =
            viewModel.onActionStarted(Pair(holder.itemId, actionState))

    private fun onSwipeOrDragEnded(viewHolder: DoggoViewHolder, actionState: Int) {
        val message = viewModel.onActionEnded(Pair(viewHolder.itemId, actionState))
        if (!TextUtils.isEmpty(message)) uiState = uiState.copy(snackbarText = message)

    }

    companion object {
        fun newInstance(): DoggoRankFragment = DoggoRankFragment().apply { arguments = Bundle() }
    }
}
