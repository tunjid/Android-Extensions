package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.DoggoAdapter.ImageListAdapterListener
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.viewholders.DoggoViewHolder

class DoggoFragment : AppBaseFragment(R.layout.fragment_image_detail), ImageListAdapterListener {

    override val stableTag: String
        get() = super.stableTag + "-" + arguments!!.getParcelable(ARG_DOGGO)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val doggo = arguments!!.getParcelable<Doggo>(ARG_DOGGO)!!

        view.tag = doggo
        DoggoViewHolder(view, this).bind(doggo)
    }

    override fun onDoggoImageLoaded(doggo: Doggo) = parentFragment!!.startPostponedEnterTransition()

    companion object {
        private const val ARG_DOGGO = "doggo"

        fun newInstance(doggo: Doggo): DoggoFragment = DoggoFragment().apply {
            arguments = bundleOf(ARG_DOGGO to doggo)
        }

    }
}
