package com.tunjid.androidbootstrap

import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.Flowable

fun <T> Flowable<T>.toLiveData() = LiveDataReactiveStreams.fromPublisher(this)

inline fun <T> Iterable<T>.modifiableForEach(action: (T) -> Unit) =
        iterator().run { while (hasNext()) next().apply(action); Unit }