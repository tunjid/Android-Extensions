package com.tunjid.androidbootstrap.viewmodels

import android.app.Application
import android.util.Pair
import androidx.lifecycle.AndroidViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.functions.collections.Lists
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.recyclerview.diff.Diff
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers.io
import java.util.*
import kotlin.math.min

class DoggoRankViewModel(application: Application) : AndroidViewModel(application) {

    val doggos: MutableList<Doggo> = ArrayList(Doggo.doggos)
    private val disposables: CompositeDisposable = CompositeDisposable()
    private val processor: PublishProcessor<DiffUtil.DiffResult> = PublishProcessor.create()

    private var doggoIdPositionPair: Pair<Long, Int>? = null

    fun watchDoggos(): Flowable<DiffUtil.DiffResult> = processor

    fun onActionStarted(doggoIdActionPair: Pair<Long, Int>) {
        val id = doggoIdActionPair.first
        val currentIndex = Lists.transform(doggos) { it.hashCode() }.indexOf(id.toInt())
        doggoIdPositionPair = Pair(id, currentIndex)
    }

    fun swap(from: Int, to: Int) {
        if (from < to) for (i in from until to) Collections.swap(doggos, i, i + 1)
        else for (i in from downTo to + 1) Collections.swap(doggos, i, i - 1)
    }

    fun remove(position: Int): Pair<Int, Int> {
        doggos.removeAt(position)

        val lastIndex = doggos.size - 1
        return Pair(min(position, lastIndex), lastIndex)
    }

    fun onActionEnded(doggoIdActionPair: Pair<Long, Int>): String {
        val action = doggoIdActionPair.second
        val startPosition = doggoIdPositionPair!!.second
        val startId = doggoIdPositionPair!!.first
        val endId = doggoIdActionPair.first

        if (action == ACTION_STATE_IDLE || startId != endId) return ""

        val isRemoving = action == ACTION_STATE_SWIPE
        val ids = Lists.transform(if (isRemoving) Doggo.doggos else doggos) { it.hashCode() }
        val endPosition = ids.indexOf(endId.toInt())

        if (endPosition < 0) return ""
        val doggo = (if (isRemoving) Doggo.doggos else doggos)[endPosition]

        if (isRemoving && doggos.indexOf(doggo) >= 0) return "" // Doggo is still in the list
        else if (!isRemoving && startPosition == endPosition) return "" // Doggo kept it's rank

        val context = getApplication<Application>()

        return if (isRemoving) context.getString(R.string.doggo_removed, doggo.name)
        else context.getString(R.string.doggo_moved, doggo.name, endPosition + 1)
    }

    fun resetList() {
        disposables.add(Single.fromCallable {
            Diff.calculate(
                    doggos,
                    Doggo.doggos,
                    { _, additions -> additions },
                    { doggo -> Differentiable.fromCharSequence { doggo.hashCode().toString() } })
        }
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe { doggoDiff ->
                    doggos.clear()
                    doggos.addAll(doggoDiff.items)
                    processor.onNext(doggoDiff.result)
                })
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        processor.onComplete()
    }
}
