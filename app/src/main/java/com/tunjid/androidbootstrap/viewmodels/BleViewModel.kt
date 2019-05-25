package com.tunjid.androidbootstrap.viewmodels

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.DiffUtil
import com.tunjid.androidbootstrap.communications.bluetooth.BLEScanner
import com.tunjid.androidbootstrap.communications.bluetooth.ScanFilterCompat
import com.tunjid.androidbootstrap.communications.bluetooth.ScanResultCompat
import com.tunjid.androidbootstrap.functions.collections.Lists
import com.tunjid.androidbootstrap.recyclerview.diff.Diff
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.Comparator

class BleViewModel(application: Application) : AndroidViewModel(application) {

    val scanResults: List<ScanResultCompat>
    private val scanner: BLEScanner?
    private var processor: PublishProcessor<Diff<ScanResultCompat>> = PublishProcessor.create()

    val isBleOn: Boolean
        get() = scanner != null && scanner.isEnabled

    init {
        scanResults = ArrayList()

        val bluetoothManager = if (application.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        else null

        if (bluetoothManager != null) scanner = BLEScanner.getBuilder(bluetoothManager.adapter)
                .addFilter(ScanFilterCompat.getBuilder()
                        .setServiceUuid(ParcelUuid(UUID.fromString(CUSTOM_SERVICE_UUID)))
                        .build())
                .withCallBack { this.onDeviceFound(it) }
                .build()
        else scanner = null

        stopScanning()
        processor = PublishProcessor.create()
    }

    override fun onCleared() {
        super.onCleared()
        scanner?.stopScan()
    }

    fun hasBle(): Boolean {
        return scanner != null
    }

    fun findDevices(): Flowable<DiffUtil.DiffResult> {
        if (scanner == null) return Flowable.empty()

        stopScanning()
        processor = PublishProcessor.create()
        scanner.startScan()

        // Clear list first, then start scanning.
        return Flowable.fromCallable {
            Diff.calculate(scanResults,
                    emptyList(),
                    { _, _ -> emptyList() },
                    { result -> Differentiable.fromCharSequence { result.device.address } })
        }
                .concatWith(processor.take(SCAN_PERIOD, TimeUnit.SECONDS, Schedulers.io()))
                .subscribeOn(Schedulers.io())
                .observeOn(mainThread()).map { diff ->
                    Lists.replace(scanResults, diff.items)
                    diff.result
                }
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
                    { currentServices, foundServices -> this.addServices(currentServices, foundServices) },
                    { result -> Differentiable.fromCharSequence { result.device.address } }))
    }

    private fun addServices(currentServices: MutableList<ScanResultCompat>, foundServices: List<ScanResultCompat>): List<ScanResultCompat> {
        val equalityMapper = { result: ScanResultCompat -> result.device.address }
        val union = Lists.union<ScanResultCompat, String>(currentServices, foundServices, equalityMapper)
        Lists.replace(currentServices, union)
        currentServices.sortWith(Comparator { a, b -> equalityMapper.invoke(a).compareTo(equalityMapper.invoke(b)) })
        return currentServices
    }

    companion object {

        private const val SCAN_PERIOD: Long = 10
        private const val CUSTOM_SERVICE_UUID = "195AE58A-437A-489B-B0CD-B7C9C394BAE4"
    }
}
