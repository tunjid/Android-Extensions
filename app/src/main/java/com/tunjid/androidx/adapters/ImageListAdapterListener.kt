package com.tunjid.androidx.adapters

import com.tunjid.androidx.model.Doggo

interface ImageListAdapterListener {
    fun onDoggoClicked(doggo: Doggo) = Unit

    fun onDoggoImageLoaded(doggo: Doggo) = Unit
}