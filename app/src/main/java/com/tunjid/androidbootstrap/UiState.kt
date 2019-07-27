package com.tunjid.androidbootstrap

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import com.tunjid.androidbootstrap.view.util.InsetFlags

class UiState : Parcelable {

    @DrawableRes
    private val fabIcon: Int
    private val fabText: CharSequence
    @MenuRes
    private val toolBarMenu: Int
    @ColorInt
    private val navBarColor: Int

    private val showsFab: Boolean
    private val showsToolbar: Boolean

    private val insetFlags: InsetFlags
    private val toolbarTitle: CharSequence
    private val fabClickListener: View.OnClickListener?

    constructor(fabIcon: Int,
                fabText: CharSequence,
                toolBarMenu: Int,
                navBarColor: Int,
                showsFab: Boolean,
                showsToolbar: Boolean,
                insetFlags: InsetFlags,
                toolbarTitle: CharSequence,
                fabClickListener: View.OnClickListener?) {
        this.fabIcon = fabIcon
        this.fabText = fabText
        this.toolBarMenu = toolBarMenu
        this.navBarColor = navBarColor
        this.showsFab = showsFab
        this.showsToolbar = showsToolbar
        this.insetFlags = insetFlags
        this.toolbarTitle = toolbarTitle
        this.fabClickListener = fabClickListener
    }

    fun diff(force: Boolean, newState: UiState,
             showsFabConsumer: (Boolean) -> Unit,
             showsToolbarConsumer: (Boolean) -> Unit,
             navBarColorConsumer: (Int) -> Unit,
             insetFlagsConsumer: (InsetFlags) -> Unit,
             fabStateConsumer: (Int, CharSequence) -> Unit,
             toolbarStateConsumer: (Int, CharSequence) -> Unit,
             fabClickListenerConsumer: (View.OnClickListener?) -> Unit
    ): UiState {
        either(force, newState, { state -> state.toolBarMenu }, { state -> state.toolbarTitle }, toolbarStateConsumer)
        either(force, newState, { state -> state.fabIcon }, { state -> state.fabText }, fabStateConsumer)

        only(force, newState, { state -> state.showsFab }, showsFabConsumer)
        only(force, newState, { state -> state.showsToolbar }, showsToolbarConsumer)
        only(force, newState, { state -> state.navBarColor }, navBarColorConsumer)
        only(force, newState, { state -> state.insetFlags }, insetFlagsConsumer)

        fabClickListenerConsumer.invoke(newState.fabClickListener)

        return newState
    }

    private fun <T> only(force: Boolean, that: UiState, first: (UiState) -> T, consumer: (T) -> Unit) {
        val thisFirst = first.invoke(this)
        val thatFirst = first.invoke(that)

        if (force || thisFirst != thatFirst) consumer.invoke(thatFirst)

    }

    private fun <S, T> either(force: Boolean,
                              that: UiState,
                              first: (UiState) -> S,
                              second: (UiState) -> T,
                              biConsumer: (S, T) -> Unit) {
        val thisFirst = first.invoke(this)
        val thatFirst = first.invoke(that)
        val thisSecond = second.invoke(this)
        val thatSecond = second.invoke(that)

        if (force || thisFirst != thatFirst || thisSecond != thatSecond)
            biConsumer.invoke(thatFirst, thatSecond)
    }

    private constructor(`in`: Parcel) {
        fabIcon = `in`.readInt()
        fabText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(`in`)
        toolBarMenu = `in`.readInt()
        navBarColor = `in`.readInt()
        showsFab = `in`.readByte().toInt() != 0x00
        showsToolbar = `in`.readByte().toInt() != 0x00

        val hasLeftInset = `in`.readByte().toInt() != 0x00
        val hasTopInset = `in`.readByte().toInt() != 0x00
        val hasRightInset = `in`.readByte().toInt() != 0x00
        val hasBottomInset = `in`.readByte().toInt() != 0x00
        insetFlags = InsetFlags.create(hasLeftInset, hasTopInset, hasRightInset, hasBottomInset)

        toolbarTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(`in`)

        fabClickListener = null
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(fabIcon)
        TextUtils.writeToParcel(fabText, dest, 0)
        dest.writeInt(toolBarMenu)
        dest.writeInt(navBarColor)
        dest.writeByte((if (showsFab) 0x01 else 0x00).toByte())
        dest.writeByte((if (showsToolbar) 0x01 else 0x00).toByte())
        dest.writeByte((if (insetFlags.hasLeftInset()) 0x01 else 0x00).toByte())
        dest.writeByte((if (insetFlags.hasTopInset()) 0x01 else 0x00).toByte())
        dest.writeByte((if (insetFlags.hasRightInset()) 0x01 else 0x00).toByte())
        dest.writeByte((if (insetFlags.hasBottomInset()) 0x01 else 0x00).toByte())

        TextUtils.writeToParcel(toolbarTitle, dest, 0)
    }

    companion object {

        fun freshState(): UiState {
            return UiState(
                    fabIcon = 0,
                    fabText = "",
                    toolBarMenu = 0,
                    navBarColor = Color.BLACK,
                    showsFab = true,
                    showsToolbar = true,
                    insetFlags = InsetFlags.ALL,
                    toolbarTitle = "",
                    fabClickListener = null
            )
        }

        @JvmField
        @Suppress("unused")
        val CREATOR: Parcelable.Creator<UiState> = object : Parcelable.Creator<UiState> {
            override fun createFromParcel(`in`: Parcel): UiState = UiState(`in`)

            override fun newArray(size: Int): Array<UiState?> = arrayOfNulls(size)
        }
    }
}