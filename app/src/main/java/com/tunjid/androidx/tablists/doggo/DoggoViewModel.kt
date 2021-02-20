package com.tunjid.androidx.tablists.doggo

import android.animation.ArgbEvaluator
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.palette.graphics.Palette
import com.tunjid.androidx.App
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.toLiveData
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.Maybes
import io.reactivex.schedulers.Schedulers.io
import kotlin.math.min

class DoggoViewModel(application: Application) : AndroidViewModel(application) {

    private val colorEvaluator = ArgbEvaluator()
    private val disposables = CompositeDisposable()
    private val processor = PublishProcessor.create<Int>()
    private val colorList = Doggo.doggos.map(this::calculateColor).onEach(Maybe<Int>::subscribe)

    val colors = processor.toLiveData()
    val doggos = Doggo.doggos

    fun onSwiped(current: Int, fraction: Float) {
        val next = min(current + 1, doggos.lastIndex)
        disposables.add(Maybes.zip(
                colorFor(doggos[current]),
                colorFor(doggos[next])
        ) { a, b -> colorEvaluator.evaluate(fraction, a, b) as Int }
                .subscribeOn(io())
                .subscribe(processor::onNext, Throwable::printStackTrace)
        )
    }

    private fun colorFor(doggo: Doggo) = colorList[doggos.indexOf(doggo)]

    private fun calculateColor(doggo: Doggo): Maybe<Int> = Maybe.fromCallable {
        val app = getApplication<App>()
        val metrics = app.resources.displayMetrics
        val bitmap = app.drawableAt(doggo.imageRes)?.toBitmap(
                width = metrics.widthPixels / 4,
                height = metrics.heightPixels / 4,
                config = Bitmap.Config.ARGB_8888
        ) ?: return@fromCallable Color.BLACK

        Palette.from(bitmap)
                .generate()
                .getDominantColor(Color.BLACK)
    }
            .subscribeOn(io())
            .observeOn(mainThread())
            .cache()

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        processor.onComplete()
    }
}
