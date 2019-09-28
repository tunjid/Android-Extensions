package com.tunjid.androidx.core.components

import android.app.ActivityManager
import android.app.ActivityManager.RunningServiceInfo
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log

/**
 * A class for binding a [Service]
 *
 *
 * Created by tj.dahunsi on 4/2/17.
 */

open class ServiceConnection<T : Service> @JvmOverloads constructor(
        val serviceClass: Class<T>,
        private val bindCallback: BindCallback<T>? = null
) : android.content.ServiceConnection {

    private var bindingContext: Context? = null

    var boundService: T? = null
        private set

    val isBound: Boolean
        get() = boundService != null

    // Type safety quasi guaranteed, provided binding is done through API
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        require(service is Binder<*>) { "Bound Service is not a Binder" }

        @Suppress("UNCHECKED_CAST")
        boundService = (service as Binder<T>).service
        bindCallback?.onServiceBound(boundService)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        boundService = null
    }

    fun with(context: Context): Binding<T> {
        bindingContext = context
        return Binding(context, this)
    }

    fun unbindService(): Boolean {
        if (bindingContext != null) return try {
            bindingContext?.unbindService(this)
            bindingContext = null
            true
        } catch (e: IllegalArgumentException) {
            Log.i(TAG, serviceClass.name + " was not bound")
            false
        }
        return false
    }

    abstract class Binder<T : Service> : android.os.Binder() {
        abstract val service: T
    }

    @FunctionalInterface
    interface BindCallback<T : Service> {
        fun onServiceBound(service: T?)
    }

    // Class is used in a public API
    class Binding<T : Service> internal constructor(
            private val context: Context,
            private val serviceConnection: ServiceConnection<T>
    ) {
        private val intent: Intent = Intent(context, serviceConnection.serviceClass)

        fun setExtras(extras: Bundle): Binding<*> {
            intent.replaceExtras(extras)
            return this
        }

        fun bind(): Boolean =
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        fun start() {
            context.startService(intent)
        }
    }

    companion object {

        private const val TAG = "ServiceConnection"

        fun isServiceRunning(serviceClass: Class<out Service>, context: Context): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val services = activityManager.getRunningServices(Integer.MAX_VALUE)

            for (info in services) if (isEquals(serviceClass, info)) return true
            return false
        }

        private fun isEquals(serviceClass: Class<out Service>, runningServiceInfo: RunningServiceInfo): Boolean =
                runningServiceInfo.service.className == serviceClass.name
    }
}
