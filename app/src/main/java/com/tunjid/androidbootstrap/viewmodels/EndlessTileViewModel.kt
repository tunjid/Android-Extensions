package com.tunjid.androidbootstrap.viewmodels

import android.app.Application

import com.tunjid.androidbootstrap.functions.collections.Lists
import com.tunjid.androidbootstrap.model.Tile
import com.tunjid.androidbootstrap.recyclerview.diff.Diff

import java.util.ArrayList
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.Single

import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io

class EndlessTileViewModel(application: Application) : AndroidViewModel(application) {

    val tiles: MutableList<Tile>

    val moreTiles: Single<DiffUtil.DiffResult>
        get() = Single.fromCallable {
            Diff.calculate(tiles, generateTiles()) { oldTiles, addedTiles ->
                oldTiles.addAll(addedTiles)
                oldTiles
            }
        }
                .subscribeOn(io())
                .observeOn(mainThread())
                .map { diff ->
                    Lists.replace(tiles, diff.items)
                    diff.result
                }

    init {
        tiles = ArrayList(NUM_TILES)
        tiles.addAll(generateTiles())
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
