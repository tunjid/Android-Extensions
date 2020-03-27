package com.tunjid.androidx.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.observe
import androidx.transition.TransitionManager
import com.transitionseverywhere.ChangeText
import com.transitionseverywhere.ChangeText.CHANGE_BEHAVIOR_OUT_IN
import com.tunjid.androidx.CounterService
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.services.HardServiceConnection
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.viewmodels.routeName


class HardServiceConnectionFragment : AppBaseFragment(R.layout.fragment_hard_service_connection) {

    private lateinit var connection: HardServiceConnection<CounterService>
    private var statusText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connection = HardServiceConnection(requireContext(), CounterService::class.java, this::onServiceBound)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolBarMenu = 0,
                toolbarShows = true,
                fabShows = true,
                fabIcon = R.drawable.ic_connect_24dp,
                fabText = getText(R.string.bind_service),
                fabClickListener = { toggleService() },
                showsBottomNav = true,
                lightStatusBar = !requireContext().isDarkTheme,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color)
        )

        statusText = view.findViewById(R.id.text)
        updateText(getString(R.string.service_disconnected))
    }

    private fun toggleService() = when {
        connection.boundService == null -> {
            connection.bind().let { Unit }
        }
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
        fun newInstance(): HardServiceConnectionFragment = HardServiceConnectionFragment().apply { arguments = Bundle() }
    }

}