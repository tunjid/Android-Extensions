package com.tunjid.androidx.core.components.services

import android.app.ActivityManager
import android.app.ActivityManager.RunningServiceInfo
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

/**
 * An implementation of a [ServiceConnection] that holds a reference to the [Service]
 * it is bound to.
 *
 *
 * Created by tj.dahunsi on 4/2/17.
 */

class HardServiceConnection<T> @JvmOverloads constructor(
        private val context: Context,
        private val serviceClass: Class<T>,
        private val bindCallback: ((T) -> Unit)? = null
) : ServiceConnection where T : Service, T : SelfBindingService<T> {

    var boundService: T? = null
        private set

    // Type safety quasi guaranteed, provided binding is done through API
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        require(service is SelfBinder<*>) { "Bound Service is not a SelfBinder" }

        @Suppress("UNCHECKED_CAST")
        boundService = ((service as SelfBinder<T>).service).apply { bindCallback?.invoke(this) }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        boundService = null
    }

    /**
     * Binds the typed [Service] to the supplied context
     */
    fun bind(flags: Int = Context.BIND_AUTO_CREATE, intentModifier: Intent.() -> Unit = {}) =
            context.bindService(Intent(context, serviceClass).apply(intentModifier), this, flags)

    /**
     * Convenience method for starting the typed [Service]
     */
    fun start(intentModifier: Intent.() -> Unit = {}) =
            context.startService(Intent(context, serviceClass).apply(intentModifier))

    /**
     * Unbinds the service from the last [Context] instance it was bound to. Returns true if
     * there was a [Service] bound to this [HardServiceConnection], false otherwise
     */
    fun unbindService(): Boolean = try {
        boundService = null
        context.unbindService(this)
        true
    } catch (e: IllegalArgumentException) {
        Log.i(TAG, "Attempted to unbind ${serviceClass.name}, but it was not bound")
        false
    }

    companion object {

        private const val TAG = "HardServiceConnection"

        @Suppress("DEPRECATION")
        fun isServiceRunning(serviceClass: Class<out Service>, context: Context): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val services = activityManager.getRunningServices(Integer.MAX_VALUE)

            for (info in services) if (isEqual(serviceClass, info)) return true
            return false
        }

        private fun isEqual(serviceClass: Class<out Service>, runningServiceInfo: RunningServiceInfo): Boolean =
                runningServiceInfo.service.className == serviceClass.name
    }
}
