package com.tunjid.androidx

import android.content.Context
import android.content.res.Configuration
import android.view.MenuItem
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.DividerItemDecoration
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.graphics.drawable.withTint
import io.reactivex.Flowable

fun <T> Flowable<T>.toLiveData() = LiveDataReactiveStreams.fromPublisher(this)

inline fun <reified T> Flowable<in T>.filterIsInstance(): Flowable<T> = filter { it is T }.cast(T::class.java)

fun <T, R> LiveData<T>.map(mapper: (T) -> R) = Transformations.map(this, mapper)

fun <T, R> LiveData<T>.mapDistinct(mapper: (T) -> R): LiveData<R> =
    map(mapper).distinctUntilChanged()

fun <T> LiveData<T>.distinctUntilChanged() = Transformations.distinctUntilChanged(this)

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

fun Context.divider(orientation: Int) = DividerItemDecoration(this, orientation).apply {
    setDrawable(drawableAt(R.drawable.bg_divider)?.withTint(themeColorAt(R.attr.colorSurface))!!)
}