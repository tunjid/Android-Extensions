package com.tunjid.androidx.tabcomms.ble

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.lifecycle.AndroidViewModel
import com.jakewharton.rx.replayingShare
import com.tunjid.androidx.App
import com.tunjid.androidx.R
import com.tunjid.androidx.communications.bluetooth.BLEScanner
import com.tunjid.androidx.communications.bluetooth.ScanFilterCompat
import com.tunjid.androidx.communications.bluetooth.ScanResultCompat
import com.tunjid.androidx.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.Flowables
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

sealed class BleInput {
    data class Permission(val hasPermission: Boolean) : BleInput()
    object StartScanning : BleInput()
    object StopScanning : BleInput()
}

private sealed class BleUpdate {
    data class Availability(val hasBluetooth: Boolean) : BleUpdate()
    data class Permission(val hasPermission: Boolean) : BleUpdate()
    data class Found(val result: ScanResultCompat) : BleUpdate()
    data class Scanning(val isScanning: Boolean) : BleUpdate()
    object TurnOn : BleUpdate()
}

private data class AggregatedInput(
    val hasPermission: Boolean = false,
    val scan: Boolean = false
)

data class BleState(
    val turnOn: Boolean = false,
    val text: String = "",
    val hasBluetooth: Boolean = false,
    val isScanning: Boolean = false,
    val items: List<ScanResultCompat> = listOf()
)

class BleViewModel(application: Application) : AndroidViewModel(application) {

    private val context get() = getApplication<App>()
    private val inputProcessor: PublishProcessor<BleInput> = PublishProcessor.create()

    val state = inputProcessor
        .scan(AggregatedInput()) { aggregated, input ->
            when (input) {
                BleInput.StopScanning -> aggregated.copy(scan = false)
                is BleInput.StartScanning -> aggregated.copy(scan = true)
                is BleInput.Permission -> aggregated.copy(hasPermission = input.hasPermission)
            }
        }
        .distinctUntilChanged()
        .switchMap { aggregated ->
            when {
                aggregated.scan && aggregated.hasPermission -> context.bluetoothUpdates()
                !aggregated.scan -> Flowable.just(BleUpdate.Scanning(isScanning = false))
                !aggregated.hasPermission -> Flowable.just(BleUpdate.Permission(hasPermission = false))
                else -> Flowable.empty()
            }
        }
        .scan(BleState()) { state, output ->
            when (output) {
                is BleUpdate.Scanning -> state.copy(isScanning = output.isScanning)
                is BleUpdate.Found -> state.copy(items = state.items.plus(output.result)
                    .distinctBy { it.device.address }
                    .sortedBy { it.device.name }
                )
                is BleUpdate.Availability -> state.copy(
                    hasBluetooth = output.hasBluetooth,
                    text = if (output.hasBluetooth) "" else context.getString(R.string.ble_not_supported)
                )
                is BleUpdate.Permission ->
                    if (output.hasPermission) state
                    else state.copy(text = context.getString(R.string.ble_no_permission))
                BleUpdate.TurnOn -> state.copy(turnOn = true)
            }
        }
        .toLiveData()

    fun accept(input: BleInput) = inputProcessor.onNext(input)
}

private fun Context.bluetoothUpdates(): Flowable<BleUpdate> =
    if (bluetoothManager?.adapter?.isEnabled == false) Flowable.just(BleUpdate.TurnOn)
    else Flowables.create<BleUpdate>(BackpressureStrategy.BUFFER) { emitter ->
        val bluetoothManager = bluetoothManager

        val scanner = if (bluetoothManager != null)
            BLEScanner.getBuilder(bluetoothManager.adapter)
                .addFilter(ScanFilterCompat.getBuilder()
                    .setServiceUuid(ParcelUuid(UUID.fromString(CUSTOM_SERVICE_UUID)))
                    .build())
                .withCallBack { emitter.onNext(BleUpdate.Found(it)) }
                .build()
        else null

        emitter.onNext(BleUpdate.Availability(hasBluetooth = scanner != null))
        emitter.setCancellable { scanner?.stopScan() }
        scanner?.startScan()
    }
        .replayingShare()
        .takeUntil(Flowable.timer(SCAN_PERIOD, TimeUnit.SECONDS, Schedulers.io()))
        .startWith(BleUpdate.Scanning(isScanning = true))
        .concatWith(Flowable.just(BleUpdate.Scanning(isScanning = false)))

private val Context.bluetoothManager: BluetoothManager?
    get() = if (packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    else null

private const val SCAN_PERIOD: Long = 10
private const val CUSTOM_SERVICE_UUID = "195AE58A-437A-489B-B0CD-B7C9C394BAE4"