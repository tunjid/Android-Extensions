package com.tunjid.androidx.core.components.services

import android.app.Service
import android.os.Binder

/**
 * A [Binder] that returns the [Service] it's bound to. It is typically an inner class of said
 * [Service]
 */
abstract class SelfBinder<T> : Binder() where T : Service, T : SelfBindingService<T> {
    abstract val service: T
}