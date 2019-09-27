package com.tunjid.androidx.core.testclasses

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.tunjid.androidx.core.components.Navigator

/**
 * Test fragment
 *
 *
 * Created by Shemanigans on 4/29/17.
 */
@VisibleForTesting
class TestFragment : Fragment(), Navigator.TagProvider {

    override val stableTag: String
        get() = arguments!!.getString(STRING_ARG_KEY)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Created TestFragment with tag: $stableTag")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "Destroying view of TestFragment with tag: $stableTag")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return TextView(inflater.context)
    }

    companion object {

        private val TAG = TestFragment::class.java.simpleName
        private const val STRING_ARG_KEY = "STRING_ARG_KEY"

        fun newInstance(stringArg: String): TestFragment = TestFragment().apply { arguments = bundleOf(STRING_ARG_KEY to stringArg) }
    }
}
