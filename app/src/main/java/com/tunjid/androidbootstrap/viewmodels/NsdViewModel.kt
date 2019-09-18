package com.tunjid.androidbootstrap.viewmodels

import android.app.Application
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import com.tunjid.androidbootstrap.communications.nsd.NsdHelper
import com.tunjid.androidbootstrap.functions.collections.Lists
import com.tunjid.androidbootstrap.recyclerview.diff.Diff
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class NsdViewModel(application: Application) : AndroidViewModel(application) {

    val services: List<NsdServiceInfo>

    private val nsdHelper: NsdHelper
    private val disposables = CompositeDisposable()
    private var processor: PublishProcessor<Diff<NsdServiceInfo>> = PublishProcessor.create()

    val scanChanges: MutableLiveData<DiffUtil.DiffResult> = MutableLiveData()
    val isScanning: MutableLiveData<Boolean> = MutableLiveData()

    init {
        nsdHelper = NsdHelper.getBuilder(getApplication())
                .setServiceFoundConsumer(this::onServiceFound)
                .setResolveSuccessConsumer(this::onServiceResolved)
                .setResolveErrorConsumer(this::onServiceResolutionFailed)
                .build()

        services = ArrayList()
        reset()
    }

    override fun onCleared() {
        super.onCleared()
        nsdHelper.stopServiceDiscovery()
        nsdHelper.tearDown()
        disposables.clear()
    }

    fun findDevices() {
        reset()
        nsdHelper.discoverServices()

        isScanning.value = true

        // Clear list first, then start scanning.
        disposables.add(Flowable.fromCallable {
            Diff.calculate(services,
                    emptyList(),
                    { _, _ -> emptyList() },
                    { info -> Differentiable.fromCharSequence { info.serviceName } })
        }
                .concatWith(processor.take(SCAN_PERIOD, TimeUnit.SECONDS, Schedulers.io()))
                .doOnTerminate { isScanning.postValue(false) }
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread()).subscribe { diff ->
                    Lists.replace(services, diff.items)
                    scanChanges.value = diff.result
                })
    }

    fun stopScanning() {
        if (!processor.hasComplete()) processor.onComplete()
        nsdHelper.stopServiceDiscovery()
    }

    private fun reset() {
        stopScanning()
        processor = PublishProcessor.create()
    }

    private fun onServiceFound(service: NsdServiceInfo) = nsdHelper.resolveService(service)

    private fun onServiceResolutionFailed(service: NsdServiceInfo, errorCode: Int) {
        if (errorCode == NsdManager.FAILURE_ALREADY_ACTIVE) nsdHelper.resolveService(service)
    }

    private fun onServiceResolved(service: NsdServiceInfo) {
        if (!processor.hasComplete()) processor.onNext(Diff.calculate(
                services,
                mutableListOf(service),
                this::addServices
        ) { info -> Differentiable.fromCharSequence { info.serviceName } })
    }

    private fun addServices(currentServices: List<NsdServiceInfo>, foundServices: List<NsdServiceInfo>): List<NsdServiceInfo> {
        val union = Lists.union<NsdServiceInfo, String>(currentServices, foundServices) { it.serviceName }
        return union.sortedBy { it.serviceName }
    }

    companion object {

        private const val SCAN_PERIOD: Long = 10
    }
}
