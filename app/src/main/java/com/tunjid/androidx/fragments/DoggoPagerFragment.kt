package com.tunjid.androidx.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.SharedElementCallback
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.doOnLayout
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.DoggoPagerAdapter
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.recyclerview.indicators.BitmapPageIndicator
import com.tunjid.androidx.recyclerview.indicators.IndicatorDecoration
import com.tunjid.androidx.uidrivers.baseSharedTransition
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.hashTransitionName
import com.tunjid.androidx.viewmodels.DoggoViewModel

class DoggoPagerFragment : AppBaseFragment(R.layout.fragment_doggo_pager),
        Navigator.TransactionModifier {

    override val insetFlags: InsetFlags = InsetFlags.NONE

    private val viewModel by viewModels<DoggoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getColors(Color.TRANSPARENT).observe(this) { view?.setBackgroundColor(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarShows = false,
                toolBarMenu = 0,
                fabIcon = R.drawable.ic_hug_24dp,
                fabShows = true,
                fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
                showsBottomNav = false,
                lightStatusBar = false,
                navBarColor = Color.TRANSPARENT,
                fabClickListener = View.OnClickListener { Doggo.transitionDoggo?.let { navigator.push(AdoptDoggoFragment.newInstance(it)) } }
        )

        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        val resources = resources
        val context = view.context
        val indicatorSize = resources.getDimensionPixelSize(R.dimen.single_and_half_margin)

        viewPager.adapter = DoggoPagerAdapter(viewModel.doggos, this)
        viewPager.setCurrentItem(Doggo.transitionIndex, false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            var current = viewPager.currentItem

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (positionOffset != 0f) viewModel.onSwiped(current, positionOffset, position == current)
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) current = viewPager.currentItem
            }

            override fun onPageSelected(position: Int) = onDoggoSwiped(position)
        })

        viewPager.addItemDecoration(IndicatorDecoration(
                verticalOffset = resources.getDimension(R.dimen.octuple_margin),
                indicatorWidth = indicatorSize.toFloat(),
                indicatorHeight = indicatorSize.toFloat(),
                indicatorPadding = resources.getDimensionPixelSize(R.dimen.half_margin).toFloat(),
                indicator = BitmapPageIndicator(
                        active = context.drawableAt(R.drawable.ic_doggo_24dp)!!.toBitmap(),
                        inActive = context.drawableAt(R.drawable.ic_circle_24dp)!!.toBitmap()
                )
        ))

//        indicatorAnimator.addIndicatorWatcher { indicator, position, fraction, _ ->
//            val radians = Math.PI * fraction
//            val sine = (-sin(radians)).toFloat()
//            val cosine = cos(radians).toFloat()
//            val maxScale = max(abs(cosine), 0.4f)
//
//            val currentIndicator = indicatorAnimator.getIndicatorAt(position)
//            currentIndicator.scaleX = maxScale
//            currentIndicator.scaleY = maxScale
//            indicator.translationY = indicatorSize * sine
//        }

        onDoggoSwiped(viewPager.currentItem)
        prepareSharedElementTransition()

        postponeEnterTransition()
    }

    private fun onDoggoSwiped(position: Int) {
        Doggo.doggos[position].apply {
            Doggo.transitionDoggo = this
            uiState = uiState.copy(fabText = getString(R.string.adopt_doggo, name.replace(" ", "")))
        }
    }

    override fun startPostponedEnterTransition() {
        // ViewPager2 likes to take it's time
        view?.doOnLayout { super.startPostponedEnterTransition() }
    }

    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        if (incomingFragment !is AdoptDoggoFragment) return

        val root = view ?: return
        val doggo = Doggo.transitionDoggo ?: return
        val childRoot = root.findViewWithTag<View>(doggo) ?: return
        val imageView = childRoot.findViewById<ImageView>(R.id.doggo_image) ?: return

        transaction
                .setReorderingAllowed(true)
                .addSharedElement(imageView, imageView.hashTransitionName(doggo))
    }

    private fun prepareSharedElementTransition() {
        sharedElementEnterTransition = baseSharedTransition()
        sharedElementReturnTransition = baseSharedTransition()

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>?, sharedElements: MutableMap<String, View>?) {
                val recyclerView = view?.findViewById<ViewGroup>(R.id.view_pager)?.get(0) ?: return
                if (names == null || sharedElements == null || recyclerView !is RecyclerView) return

                val viewHolder = Doggo.transitionDoggo
                        ?.let { recyclerView.findViewHolderForItemId(it.hashCode().toLong()) }
                        ?: return

                val view: View = viewHolder.itemView.findViewById(R.id.doggo_image) ?: return

                sharedElements[names[0]] = view
            }
        })
    }

    companion object {
        fun newInstance(): DoggoPagerFragment = DoggoPagerFragment().apply { arguments = Bundle(); prepareSharedElementTransition() }
    }

}
