package com.tunjid.androidbootstrap.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.tunjid.androidbootstrap.GlobalUiController
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.UiState
import com.tunjid.androidbootstrap.activityGlobalUiController
import com.tunjid.androidbootstrap.adapters.DoggoPagerAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.constraintlayout.animator.ViewPagerIndicatorAnimator
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.view.util.InsetFlags
import com.tunjid.androidbootstrap.view.util.ViewUtil
import com.tunjid.androidbootstrap.viewmodels.DoggoViewModel
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class DoggoPagerFragment : AppBaseFragment(), GlobalUiController {

    override var uiState: UiState by activityGlobalUiController()

    private val viewModel by viewModels<DoggoViewModel>()

    override val insetFlags: InsetFlags = InsetFlags.NONE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        uiState = uiState.copy(
                showsToolbar = false,
                fabIcon = R.drawable.ic_hug_24dp,
                showsFab = true,
                fabExtended = !restoredFromBackStack(),
                navBarColor = Color.TRANSPARENT,
                fabClickListener = View.OnClickListener { Doggo.getTransitionDoggo()?.let { showFragment(AdoptDoggoFragment.newInstance(it)) } }
        )

        val root = inflater.inflate(R.layout.fragment_doggo_pager, container, false)
        val viewPager = root.findViewById<ViewPager>(R.id.view_pager)
        val resources = resources
        val indicatorSize = resources.getDimensionPixelSize(R.dimen.single_and_half_margin)

        viewPager.adapter = DoggoPagerAdapter(viewModel.doggos, childFragmentManager)
        viewPager.currentItem = Doggo.transitionIndex
        viewPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) = onDoggoSwiped(position)
        })

        val indicatorAnimator = ViewPagerIndicatorAnimator.builder()
                .setIndicatorWidth(indicatorSize)
                .setIndicatorHeight(indicatorSize)
                .setIndicatorPadding(resources.getDimensionPixelSize(R.dimen.half_margin))
                .setInActiveDrawable(R.drawable.ic_circle_24dp)
                .setActiveDrawable(R.drawable.ic_doggo_24dp)
                .setGuideLine(root.findViewById(R.id.guide))
                .setContainer(root as ConstraintLayout)
                .setViewPager(viewPager)
                .build()

        indicatorAnimator.addIndicatorWatcher { indicator, position, fraction, _ ->
            val radians = Math.PI * fraction
            val sine = (-sin(radians)).toFloat()
            val cosine = cos(radians).toFloat()
            val maxScale = max(abs(cosine), 0.4f)

            val currentIndicator = indicatorAnimator.getIndicatorAt(position)
            currentIndicator.scaleX = maxScale
            currentIndicator.scaleY = maxScale
            indicator.translationY = indicatorSize * sine
        }

        indicatorAnimator.addIndicatorWatcher watcher@{ indicator, position, fraction, _ ->
            if (fraction == 0F) return@watcher

            val static = intArrayOf(0, 0).apply { indicatorAnimator.getIndicatorAt(position).getLocationInWindow(this) }
            val dynamic = intArrayOf(0, 0).apply { indicator.getLocationInWindow(this) }
            val toTheRight = dynamic[0] > static[0]

            viewModel.onSwiped(position, fraction, toTheRight)
        }

        prepareSharedElementTransition()

        if (savedInstanceState == null) postponeEnterTransition()

        return root
    }

    override fun onResume() {
        super.onResume()
        val root = view ?: return
        disposables.add(viewModel.getColors(Color.TRANSPARENT).subscribe(root::setBackgroundColor, Throwable::printStackTrace))
    }

    private fun onDoggoSwiped(position: Int) {
        Doggo.doggos[position].apply {
            Doggo.setTransitionDoggo(this)
            uiState = uiState.copy(fabText = getString(R.string.adopt_doggo, name.replace(" ", "")))
        }
    }

    @SuppressLint("CommitTransaction")
    override fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? {
        if (!fragmentTo.stableTag.contains(AdoptDoggoFragment::class.java.simpleName)) return null

        val root = view ?: return null

        val doggo = Doggo.getTransitionDoggo() ?: return null

        val childRoot = root.findViewWithTag<View>(doggo) ?: return null

        val imageView = childRoot.findViewById<ImageView>(R.id.doggo_image) ?: return null

        return requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .setReorderingAllowed(true)
                .addSharedElement(imageView, ViewUtil.transitionName(doggo, imageView))
    }

    private fun prepareSharedElementTransition() {
        val baseSharedTransition = baseSharedTransition()

        sharedElementEnterTransition = baseSharedTransition
        sharedElementReturnTransition = baseSharedTransition

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>?, sharedElements: MutableMap<String, View>?) {
                val viewPager = view?.findViewById<ViewPager>(R.id.view_pager) ?: return
                if (names == null || sharedElements == null || view == null) return

                val currentFragment = Objects.requireNonNull<PagerAdapter>(viewPager.adapter)
                        .instantiateItem(viewPager, Doggo.transitionIndex) as Fragment
                val view = currentFragment.view ?: return

                sharedElements[names[0]] = view.findViewById(R.id.doggo_image)
            }
        })
    }

    companion object {
        fun newInstance(): DoggoPagerFragment = DoggoPagerFragment().apply { arguments = Bundle(); prepareSharedElementTransition() }
    }

}
