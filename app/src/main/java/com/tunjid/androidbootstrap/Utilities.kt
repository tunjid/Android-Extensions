package com.tunjid.androidbootstrap

import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.Flowable

fun <T> Flowable<T>.toLiveData() = LiveDataReactiveStreams.fromPublisher(this)
