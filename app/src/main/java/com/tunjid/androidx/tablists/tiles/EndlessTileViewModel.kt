package com.tunjid.androidx.tablists.tiles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import com.tunjid.androidx.functions.collections.replace
import com.tunjid.androidx.recyclerview.diff.Diff
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers.io
import java.util.*

class EndlessTileViewModel(application: Application) : AndroidViewModel(application) {

    val tiles: MutableList<Tile>

    private val disposables = CompositeDisposable()

    val moreTiles: MutableLiveData<DiffUtil.DiffResult> = MutableLiveData()

    init {
        tiles = ArrayList(NUM_TILES)
        tiles.addAll(generateTiles())
    }

    override fun onCleared() = disposables.clear()

    fun fetchMore() {
        disposables.add(Single.fromCallable {
            Diff.calculate(tiles, generateTiles()) { oldTiles, addedTiles -> oldTiles + addedTiles }
        }
            .subscribeOn(io())
            .observeOn(mainThread())
            .map { diff ->
                tiles.replace(diff.items)
                diff.result
            }.subscribe(moreTiles::setValue))
    }

    private fun generateTiles(): List<Tile> {
        val next = tiles.size + 1
        val end = next + NUM_TILES

        val result = ArrayList<Tile>(NUM_TILES)
        for (i in next until end) result.add(Tile.generate(i))

        return result
    }

    companion object {

        const val NUM_TILES = 24
    }
}
