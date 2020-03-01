package com.tunjid.androidx.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat.setTransitionName
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
    val doggoName: TextView
    val fullSize: ImageView?
    val thumbnail: ImageView
    fun onDoggoThumbnailLoaded(doggo: Doggo)
}

fun DoggoBinder.bind(doggo: Doggo) {
    this.doggo = doggo

    setTransitionName(thumbnail, thumbnail.hashTransitionName(doggo))
    doggo.imageCreator()
            .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
            .into(thumbnail, onSuccess {
                onDoggoThumbnailLoaded(doggo)
                fullSize?.postDelayed(FULL_SIZE_DELAY.toLong()) {
                    doggo.imageCreator()
                            .fit()
                            .into(fullSize, onSuccess { fullSize?.visibility = View.VISIBLE })
                }
            })

    doggoName.text = doggo.name
}

private fun Doggo.imageCreator(): RequestCreator = Picasso.get().load(imageRes).centerCrop()

private fun onSuccess(runnable: () -> Unit): Callback = object : Callback {
    override fun onSuccess() = runnable.invoke()

    override fun onError(e: Exception) = e.printStackTrace()
}

private const val FULL_SIZE_DELAY = 100
private const val THUMBNAIL_SIZE = 250
