package com.tunjid.androidx.test.idlingresources

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Idling resource that idles until a fragment has disappeared.
 *
 *
 * Created by tj.dahunsi on 4/29/17.
 */
class FragmentGoneIdlingResource(
        fragmentManager: FragmentManager,
        private val fragmentTag: String
) : BaseFragmentIdlingResource(fragmentManager, fragmentTag) {

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        if (f.tag == fragmentTag) idle = true
    }
}
