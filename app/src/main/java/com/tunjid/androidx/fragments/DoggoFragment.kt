package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.DoggoAdapter.ImageListAdapterListener
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.viewholders.DoggoViewHolder

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
