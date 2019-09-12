package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.view.View
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.DoggoAdapter.ImageListAdapterListener
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.core.components.args
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.viewholders.DoggoViewHolder

class DoggoFragment : AppBaseFragment(R.layout.fragment_image_detail), ImageListAdapterListener {

    override val stableTag: String
        get() = super.stableTag + "-" + doggo

    private var doggo: Doggo by args()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.tag = doggo
        DoggoViewHolder(view, this).bind(doggo)
    }

    override fun onDoggoImageLoaded(doggo: Doggo) = parentFragment!!.startPostponedEnterTransition()

    companion object {

        fun newInstance(doggo: Doggo): DoggoFragment = DoggoFragment().apply { this.doggo = doggo }

    }
}
