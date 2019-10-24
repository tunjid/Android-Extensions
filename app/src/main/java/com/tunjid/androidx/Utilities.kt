package com.tunjid.androidx

import android.view.MenuItem
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.lifecycle.LiveDataReactiveStreams
import com.tunjid.androidx.core.graphics.drawable.updateTint
import io.reactivex.Flowable

fun <T> Flowable<T>.toLiveData() = LiveDataReactiveStreams.fromPublisher(this)

inline fun <T> Iterable<T>.modifiableForEach(action: (T) -> Unit) =
        iterator().run { while (hasNext()) next().apply(action); Unit }

fun MenuItem.setLoading(@ColorInt tint: Int): MenuItem? = setActionView(R.layout.actionbar_indeterminate_progress).also {
    val progressBar = it?.actionView as? ProgressBar ?: return@also
    progressBar.apply { indeterminateDrawable = indeterminateDrawable.updateTint(tint) }
}