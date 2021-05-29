package com.tunjid.androidx.tabmisc

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tunjid.androidx.R
import com.tunjid.androidx.core.components.doOnEveryEvent
import com.tunjid.androidx.core.delegates.fragmentArgs
import com.tunjid.androidx.core.text.color
import com.tunjid.androidx.core.text.plus
import com.tunjid.androidx.databinding.FragmentSimpleListBinding
import com.tunjid.androidx.databinding.ViewholderUiStateSliceBinding
import com.tunjid.androidx.recyclerview.listAdapterOf
import com.tunjid.androidx.recyclerview.viewbinding.BindingViewHolder
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderDelegate
import com.tunjid.androidx.recyclerview.viewbinding.viewHolderFrom
import com.tunjid.androidx.uidrivers.InsetFlags
import com.tunjid.androidx.uidrivers.UiState
import com.tunjid.androidx.uidrivers.callback
import com.tunjid.androidx.uidrivers.uiState
import com.tunjid.androidx.uidrivers.updatePartial

class UiStatePlaygroundFragment : Fragment(R.layout.fragment_simple_list) {

    private var isTopLevel by fragmentArgs<Boolean>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isTopLevel) uiState = uiState.copy(
            toolbarMenuRes = R.menu.menu_ui_state_playground,
            toolbarMenuClickListener = viewLifecycleOwner.callback(::onMenuItemClicked),
            toolbarTitle = getString(R.string.ui_state_playground),
            toolbarOverlaps = false,
            toolbarShows = true,
            showsBottomNav = true,
            fabIcon = R.drawable.ic_add_24dp,
            fabText = getString(R.string.hi),
            fabShows = true
        )
        else viewLifecycleOwner.lifecycle.doOnEveryEvent(Lifecycle.Event.ON_RESUME) {
            ::uiState.updatePartial {
                copy(
                    fabShows = true,
                    fabIcon = R.drawable.ic_add_24dp,
                    fabText = getString(R.string.hi),
                )
            }
        }

        val context = requireContext()
        val fragmentBinding = FragmentSimpleListBinding.bind(view)

        fragmentBinding.recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        fragmentBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        fragmentBinding.recyclerView.adapter = listAdapterOf(
            initialItems = context.slices,
            viewHolderCreator = { parent: ViewGroup, _ ->
                parent.viewHolderFrom(ViewholderUiStateSliceBinding::inflate).apply {
                    this.binding.root.setOnClickListener {
                        MaterialAlertDialogBuilder(context)
                            .setTitle(R.string.choose_an_option)
                            .setItems(slice.optionNames()) { _, index ->
                                slice.choose(index)
                                fragmentBinding.recyclerView.adapter!!.notifyItemChanged(adapterPosition)
                            }
                            .show()
                    }
                }
            },
            viewHolderBinder = { holder, item, _/*index*/ ->
                holder.slice = item
                holder.binding.sliceName.text = item.name
                holder.binding.sliceDescription.text = item.currentSelection()
            }
        )
    }

    private fun onMenuItemClicked(item: MenuItem) = when (item.itemId) {
        R.id.select_all -> requireContext().getSystemService<InputMethodManager>()
            ?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        else -> Unit
    }

    private fun <T> Slice<T>.optionNames() = options.map(nameTransformer).toTypedArray()

    private fun <T> Slice<T>.currentSelection() = nameTransformer(getter(uiState))

    private fun <T> Slice<T>.choose(index: Int) {
        uiState = setter.invoke(uiState, options[index])
    }

    companion object {
        fun newInstance(isTopLevel: Boolean): UiStatePlaygroundFragment = UiStatePlaygroundFragment().apply { this.isTopLevel = isTopLevel }
    }
}

private val Int.stringHex: CharSequence get() = "⦿".color(this) + "#${Integer.toHexString(this)}"

private var BindingViewHolder<ViewholderUiStateSliceBinding>.slice by viewHolderDelegate<Slice<*>>()

private val Context.slices
    get() = listOf<Slice<*>>(
        Slice(
            name = "Status bar color",
            nameTransformer = Int::stringHex,
            options = listOf(Color.TRANSPARENT, Color.parseColor("#80000000"), Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE),
            getter = UiState::statusBarColor
        ) {
            copy(statusBarColor = it)
        },
        Slice(
            name = "Is immersive",
            options = listOf(true, false),
            getter = UiState::isImmersive
        ) {
            copy(isImmersive = it)
        },
        Slice(
            name = "Has light status bar icons",
            options = listOf(true, false),
            getter = UiState::lightStatusBar
        ) {
            copy(lightStatusBar = it)
        },
        Slice(
            name = "Toolbar title",
            options = listOf(
                R.string.ui_state_playground,
                R.string.reality_can_be,
                R.string.inevitable,
                R.string.survivor
            ).map(resources::getString),
            getter = UiState::toolbarTitle
        ) {
            copy(toolbarTitle = it)
        },
        Slice(
            name = "Tool bar shows",
            options = listOf(true, false),
            getter = UiState::toolbarShows
        ) {
            copy(toolbarShows = it)
        },
        Slice(
            name = "Tool bar overlaps",
            options = listOf(true, false),
            getter = UiState::toolbarOverlaps
        ) {
            copy(toolbarOverlaps = it)
        },
        Slice(
            name = "FAB shows",
            options = listOf(true, false),
            getter = UiState::fabShows
        ) {
            copy(fabShows = it)
        },
        Slice(
            name = "FAB icon",
            nameTransformer = resources::getResourceName,
            options = listOf(
                R.drawable.ic_add_24dp,
                R.drawable.ic_android_24dp,
                R.drawable.ic_bullseye_24dp,
                R.drawable.ic_compass_24dp
            ),
            getter = { it.fabIcon }
        ) {
            copy(fabIcon = it)
        },
        Slice(
            name = "FAB text",
            options = listOf("Hello", "Hi", "How do you do"),
            getter = UiState::fabText
        ) {
            copy(fabText = it)
        },
        Slice(
            name = "FAB extended",
            options = listOf(true, false),
            getter = UiState::fabExtended
        ) {
            copy(fabExtended = it)
        },
        Slice(
            name = "Bottom nav shows",
            options = listOf(true, false),
            getter = UiState::showsBottomNav
        ) {
            copy(showsBottomNav = it)
        },
        Slice(
            name = "Nav bar color",
            nameTransformer = Int::stringHex,
            options = listOf(Color.TRANSPARENT, Color.parseColor("#80000000"), Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE),
            getter = UiState::navBarColor
        ) {
            copy(navBarColor = it)
        },
        Slice(
            name = "Inset Flags",
            options = listOf(InsetFlags.ALL, InsetFlags.NO_TOP, InsetFlags.NO_BOTTOM, InsetFlags.NONE),
            getter = UiState::insetFlags
        ) {
            copy(insetFlags = it)
        }
    )

data class Slice<T>(
    val name: String,
    val options: List<T>,
    val nameTransformer: (T) -> CharSequence = Any?::toString,
    val getter: (UiState) -> T,
    val setter: UiState.(T) -> UiState
)