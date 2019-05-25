package com.tunjid.androidbootstrap.adapters

import android.view.View
import android.view.ViewGroup
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter
import com.tunjid.androidbootstrap.viewholders.DoggoViewHolder

class DoggoAdapter<T : DoggoViewHolder>(private val doggos: List<Doggo>,
                                        private val layoutRes: Int,
                                        private val viewHolderFactory: (View, ImageListAdapterListener) -> T,
                                        listener: ImageListAdapterListener) : InteractiveAdapter<T, DoggoAdapter.ImageListAdapterListener>(listener) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): T =
            viewHolderFactory.invoke(getItemView(layoutRes, parent), adapterListener)

    override fun onBindViewHolder(holder: T, recyclerViewPosition: Int) =
            holder.bind(doggos[recyclerViewPosition])

    override fun getItemCount(): Int = doggos.size

    override fun getItemId(position: Int): Long = doggos[position].hashCode().toLong()

    interface ImageListAdapterListener : AdapterListener {
        fun onDoggoClicked(doggo: Doggo) {}

        fun onDoggoImageLoaded(doggo: Doggo) {}
    }

}
