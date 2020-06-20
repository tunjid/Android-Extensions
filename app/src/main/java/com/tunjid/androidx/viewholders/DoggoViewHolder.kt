package com.tunjid.androidx.viewholders

import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat.setTransitionName
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.view.util.hashTransitionName

/**
 * Various things that bind doggos and their images
 */
interface DoggoBinder {
    var doggo: Doggo?
    val doggoName: TextView?
    val thumbnail: ImageView
    val fullResolution: ImageView?
    fun onDoggoThumbnailLoaded(doggo: Doggo)
}

fun DoggoBinder.bind(doggo: Doggo) {
    this.doggo = doggo

    setTransitionName(thumbnail, thumbnail.hashTransitionName(doggo))
    doggo.imageCreator()
            .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
            .into(thumbnail) thumbnail@{
                onDoggoThumbnailLoaded(doggo)
                val full = fullResolution ?: return@thumbnail
                full.postDelayed(FULL_SIZE_DELAY.toLong()) {
                    doggo.imageCreator()
                            .fit()
                            .into(full) { full.isVisible = false }
                }
            }

    doggoName?.text = doggo.name
}

private fun Doggo.imageCreator(): RequestCreator = Picasso.get().load(imageRes).centerCrop()

private fun RequestCreator.into(imageView: ImageView, onSuccess: () -> Unit) = imageView.doOnAttach {
    into(imageView, object : Callback.EmptyCallback() {
        override fun onSuccess() = onSuccess()
    })
    imageView.doOnDetach { Picasso.get().cancelRequest(imageView) }
}

private const val FULL_SIZE_DELAY = 100
private const val THUMBNAIL_SIZE = 250
