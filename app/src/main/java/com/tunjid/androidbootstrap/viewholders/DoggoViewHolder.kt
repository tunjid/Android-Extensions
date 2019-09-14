package com.tunjid.androidbootstrap.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat.setTransitionName
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.adapters.DoggoAdapter
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.view.util.hashTransitionName

open class DoggoViewHolder(itemView: View, adapterListener: DoggoAdapter.ImageListAdapterListener)
    : InteractiveViewHolder<DoggoAdapter.ImageListAdapterListener>(itemView, adapterListener), View.OnClickListener {

    private lateinit var doggo: Doggo
    private val textView: TextView = itemView.findViewById(R.id.doggo_name)
    val fullSize: ImageView? = itemView.findViewById(R.id.full_size)
    val thumbnail: ImageView = itemView.findViewById(R.id.doggo_image)

    init {
        itemView.setOnClickListener(this)
    }

    open fun bind(doggo: Doggo) {
        this.doggo = doggo

        setTransitionName(thumbnail, thumbnail.hashTransitionName(doggo))
        getCreator(doggo)
                .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                .into(thumbnail, onSuccess(this::onThumbnailLoaded))

        textView.text = doggo.name
    }

    private fun onThumbnailLoaded() {
        delegate.onDoggoImageLoaded(doggo)
        fullSize?.postDelayed({
            getCreator(doggo).fit()
                    .into(fullSize, onSuccess { fullSize.visibility = View.VISIBLE })
        }, FULL_SIZE_DELAY.toLong())
    }

    private fun getCreator(doggo: Doggo): RequestCreator {
        return Picasso.get().load(doggo.imageRes).centerCrop()
    }

    private fun onSuccess(runnable: () -> Unit): Callback {
        return object : Callback {
            override fun onSuccess() = runnable.invoke()

            override fun onError(e: Exception) = e.printStackTrace()
        }
    }

    override fun onClick(v: View) = delegate.onDoggoClicked(doggo)

    companion object {

        private const val FULL_SIZE_DELAY = 100
        private const val THUMBNAIL_SIZE = 250
    }
}
