package com.tunjid.androidx.test.idlingresources

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Idling resource that waits for a fragment to be visible.
 *
 *
 * Created by tj.dahunsi on 4/29/17.
 */
class FragmentVisibleIdlingResource(
    fragmentManager: FragmentManager,
    private val fragmentTag: String
) : BaseFragmentIdlingResource(fragmentManager, fragmentTag) {

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        if (f.tag == fragmentTag) idle = true
    }
}
