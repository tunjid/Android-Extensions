package com.tunjid.androidx.fragments

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.SharedElementCallback
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.view.doOnLayout
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.tunjid.androidx.R
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.activityNavigatorController
import com.tunjid.androidx.recyclerview.indicators.PageIndicator
import com.tunjid.androidx.recyclerview.indicators.Params
import com.tunjid.androidx.recyclerview.indicators.indicatorDecoration
import com.tunjid.androidx.recyclerview.indicators.start
import com.tunjid.androidx.recyclerview.indicators.width
import com.tunjid.androidx.uidrivers.activityGlobalUiController
import com.tunjid.androidx.uidrivers.baseSharedTransition
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.hashTransitionName
import com.tunjid.androidx.viewmodels.DoggoViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

class DoggoPagerFragment : Fragment(R.layout.fragment_doggo_pager),
        Navigator.TransactionModifier {

    private var uiState by activityGlobalUiController()
    private val viewModel by viewModels<DoggoViewModel>()
    private val navigator by activityNavigatorController<MultiStackNavigator>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
                if (f == this@DoggoPagerFragment) Log.i("TEST", "Started")
            }

            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                if (f == this@DoggoPagerFragment) Log.i("TEST", "callback View created. View identity hash: ${System.identityHashCode(view)}")
            }

            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                if (f == this@DoggoPagerFragment) Log.i("TEST", "Resumed")
            }

            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                if (f == this@DoggoPagerFragment) fm.unregisterFragmentLifecycleCallbacks(this)
            }
        }, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("TEST", "override onViewCreated. View identity hash: ${System.identityHashCode(view)}")

        val initialUiState = uiState
        uiState = uiState.copy(
                toolbarOverlaps = true,
                toolbarShows = false,
                toolBarMenu = 0,
                fabIcon = R.drawable.ic_hug_24dp,
                fabShows = true,
                fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
                showsBottomNav = false,
                lightStatusBar = false,
                insetFlags = InsetFlags.NONE,
                navBarColor = Color.TRANSPARENT,
                fabClickListener = { Doggo.transitionDoggo?.let { navigator.push(AdoptDoggoFragment.newInstance(it)) } }
        )

        sharedElementEnterTransition = baseSharedTransition(initialUiState)
        sharedElementReturnTransition = baseSharedTransition(uiState)
        setEnterSharedElementCallback(createSharedEnterCallback())

        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        val resources = resources
        val context = view.context
        val indicatorSize = resources.getDimensionPixelSize(R.dimen.single_and_half_margin)

        viewPager.adapter = DoggoPagerAdapter(viewModel.doggos, this)
        viewPager.setCurrentItem(Doggo.transitionIndex, false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) =
                    viewModel.onSwiped(position, positionOffset)

            override fun onPageSelected(position: Int) = onDoggoSwiped(position)
        })

        val recyclerView = viewPager[0] as RecyclerView
        recyclerView.indicatorDecoration(
                horizontalOffset = resources.displayMetrics.widthPixels / 2f,
                verticalOffset = resources.getDimension(R.dimen.octuple_margin),
                indicatorWidth = indicatorSize.toFloat(),
                indicatorHeight = indicatorSize.toFloat(),
                indicatorPadding = resources.getDimensionPixelSize(R.dimen.half_margin).toFloat(),
                indicator = DrawablePageIndicator(
                        activeDrawable = context.drawableAt(R.drawable.ic_doggo_24dp)!!,
                        inActiveDrawable = context.drawableAt(R.drawable.ic_circle_24dp)!!
                )
        ) {
            viewPager.setCurrentItem(it); Log.i("TEST", "view instance: ${System.identityHashCode(view)}")
        }

        viewModel.colors.observe(viewLifecycleOwner, view::setBackgroundColor)

        onDoggoSwiped(viewPager.currentItem)
        postponeEnterTransition()
    }

    private fun onDoggoSwiped(position: Int) {
        Doggo.doggos[position].apply {
            Doggo.transitionDoggo = this
            uiState = uiState.copy(fabText = getString(R.string.adopt_doggo, name.replace(" ", "")))
        }
    }

    override fun onDestroyView() {
        Log.i("TEST", "Destroyed view of identity hash: ${System.identityHashCode(view)}")
        super.onDestroyView()
    }


    override fun startPostponedEnterTransition() {
        Log.i("TEST", "Starting postponed")

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

    private fun createSharedEnterCallback() = object : SharedElementCallback() {
        override fun onMapSharedElements(names: List<String>?, sharedElements: MutableMap<String, View>?) {
            val recyclerView = view?.findViewById<ViewGroup>(R.id.view_pager)?.get(0) ?: return
            if (names == null || sharedElements == null || recyclerView !is RecyclerView) return

            val viewHolder = Doggo.transitionDoggo
                    ?.let { recyclerView.findViewHolderForItemId(it.hashCode().toLong()) }
                    ?: return

            val view: View = viewHolder.itemView.findViewById(R.id.doggo_image) ?: return

            sharedElements[names[0]] = view
        }
    }

    companion object {
        fun newInstance(): DoggoPagerFragment = DoggoPagerFragment().apply { arguments = Bundle() }
    }

}

private class DrawablePageIndicator(
        activeDrawable: Drawable,
        inActiveDrawable: Drawable
) : PageIndicator {

    private val active = activeDrawable.toBitmap()
    private val inActive = inActiveDrawable.toBitmap()

    override fun drawInActive(
            canvas: Canvas,
            params: Params,
            index: Int,
            count: Int,
            progress: Float
    ) {
        val start = params.start(count)
        for (i in 0 until count) {
            canvas.drawBitmap(inActive, start + (params.width * i), params.verticalOffset, null)
        }
    }

    override fun drawActive(
            canvas: Canvas,
            params: Params,
            index: Int,
            count: Int,
            progress: Float
    ) = canvas.drawBitmap(
            active.scale(params.indicatorWidth, progress),
            params.start(count) + (params.width * index) + (params.width * progress),
            params.verticalOffset.bounce(params.indicatorHeight, progress),
            null
    )

    private fun Float.bounce(height: Float, progress: Float): Float {
        val radians = Math.PI * progress
        val sine = (-sin(radians)).toFloat()
        return this + (height * 0.5f * sine)
    }

    private fun Bitmap.scale(size: Float, progress: Float): Bitmap {
        val radians = Math.PI * progress
        val cosine = cos(radians).toFloat()
        val maxScale = max(abs(cosine), 0.8f)

        val scaled = (size * maxScale).roundToInt()
        return scale(scaled, scaled)
    }
}

private class DoggoPagerAdapter(private val doggos: List<Doggo>, fragment: Fragment)
    : FragmentStateAdapter(fragment.childFragmentManager, fragment.viewLifecycleOwner.lifecycle) {

    override fun getItemCount(): Int = this.doggos.size

    override fun getItemId(position: Int): Long = doggos[position].hashCode().toLong()

    override fun containsItem(itemId: Long): Boolean = doggos.map(Doggo::hashCode).contains(itemId.toInt())

    override fun createFragment(position: Int): Fragment = DoggoFragment.newInstance(doggos[position])
}