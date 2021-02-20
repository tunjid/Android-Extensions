package com.tunjid.androidx.tablists.tiles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tunjid.androidx.R
import com.tunjid.androidx.toLiveData
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.Flowables
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

data class ShiftingState(
    val changes: Boolean = false,
    val tiles: List<Tile> = generateTiles(changes)
)

val ShiftingState.fabIconRes: Int
    get() = if (changes) R.drawable.ic_grid_24dp else R.drawable.ic_blur_24dp

val ShiftingState.fabText: Int
    get() = if (changes) R.string.static_tiles else R.string.dynamic_tiles

class ShiftingTileViewModel(application: Application) : AndroidViewModel(application) {

    private val processor: PublishProcessor<Unit> = PublishProcessor.create()

    val state = Flowables.combineLatest(
        Flowable.interval(2, TimeUnit.SECONDS, Schedulers.io()),
        processor.scan(true) { flag, _ -> !flag }
    )
        .map(Pair<Long, Boolean>::second)
        .startWith(true)
        .map(::ShiftingState)
        .toLiveData()

    fun toggleChanges() = processor.onNext(Unit)
}

private fun generateTiles(changes: Boolean): List<Tile> =
    0.until(if (changes) max(5, (Math.random() * NUM_TILES).toInt()) else NUM_TILES)
        .map(Tile.Companion::generate)
        .shuffled()

private const val NUM_TILES = 32
