package com.tunjid.androidbootstrap.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.DoggoPagerAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.constraintlayout.animator.ViewPagerIndicatorAnimator
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator.GlyphState
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator.newState
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.view.util.InsetFlags
import com.tunjid.androidbootstrap.view.util.ViewUtil
import java.util.*

class DoggoPagerFragment : AppBaseFragment() {

    companion object {
        fun newInstance(): DoggoPagerFragment {
            val fragment = DoggoPagerFragment()
            fragment.arguments = Bundle()
            fragment.prepareSharedElementTransition()
            return fragment
        }
    }

    public override val fabState: GlyphState
        get() = newState(dogName, ContextCompat.getDrawable(requireContext(), R.drawable.ic_hug_24dp))

    override val fabClickListener: View.OnClickListener
        get() = View.OnClickListener { Doggo.getTransitionDoggo()?.let { showFragment(AdoptDoggoFragment.newInstance(it)) } }

    private val dogName: String
        get() {
            val doggo = Doggo.getTransitionDoggo() ?: return ""
            val name = doggo.name.replace(" ", "")
            return getString(R.string.adopt_doggo, name)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_doggo_pager, container, false)
        val viewPager = root.findViewById<ViewPager>(R.id.view_pager)
        val resources = resources
        val indicatorSize = resources.getDimensionPixelSize(R.dimen.single_and_half_margin)

        viewPager.adapter = DoggoPagerAdapter(Doggo.doggos, childFragmentManager)
        viewPager.currentItem = Doggo.transitionIndex
        viewPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                onDoggoSwiped(position)
            }
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
            val sine = (-Math.sin(radians)).toFloat()
            val cosine = Math.cos(radians).toFloat()
            val maxScale = Math.max(Math.abs(cosine), 0.4f)

            val currentIndicator = indicatorAnimator.getIndicatorAt(position)
            currentIndicator.scaleX = maxScale
            currentIndicator.scaleY = maxScale
            indicator.translationY = indicatorSize * sine
        }

        prepareSharedElementTransition()
        tintView<View>(R.color.black, root, { color, view -> view.setBackgroundColor(color) })
        if (savedInstanceState == null) postponeEnterTransition()
        return root
    }

    private fun onDoggoSwiped(position: Int) {
        Doggo.setTransitionDoggo(Doggo.doggos[position])
        togglePersistentUi()
    }

    override fun insetFlags(): InsetFlags {
        return InsetFlags.NONE
    }

    override fun showsToolBar(): Boolean {
        return false
    }

    override fun showsFab(): Boolean {
        return true
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
        val baseTransition = baseTransition()
        val baseSharedTransition = baseSharedTransition()

        enterTransition = baseTransition
        exitTransition = baseTransition
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

}
