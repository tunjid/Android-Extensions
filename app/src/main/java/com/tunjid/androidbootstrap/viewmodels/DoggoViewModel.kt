package com.tunjid.androidbootstrap.viewmodels

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.animation.doOnEnd
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.palette.graphics.Palette
import com.tunjid.androidbootstrap.App
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment.Companion.BACKGROUND_TINT_DURATION
import com.tunjid.androidbootstrap.model.Doggo
import com.tunjid.androidbootstrap.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.Singles
import io.reactivex.schedulers.Schedulers.io
import kotlin.math.max
import kotlin.math.min

class DoggoViewModel(application: Application) : AndroidViewModel(application) {

    private val colorMap = mutableMapOf<Doggo, Single<Int>>()
    private val processor = PublishProcessor.create<Int>()
    private val disposables = CompositeDisposable()
    private val colorEvaluator = ArgbEvaluator()

    val doggos = Doggo.doggos

    fun getColors(startColor: Int)= processor.startWith(when (val doggo = Doggo.getTransitionDoggo()) {
        null -> Flowable.empty<Int>()
        else -> colorMap.getOrPut(doggo) { doggo.calculateColor() }.flatMapPublisher { endColor ->
            Flowable.create<Int>({ emitter ->
                val animator = ValueAnimator.ofObject(colorEvaluator, startColor, endColor)
                animator.addUpdateListener { emitter.onNext(it.animatedValue as Int) }
                animator.doOnEnd { emitter.onComplete() }
                animator.duration = BACKGROUND_TINT_DURATION.toLong()

                animator.start()
            }, BackpressureStrategy.DROP)
        }
    }).toLiveData()

    fun onSwiped(current: Int, fraction: Float, toTheRight: Boolean) {
        val percentage = if (toTheRight) fraction else 1 - fraction
        val next = when (toTheRight) {
            true -> min(current + 1, doggos.size - 1)
            false -> max(current - 1, 0)
        }

        disposables.add(Singles.zip(doggos[current].color, doggos[next].color) { a, b -> colorEvaluator.evaluate(percentage, a, b) as Int }
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(processor::onNext, Throwable::printStackTrace)
        )
    }

    private val Doggo.color get() = colorMap.getOrPut(this) { calculateColor() }

    private fun Doggo.calculateColor(): Single<Int> = Single.fromCallable {
        val app = getApplication<App>()
        val metrics = app.resources.displayMetrics
        val bitmap = app.getDrawable(imageRes)?.toBitmap(
                width = metrics.widthPixels / 4,
                height = metrics.heightPixels / 4,
                config = Bitmap.Config.ARGB_8888) ?: return@fromCallable Color.BLACK

        Palette.from(bitmap).generate().getDarkVibrantColor(Color.BLACK)
    }.cache()

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        processor.onComplete()
    }
}
