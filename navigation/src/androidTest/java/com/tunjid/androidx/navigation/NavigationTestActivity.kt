package com.tunjid.androidx.navigation

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Test Activity
 *
 *
 * Created by tj.dahunsi on 4/29/17.
 */
class NavigationTestActivity : AppCompatActivity() {

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

fun <T: Navigator> T.waitForIdleSyncAfter(action: T.() -> Unit) {
    action(this)
    InstrumentationRegistry.getInstrumentation().waitForIdleSync()
}