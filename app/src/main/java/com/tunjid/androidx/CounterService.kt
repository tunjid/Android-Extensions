package com.tunjid.androidx

import android.app.Service
import android.content.Intent
import com.tunjid.androidx.core.components.services.SelfBinder
import com.tunjid.androidx.core.components.services.SelfBindingService
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class CounterService : Service(), SelfBindingService<CounterService> {

    private val binder = LocalBinder()
    private var disposable: Disposable? = null
    private val source = Flowable.interval(1, TimeUnit.SECONDS).map { it + 1 }.publish()

    val counter = source.toLiveData()

    override fun onBind(intent: Intent): LocalBinder {
        disposable = source.connect()
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    inner class LocalBinder : SelfBinder<CounterService>() {
        override val service: CounterService = this@CounterService
    }
}