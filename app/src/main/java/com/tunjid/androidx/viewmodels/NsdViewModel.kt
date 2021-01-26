package com.tunjid.androidx.viewmodels

import android.app.Application
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.AndroidViewModel
import com.jakewharton.rx.replayingShare
import com.tunjid.androidx.App
import com.tunjid.androidx.communications.nsd.NsdHelper
import com.tunjid.androidx.filterIsInstance
import com.tunjid.androidx.recyclerview.diff.Diffable
import com.tunjid.androidx.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.Flowables
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

sealed class Input {
    object StartScanning : Input()
    object StopScanning : Input()
}

private sealed class Output {
    data class Scanning(val isScanning: Boolean) : Output()
    data class ScanResult(val item: NsdItem) : Output()
}

data class NSDState(
    val isScanning: Boolean = false,
    val items: List<NsdItem> = listOf()
)

data class NsdItem(
    val info: NsdServiceInfo
) : Diffable {
    override val diffId: String
        get() = info.host.hostAddress
}

private val NsdItem.sortKey get() = info.serviceName

class NsdViewModel(application: Application) : AndroidViewModel(application) {

    private val context get() = getApplication<App>()
    private val disposables = CompositeDisposable()
    private val inputProcessor: PublishProcessor<Input> = PublishProcessor.create()

    val state = inputProcessor
        .switchMap {
            when (it) {
                Input.StartScanning -> context.nsdServices()
                    .map(::NsdItem)
                    .map<Output>(Output::ScanResult)
                    .startWith(Output.Scanning(isScanning = true))
                    .concatWith(Flowable.just(Output.Scanning(isScanning = false)))
                Input.StopScanning -> Flowable.just(Output.Scanning(isScanning = false))
            }
        }
        .scan(NSDState()) { state, output ->
            when (output) {
                is Output.Scanning -> state.copy(isScanning = output.isScanning)
                is Output.ScanResult -> state.copy(items = state.items.plus(output.item)
                    .distinctBy(NsdItem::diffId)
                    .sortedBy(NsdItem::sortKey)
                )
            }
        }
        .toLiveData()

    override fun onCleared() = disposables.clear()

    fun accept(input: Input) = inputProcessor.onNext(input)
}

private fun Context.nsdServices(): Flowable<NsdServiceInfo> {
    val emissions = Flowables.create<NsdUpdate>(BackpressureStrategy.BUFFER) { emitter ->
        emitter.onNext(NsdUpdate.Helper(NsdHelper.getBuilder(this)
            .setServiceFoundConsumer { emitter.onNext(NsdUpdate.Found(it)) }
            .setResolveSuccessConsumer { emitter.onNext(NsdUpdate.Resolved(it)) }
            .setResolveErrorConsumer { service, errorCode -> emitter.onNext(NsdUpdate.ResolutionFailed(service, errorCode)) }
            .build()))
    }
        .replayingShare()

    return emissions
        .filterIsInstance<NsdUpdate.Helper>()
        .switchMap { (nsdHelper) ->
            emissions
                .doOnNext(nsdHelper::onUpdate)
                .doFinally(nsdHelper::tearDown)
                .filterIsInstance<NsdUpdate.Resolved>()
                .map(NsdUpdate.Resolved::service)
                .startWith(Flowable.empty<NsdServiceInfo>().delay(2, TimeUnit.SECONDS, Schedulers.io()))
        }
        .takeUntil(Flowable.timer(SCAN_PERIOD, TimeUnit.SECONDS, Schedulers.io()))
}

private fun NsdHelper.onUpdate(update: NsdUpdate) = when (update) {
    is NsdUpdate.Helper -> discoverServices()
    is NsdUpdate.Found -> resolveService(update.service)
    is NsdUpdate.ResolutionFailed -> when (update.errorCode) {
        NsdManager.FAILURE_ALREADY_ACTIVE -> resolveService(update.service)
        else -> Unit
    }
    else -> Unit
}

private sealed class NsdUpdate {
    data class Helper(val helper: NsdHelper) : NsdUpdate()
    data class Found(val service: NsdServiceInfo) : NsdUpdate()
    data class Resolved(val service: NsdServiceInfo) : NsdUpdate()
    data class ResolutionFailed(val service: NsdServiceInfo, val errorCode: Int) : NsdUpdate()
}

private const val SCAN_PERIOD: Long = 25
