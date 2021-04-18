package com.tunjid.androidx.tabmisc

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.transition.TransitionManager
import com.transitionseverywhere.ChangeText
import com.transitionseverywhere.ChangeText.CHANGE_BEHAVIOR_OUT_IN
import com.tunjid.androidx.CounterService
import com.tunjid.androidx.R
import com.tunjid.androidx.core.components.services.HardServiceConnection
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.tabnav.routing.routeName
import com.tunjid.androidx.uidrivers.callback


class HardServiceConnectionFragment : Fragment(R.layout.fragment_hard_service_connection) {

    private var isTopLevel by fragmentArgs<Boolean>()
    private val connection by lazy { HardServiceConnection(requireContext(), CounterService::class.java, this::onServiceBound) }

    private var statusText: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isTopLevel) uiState = uiState.copy(
            toolbarTitle = this::class.java.routeName,
            toolbarOverlaps = false,
            toolbarShows = true,
            toolbarMenuRes = 0,
            fabShows = true,
            fabIcon = R.drawable.ic_connect_24dp,
            fabText = getText(R.string.bind_service),
            fabClickListener = viewLifecycleOwner.callback { toggleService() },
            insetFlags = InsetFlags.ALL,
            showsBottomNav = true,
            lightStatusBar = !requireContext().isDarkTheme,
            navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        statusText = view.findViewById(R.id.text)
        updateText(getString(R.string.service_disconnected))
    }

    private fun toggleService() = when (connection.boundService) {
        null -> connection.bind().let { Unit }
        else -> {
            connection.unbindService()
            updateText(getString(R.string.service_disconnected))
            uiState = uiState.copy(fabIcon = R.drawable.ic_connect_24dp, fabText = getText(R.string.bind_service))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        statusText = null
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.unbindService()
    }

    private fun onServiceBound(service: CounterService) {
        uiState = uiState.copy(fabIcon = R.drawable.ic_disconnect_24dp, fabText = getText(R.string.unbind_service))
        service.counter.observe(this) { updateText(resources.getQuantityString(R.plurals.bind_duration, it.toInt(), it)) }
    }

    private fun updateText(text: CharSequence) = (view as? ViewGroup)?.run {
        TransitionManager.beginDelayedTransition(this, ChangeText().setChangeBehavior(CHANGE_BEHAVIOR_OUT_IN))
        statusText?.text = text
    }

    companion object {
        fun newInstance(isTopLevel: Boolean): HardServiceConnectionFragment = HardServiceConnectionFragment().apply { this.isTopLevel = isTopLevel }
    }

}