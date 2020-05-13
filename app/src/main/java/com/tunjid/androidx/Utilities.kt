package com.tunjid.androidx

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.view.MenuItem
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.graphics.drawable.withTint
import io.reactivex.Flowable
import java.util.*

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

fun Context.divider(orientation: Int): RecyclerView.ItemDecoration {
    val decoration = DividerItemDecoration(this, orientation)
    decoration.setDrawable(drawableAt(R.drawable.bg_divider)?.withTint(themeColorAt(R.attr.colorSurface))!!)
    return decoration
}