package com.tunjid.androidbootstrap.core.testclasses

import android.os.Bundle
import androidx.annotation.IdRes
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import com.tunjid.androidbootstrap.core.R
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity

/**
 * Test Activity
 *
 *
 * Created by tj.dahunsi on 4/29/17.
 */
class TestActivity : BaseActivity() {

    @IdRes
    val ignoredLayoutId = View.generateViewId()

    private val contentView: ViewGroup
        get() {
            val parent = FrameLayout(this)
            val ignored = FrameLayout(this)
            val inner = FrameLayout(this)

            ignored.id = ignoredLayoutId
            inner.id = R.id.main_fragment_container

            parent.addView(ignored)
            parent.addView(inner)

            return parent
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentView)
    }
}
