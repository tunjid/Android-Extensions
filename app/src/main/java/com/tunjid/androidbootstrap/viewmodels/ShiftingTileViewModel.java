package com.tunjid.androidbootstrap.viewmodels;

import android.app.Application;

import com.tunjid.androidbootstrap.functions.collections.Lists;
import com.tunjid.androidbootstrap.model.Tile;
import com.tunjid.androidbootstrap.recyclerview.diff.Diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class ShiftingTileViewModel extends AndroidViewModel {

    private static final int NUM_TILES = 32;

    private boolean changes;
    private final List<Tile> tiles;
    private final CompositeDisposable disposables;
    private final PublishProcessor<DiffUtil.DiffResult> processor;

    public ShiftingTileViewModel(@NonNull Application application) {
        super(application);
        tiles = new ArrayList<>(generateTiles(NUM_TILES));
        disposables = new CompositeDisposable();
        processor = PublishProcessor.create();
        dance();
    }

    public void toggleChanges() {
        changes = !changes;
    }

    public boolean changes() {
        return changes;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public Flowable<DiffUtil.DiffResult> watchTiles() {
        return processor;
    }

    private void dance() {
        disposables.add(Flowable.interval(2, TimeUnit.SECONDS, Schedulers.io())
                .map(__ -> makeNewTiles())
                .map(newTiles -> Diff.calculate(tiles, newTiles, (__, newTilesCopy) -> newTilesCopy))
                .observeOn(mainThread())
                .subscribe(diff -> {
                    Lists.replace(tiles, diff.cumulative);
                    processor.onNext(diff.result);
                }, processor::onError));
    }

    private List<Tile> generateTiles(int number) {
        List<Tile> result = new ArrayList<>(number);

        for (int i = 0; i < number; i++) result.add(Tile.generate(i));
        Collections.shuffle(result);

        return result;
    }

    private List<Tile> makeNewTiles() {
        return generateTiles(changes ? Math.max(5, (int) (Math.random() * NUM_TILES)) : NUM_TILES);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
        processor.onComplete();
    }
}
