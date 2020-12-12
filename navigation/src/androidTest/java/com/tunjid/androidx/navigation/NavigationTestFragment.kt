package com.tunjid.androidx.navigation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import com.tunjid.androidx.core.delegates.fragmentArgs

/**
 * Test fragment
 *
 *
 * Created by Shemanigans on 4/29/17.
 */
@VisibleForTesting
class NavigationTestFragment : Fragment(), Navigator.TagProvider {

    override val stableTag: String get() = argTag

    private var argTag by fragmentArgs<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Created NavigationTestFragment with tag: $stableTag")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "Destroying view of NavigationTestFragment with tag: $stableTag")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return TextView(inflater.context)
    }

    companion object {

        private val TAG = NavigationTestFragment::class.java.simpleName

        fun newInstance(stringArg: String): NavigationTestFragment = NavigationTestFragment().apply { this.argTag = stringArg }
    }
}
