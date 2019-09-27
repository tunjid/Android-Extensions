package com.tunjid.androidx.viewmodels

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import com.tunjid.androidx.communications.bluetooth.BLEScanner
import com.tunjid.androidx.communications.bluetooth.ScanFilterCompat
import com.tunjid.androidx.communications.bluetooth.ScanResultCompat
import com.tunjid.androidx.functions.collections.Lists
import com.tunjid.androidx.recyclerview.diff.Diff
import com.tunjid.androidx.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class BleViewModel(application: Application) : AndroidViewModel(application) {

    val scanResults: List<ScanResultCompat>

    private val scanner: BLEScanner?
    private val disposables = CompositeDisposable()
    private var processor: PublishProcessor<Diff<ScanResultCompat>> = PublishProcessor.create()

    val devices: MutableLiveData<DiffUtil.DiffResult> = MutableLiveData()
    val isScanning: MutableLiveData<Boolean> = MutableLiveData()

    val isBleOn: Boolean
        get() = scanner != null && scanner.isEnabled

    init {
        scanResults = ArrayList()

        val bluetoothManager = if (application.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        else null

        scanner = if (bluetoothManager != null) BLEScanner.getBuilder(bluetoothManager.adapter)
                .addFilter(ScanFilterCompat.getBuilder()
                        .setServiceUuid(ParcelUuid(UUID.fromString(CUSTOM_SERVICE_UUID)))
                        .build())
                .withCallBack(this::onDeviceFound)
                .build()
        else null

        stopScanning()
        processor = PublishProcessor.create()
    }

    override fun onCleared() {
        super.onCleared()
        scanner?.stopScan()
        disposables.clear()
    }

    fun hasBle(): Boolean = scanner != null

    fun findDevices() {
        if (scanner == null) return

        stopScanning()
        processor = PublishProcessor.create()
        scanner.startScan()

        isScanning.value = true

        // Clear list first, then start scanning.
        disposables.add(Flowable.fromCallable {
            Diff.calculate(scanResults,
                    emptyList(),
                    { _, _ -> emptyList() },
                    { result -> Differentiable.fromCharSequence { result.device.address } })
        }
                .concatWith(processor.take(SCAN_PERIOD, TimeUnit.SECONDS, Schedulers.io()))
                .doOnTerminate { isScanning.postValue(false) }
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread()).subscribe { diff ->
                    Lists.replace(scanResults, diff.items)
                    devices.value = diff.result
                })
    }

    fun stopScanning() {
        if (!processor.hasComplete()) processor.onComplete()
        scanner?.stopScan()
    }

    private fun onDeviceFound(scanResult: ScanResultCompat) {
        if (!processor.hasComplete())
            processor.onNext(Diff.calculate(
                    scanResults,
                    mutableListOf(scanResult),
                    this::addServices
            ) { result -> Differentiable.fromCharSequence { result.device.address } })
    }

    private fun addServices(currentServices: List<ScanResultCompat>, foundServices: List<ScanResultCompat>): List<ScanResultCompat> {
        val equalityMapper = { result: ScanResultCompat -> result.device.address }
        val union = Lists.union<ScanResultCompat, String>(currentServices, foundServices, equalityMapper)
        Lists.replace(currentServices, union)

        return union.sortedBy(equalityMapper)
    }

    companion object {

        private const val SCAN_PERIOD: Long = 10
        private const val CUSTOM_SERVICE_UUID = "195AE58A-437A-489B-B0CD-B7C9C394BAE4"
    }
}
