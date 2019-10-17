package com.tunjid.androidx.core.components.services

import android.app.Service
import android.content.Intent

/**
 * A [Service] that returns an implementation of [SelfBinder] from it's [Service.onBind] method.
 * A [SelfBinder] is typically an inner class of the [Service] that has an easy reference to the
 * service itself
 */
interface SelfBindingService<T> where T : Service, T : SelfBindingService<T> {
    /**
     * Same signature as [Service.onBind], but returns a [SelfBinder] instance which is aware of
     * the type of [Service] it binds to, reducing the need for what would appear to be spurious
     * casting
     */
    fun onBind(intent: Intent): SelfBinder<T>
}