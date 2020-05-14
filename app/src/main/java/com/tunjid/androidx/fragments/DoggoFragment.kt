package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tunjid.androidx.R
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.databinding.FragmentImageDetailBinding
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.viewholders.DoggoBinder
import com.tunjid.androidx.viewholders.bind

class DoggoFragment : Fragment(R.layout.fragment_image_detail) {

    private var doggo: Doggo by args()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.tag = doggo

        val binding = FragmentImageDetailBinding.bind(view)
        object : DoggoBinder {
            override var doggo: Doggo?
                get() = this@DoggoFragment.doggo
                set(_) = Unit
            override val doggoName: TextView get() = binding.doggoName
            override val thumbnail: ImageView get() = binding.doggoImage
            override val fullResolution: ImageView? get() = binding.fullSize
            override fun onDoggoThumbnailLoaded(doggo: Doggo) = parentFragment?.startPostponedEnterTransition()
                    ?: Unit
        }.bind(doggo)
    }

    companion object {

        fun newInstance(doggo: Doggo): DoggoFragment = DoggoFragment().apply { this.doggo = doggo }

    }
}
