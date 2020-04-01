package com.tunjid.androidx

import android.content.Context
import android.content.res.Configuration
import android.view.MenuItem
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.Transformations
import com.tunjid.androidx.core.graphics.drawable.withTint
import io.reactivex.Flowable

fun <T> Flowable<T>.toLiveData() = LiveDataReactiveStreams.fromPublisher(this)

fun <T, R> LiveData<T>.map(mapper: (T) -> R) = Transformations.map(this, mapper)

fun <T> LiveData<T>.distinctUntilChanged() = Transformations.distinctUntilChanged(this)

inline fun <T> Iterable<T>.modifiableForEach(action: (T) -> Unit) =
        iterator().run { while (hasNext()) next().apply(action); Unit }

fun MenuItem.setLoading(@ColorInt tint: Int): MenuItem? = setActionView(R.layout.actionbar_indeterminate_progress).also {
    val progressBar = it?.actionView as? ProgressBar ?: return@also
    progressBar.apply { indeterminateDrawable = indeterminateDrawable.withTint(tint) }
}

val Context.isDarkTheme
    get() = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_NO,
        Configuration.UI_MODE_NIGHT_UNDEFINED -> false
        else -> true
    }