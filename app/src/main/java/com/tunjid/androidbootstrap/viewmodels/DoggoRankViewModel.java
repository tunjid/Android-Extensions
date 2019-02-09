package com.tunjid.androidbootstrap.viewmodels;

import android.app.Application;
import android.content.Context;
import android.util.Pair;

import com.tunjid.androidbootstrap.R;
import com.tunjid.androidbootstrap.functions.TransformingSequentialList;
import com.tunjid.androidbootstrap.model.Doggo;
import com.tunjid.androidbootstrap.recyclerview.Differentiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE;
import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

public class DoggoRankViewModel extends AndroidViewModel {

    private Pair<Long, Integer> doggoIdPositionPair;
    private final List<Doggo> doggos;
    private final CompositeDisposable disposables;
    private final PublishProcessor<DiffUtil.DiffResult> processor;

    public DoggoRankViewModel(@NonNull Application application) {
        super(application);
        doggos = new ArrayList<>(Doggo.doggos);
        disposables = new CompositeDisposable();
        processor = PublishProcessor.create();
    }

    public List<Doggo> getDoggos() {
        return doggos;
    }

    public Flowable<DiffUtil.DiffResult> watchDoggos() {
        return processor;
    }

    public void onActionStarted(Pair<Long, Integer> doggoIdActionPair) {
        long id = doggoIdActionPair.first;
        int currentIndex = TransformingSequentialList.transform(doggos, Doggo::hashCode).indexOf((int) id);
        doggoIdPositionPair = new Pair<>(id, currentIndex);
    }

    public void swap(int from, int to) {
        if (from < to) for (int i = from; i < to; i++) Collections.swap(doggos, i, i + 1);
        else for (int i = from; i > to; i--) Collections.swap(doggos, i, i - 1);
    }

    public Pair<Integer, Integer> remove(int position) {
        doggos.remove(position);

        int lastIndex = doggos.size() - 1;
        return new Pair<>(Math.min(position, lastIndex), lastIndex);
    }

    public String onActionEnded(Pair<Long, Integer> doggoIdActionPair) {
        int action = doggoIdActionPair.second;
        int startPosition = doggoIdPositionPair.second;
        long startId = doggoIdPositionPair.first;
        long endId = doggoIdActionPair.first;

        if (action == ACTION_STATE_IDLE || startId != endId) return "";

        boolean isRemoving = action == ACTION_STATE_SWIPE;
        List<Integer> ids = TransformingSequentialList.transform(isRemoving ? Doggo.doggos : doggos, Doggo::hashCode);
        int endPosition = ids.indexOf((int) endId);

        if (endPosition < 0) return "";
        Doggo doggo = (isRemoving ? Doggo.doggos : doggos).get(endPosition);

        if (isRemoving && doggos.indexOf(doggo) >= 0) return ""; // Doggo is still in the list
        else if (!isRemoving && startPosition == endPosition) return ""; // Doggo kept it's rank

        Context context = getApplication();
        if (isRemoving) return context.getString(R.string.doggo_removed, doggo.getName());
        else return context.getString(R.string.doggo_moved, doggo.getName(), endPosition + 1);
    }

    public void resetList() {
        disposables.add(Single.fromCallable(() -> Differentiable.diff(
                doggos,
                Doggo.doggos,
                (destination, additions) -> additions,
                doggo -> Differentiable.fromCharSequence(() -> String.valueOf(doggo.hashCode()))))
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(doggoDiff -> {
                    doggos.clear();
                    doggos.addAll(doggoDiff.cumulative);
                    processor.onNext(doggoDiff.result);
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
        processor.onComplete();
    }
}
