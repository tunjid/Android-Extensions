package com.tunjid.androidbootstrap.adapters

import android.view.View
import android.view.ViewGroup
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.view.util.inflate
import com.tunjid.androidbootstrap.viewholders.DoggoViewHolder

class DoggoAdapter<T : DoggoViewHolder>(private val doggos: List<Doggo>,
                                        private val layoutRes: Int,
                                        private val viewHolderFactory: (View, ImageListAdapterListener) -> T,
                                        listener: ImageListAdapterListener
) : InteractiveAdapter<T, DoggoAdapter.ImageListAdapterListener>(listener) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): T =
            viewHolderFactory.invoke(parent.inflate(layoutRes), delegate)

    override fun onBindViewHolder(holder: T, recyclerViewPosition: Int) =
            holder.bind(doggos[recyclerViewPosition])

    override fun getItemCount(): Int = doggos.size

    override fun getItemId(position: Int): Long = doggos[position].hashCode().toLong()

    interface ImageListAdapterListener {
        fun onDoggoClicked(doggo: Doggo) = Unit

        fun onDoggoImageLoaded(doggo: Doggo) = Unit
    }

}
