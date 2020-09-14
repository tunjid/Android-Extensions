package com.tunjid.androidx.uidrivers

import android.view.View
import androidx.fragment.app.Fragment
import androidx.transition.TransitionValues
import com.tunjid.androidx.core.content.unwrapActivity
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private class GlobalUiHostDelegate<T : Any>(
        private val source: (T) -> Any?
) : ReadOnlyProperty<T, GlobalUiHost> {

    private val T.logName get() = this::class.java.simpleName

    override operator fun getValue(thisRef: T, property: KProperty<*>): GlobalUiHost =
            (source.invoke(thisRef) as? GlobalUiHost)
                    ?: throw IllegalStateException("This ${thisRef.logName} is not hosted by a GlobalUiHost")
}

private val Fragment.globalUiHost by GlobalUiHostDelegate<Fragment>(Fragment::getActivity)

private val View.globalUiHost by GlobalUiHostDelegate<View> { it.context.unwrapActivity }

private val TransitionValues.globalUiHost by GlobalUiHostDelegate<TransitionValues>{ it.view.context.unwrapActivity }

val Fragment.liveUiState get() = globalUiHost.globalUiController.liveUiState

val View.liveUiState get() = globalUiHost.globalUiController.liveUiState

/**
 * Convenience for changing [GlobalUiConfig] with a [Fragment] ref.
 */
var Fragment.uiState: UiState
    get() = globalUiHost.globalUiController.uiState
    set(value) {
        globalUiHost.globalUiController.uiState = value
    }

/**
 * Convenience for changing [GlobalUiConfig] with a [View] ref, useful in Anko Components
 */
var View.uiState: UiState
    get() = globalUiHost.globalUiController.uiState
    set(value) {
        globalUiHost.globalUiController.uiState = value
    }

var TransitionValues.uiState: UiState
    get() = globalUiHost.globalUiController.uiState
    set(value) {
        globalUiHost.globalUiController.uiState = value
    }
