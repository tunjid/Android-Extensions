package com.tunjid.androidbootstrap.viewmodels;

import android.app.Application;
import android.util.Log;

import com.tunjid.androidbootstrap.functions.collections.Lists;
import com.tunjid.androidbootstrap.model.Tile;
import com.tunjid.androidbootstrap.recyclerview.diff.Diff;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Single;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class EndlessTileViewModel extends AndroidViewModel {

    public static final int NUM_TILES = 24;

    private final List<Tile> tiles;

    public EndlessTileViewModel(@NonNull Application application) {
        super(application);
        tiles = new ArrayList<>(NUM_TILES);
        tiles.addAll(generateTiles());
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public Single<DiffUtil.DiffResult> getMoreTiles() {
        return Single.fromCallable(() -> Diff.calculate(tiles, generateTiles(), (oldTiles, addedTiles) -> {
            oldTiles.addAll(addedTiles);
            return oldTiles;
        }))
                .subscribeOn(io())
                .observeOn(mainThread())
                .map(diff -> {
                    Lists.replace(tiles, diff.cumulative);
                    Log.i("TEST", "new size: " + tiles.size());
                    return diff.result;
                });
    }

    private List<Tile> generateTiles() {
        int next = tiles.size() + 1;
        int end = next + NUM_TILES;

        List<Tile> result = new ArrayList<>(NUM_TILES);
        for (int i = next; i < end; i++) result.add(Tile.generate(i));

        return result;
    }
}
