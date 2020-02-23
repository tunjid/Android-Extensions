package com.tunjid.androidx.viewmodels

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.palette.graphics.Palette
import com.tunjid.androidx.App
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.toLiveData
import com.tunjid.androidx.uidrivers.BACKGROUND_TINT_DURATION
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.Maybes
import io.reactivex.schedulers.Schedulers.io
import kotlin.math.max
import kotlin.math.min

class DoggoViewModel(application: Application) : AndroidViewModel(application) {

    private val Doggo.color get() = colorMap.getOrPut(this) { calculateColor() }
    private val colorMap = mutableMapOf<Doggo, Maybe<Int>>()
    private val processor = PublishProcessor.create<Int>()
    private val disposables = CompositeDisposable()
    private val colorEvaluator = ArgbEvaluator()
    val colors = processor.toLiveData()

    val doggos = Doggo.doggos

    init {
        disposables.add(when (val doggo = Doggo.transitionDoggo) {
            null -> Maybe.empty<Int>()
            else -> colorMap.getOrPut(doggo) { doggo.calculateColor() }
        }.subscribe { endColor -> animate(endColor) }).let { colors }
    }

    fun onSwiped(current: Int, fraction: Float, toTheRight: Boolean) {
        val percentage = if (toTheRight) fraction else 1 - fraction
        val next = when (toTheRight) {
            true -> min(current + 1, doggos.size - 1)
            false -> max(current - 1, 0)
        }

        disposables.add(Maybes.zip(doggos[current].color, doggos[next].color) { a, b -> colorEvaluator.evaluate(percentage, a, b) as Int }
                .subscribeOn(io())
                .subscribe(processor::onNext, Throwable::printStackTrace)
        )
    }

    private fun animate(endColor: Int?) = ValueAnimator.ofObject(colorEvaluator, Color.TRANSPARENT, endColor).apply {
        addUpdateListener { processor.onNext(it.animatedValue as Int) }
        duration = BACKGROUND_TINT_DURATION
        start()
    }

    private fun Doggo.calculateColor(): Maybe<Int> = Maybe.fromCallable {
        val app = getApplication<App>()
        val metrics = app.resources.displayMetrics
        val bitmap = app.getDrawable(imageRes)?.toBitmap(
                width = metrics.widthPixels / 4,
                height = metrics.heightPixels / 4,
                config = Bitmap.Config.ARGB_8888) ?: return@fromCallable Color.BLACK

        Palette.from(bitmap).generate().getLightVibrantColor(Color.BLACK)
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
