package com.tunjid.androidx.tablists.doggo

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.app.SharedElementCallback
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tunjid.androidx.R
import com.tunjid.androidx.core.components.doOnEveryEvent
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.databinding.FragmentDoggoPagerBinding
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.activityNavigatorController
import com.tunjid.androidx.recyclerview.indicators.PageIndicator
import com.tunjid.androidx.recyclerview.indicators.Params
import com.tunjid.androidx.recyclerview.indicators.indicatorDecoration
import com.tunjid.androidx.recyclerview.indicators.start
import com.tunjid.androidx.recyclerview.indicators.width
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.baseSharedTransition
import com.tunjid.androidx.uidrivers.callback
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.updatePartial
import com.tunjid.androidx.view.util.hashTransitionName
import com.tunjid.viewpager2.FragmentListAdapter
import com.tunjid.viewpager2.FragmentTab
import kotlin.collections.set
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

class DoggoPagerFragment : Fragment(R.layout.fragment_doggo_pager),
    Navigator.TransactionModifier {

    private var isTopLevel by fragmentArgs<Boolean>()
    private val viewModel by viewModels<DoggoViewModel>()
    private val navigator by activityNavigatorController<MultiStackNavigator>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialUiState = uiState
        if (isTopLevel) uiState = UiState(
            toolbarOverlaps = true,
            toolbarShows = false,
            toolbarMenuRes = 0,
            fabIcon = R.drawable.ic_hug_24dp,
            fabShows = true,
            fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
            showsBottomNav = false,
            lightStatusBar = false,
            insetFlags = InsetFlags.NONE,
            navBarColor = Color.TRANSPARENT,
            fabClickListener = viewLifecycleOwner.callback {
                Doggo.transitionDoggo?.let { navigator.push(AdoptDoggoFragment.newInstance(it)) }
            }
        )
        else viewLifecycleOwner.lifecycle.doOnEveryEvent(Lifecycle.Event.ON_RESUME) {
            ::uiState.updatePartial {
                copy(
                    fabIcon = R.drawable.ic_hug_24dp,
                    fabShows = true,
                    fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
                    fabClickListener = viewLifecycleOwner.callback {
                        Doggo.transitionDoggo?.let { navigator.push(AdoptDoggoFragment.newInstance(it)) }
                    }
                )
            }
        }

        sharedElementEnterTransition = baseSharedTransition(initialUiState)
        sharedElementReturnTransition = baseSharedTransition(uiState)
        setEnterSharedElementCallback(createSharedEnterCallback())

        val binding = FragmentDoggoPagerBinding.bind(view)
        val resources = resources
        val context = view.context
        val indicatorSize = resources.getDimensionPixelSize(R.dimen.single_and_half_margin)

        val adapter = FragmentListAdapter<DoggoTab>(fragment = this)
        adapter.submitList(Doggo.doggos.map(::DoggoTab))

        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(Doggo.transitionIndex, false)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) =
                viewModel.onSwiped(position, positionOffset)

            override fun onPageSelected(position: Int) = onDoggoSwiped(position)
        })

        binding.viewPager.children.filterIsInstance<RecyclerView>().firstOrNull()?.indicatorDecoration(
            horizontalOffset = resources.displayMetrics.widthPixels / 2f,
            verticalOffset = resources.getDimension(R.dimen.octuple_margin),
            indicatorWidth = indicatorSize.toFloat(),
            indicatorHeight = indicatorSize.toFloat(),
            indicatorPadding = resources.getDimensionPixelSize(R.dimen.half_margin).toFloat(),
            indicator = DrawablePageIndicator(
                activeDrawable = context.drawableAt(R.drawable.ic_doggo_24dp)!!,
                inActiveDrawable = context.drawableAt(R.drawable.ic_circle_24dp)!!
            ),
            onIndicatorClicked = binding.viewPager::setCurrentItem
        )

        viewModel.colors.observe(viewLifecycleOwner, view::setBackgroundColor)

        onDoggoSwiped(binding.viewPager.currentItem)
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
        val (doggo, imageView) = transitionDoggoAndImageView() ?: return

        transaction
            .setReorderingAllowed(true)
            .addSharedElement(imageView, imageView.hashTransitionName(doggo))
    }

    private fun createSharedEnterCallback() = object : SharedElementCallback() {
        override fun onMapSharedElements(names: List<String>?, sharedElements: MutableMap<String, View>?) {
            if (names == null || sharedElements == null) return
            val (_, imageView) = transitionDoggoAndImageView() ?: return

            sharedElements[names[0]] = imageView
        }
    }

    private fun transitionDoggoAndImageView(): Pair<Doggo, ImageView>? {
        val root = view ?: return null
        val doggo = Doggo.transitionDoggo ?: return null
        val childRoot = root.findViewWithTag<View>(doggo) ?: return null
        return childRoot.findViewById<ImageView>(R.id.doggo_image)?.let { doggo to it }
    }

    companion object {
        fun newInstance(isTopLevel: Boolean): DoggoPagerFragment = DoggoPagerFragment().apply { this.isTopLevel = isTopLevel }
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

private data class DoggoTab(val doggo: Doggo) : FragmentTab {
    override fun title(res: Resources): CharSequence = ""

    override fun createFragment(): Fragment = DoggoFragment.newInstance(doggo)
}
