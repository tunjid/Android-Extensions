package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.view.animator.ViewHider

/**
 * Fragment demonstrating hiding views
 *
 *
 * Created by tj.dahunsi on 5/6/17.
 */

class HidingViewFragment : AppBaseFragment() {

    companion object {
        fun newInstance(): HidingViewFragment {
            val fragment = HidingViewFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var leftHider: ViewHider
    private lateinit var topHider: ViewHider
    private lateinit var rightHider: ViewHider
    private lateinit var bottomHider: ViewHider

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_hiding_view, container, false)

        val leftButton = rootView.findViewById<View>(R.id.left_button)
        val topButton = rootView.findViewById<View>(R.id.top_button)
        val rightButton = rootView.findViewById<View>(R.id.right_button)
        val bottomButton = rootView.findViewById<View>(R.id.bottom_button)

        leftButton.setOnClickListener(this::onClick)
        topButton.setOnClickListener(this::onClick)
        rightButton.setOnClickListener(this::onClick)
        bottomButton.setOnClickListener(this::onClick)
        rootView.findViewById<View>(R.id.reset).setOnClickListener(this::onClick)

        leftHider = ViewHider.of(leftButton).setDirection(ViewHider.LEFT).build()
        topHider = ViewHider.of(topButton).setDirection(ViewHider.TOP).build()
        rightHider = ViewHider.of(rightButton).setDirection(ViewHider.RIGHT).build()
        bottomHider = ViewHider.of(bottomButton).setDirection(ViewHider.BOTTOM).build()

        return rootView
    }

    private fun onClick(v: View) {
        when (v.id) {
            R.id.left_button -> leftHider.hide()
            R.id.top_button -> topHider.hide()
            R.id.right_button -> rightHider.hide()
            R.id.bottom_button -> bottomHider.hide()
            R.id.reset -> {
                leftHider.show()
                topHider.show()
                rightHider.show()
                bottomHider.show()
            }
        }
    }

}
