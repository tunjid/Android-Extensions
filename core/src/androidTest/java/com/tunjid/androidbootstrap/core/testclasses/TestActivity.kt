package com.tunjid.androidbootstrap.core.testclasses

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.tunjid.androidbootstrap.core.R

/**
 * Test Activity
 *
 *
 * Created by tj.dahunsi on 4/29/17.
 */
class TestActivity : AppCompatActivity() {

    @IdRes
    val ignoredLayoutId = View.generateViewId()
    val containerId = View.generateViewId()

    private val contentView: ViewGroup
        get() {
            val parent = FrameLayout(this)
            val ignored = FrameLayout(this)
            val inner = FrameLayout(this)

            ignored.id = ignoredLayoutId
            inner.id = containerId

            parent.addView(ignored)
            parent.addView(inner)

            return parent
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentView)
    }
}
