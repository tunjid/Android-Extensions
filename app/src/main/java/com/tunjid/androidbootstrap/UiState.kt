package com.tunjid.androidbootstrap

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes

data class UiState(
        @DrawableRes val fabIcon: Int,
        val fabText: CharSequence,
        @MenuRes val toolBarMenu: Int,
        @ColorInt val navBarColor: Int,
        val showsFab: Boolean,
        val fabExtended: Boolean,
        val showsToolbar: Boolean,
        val toolbarTitle: CharSequence,
        val fabClickListener: View.OnClickListener?
) : Parcelable {

    fun diff(newState: UiState,
             showsFabConsumer: (Boolean) -> Unit,
             showsToolbarConsumer: (Boolean) -> Unit,
             navBarColorConsumer: (Int) -> Unit,
             fabStateConsumer: (Int, CharSequence) -> Unit,
             toolbarStateConsumer: (Int, CharSequence) -> Unit,
             fabClickListenerConsumer: (View.OnClickListener?) -> Unit
    ): UiState {
        either(newState, { state -> state.toolBarMenu }, { state -> state.toolbarTitle }, toolbarStateConsumer)
        either(newState, { state -> state.fabIcon }, { state -> state.fabText }, fabStateConsumer)

        only(newState, { state -> state.showsFab }, showsFabConsumer)
        only(newState, { state -> state.showsToolbar }, showsToolbarConsumer)
        only(newState, { state -> state.navBarColor }, navBarColorConsumer)

        fabClickListenerConsumer.invoke(newState.fabClickListener)

        return newState
    }

    private fun <T> only(that: UiState, first: (UiState) -> T, consumer: (T) -> Unit) {
        val thisFirst = first.invoke(this)
        val thatFirst = first.invoke(that)

        if (thisFirst != thatFirst) consumer.invoke(thatFirst)
    }

    private fun <S, T> either(that: UiState,
                              first: (UiState) -> S,
                              second: (UiState) -> T,
                              biConsumer: (S, T) -> Unit) {
        val thisFirst = first.invoke(this)
        val thatFirst = first.invoke(that)
        val thisSecond = second.invoke(this)
        val thatSecond = second.invoke(that)

        if (thisFirst != thatFirst || thisSecond != thatSecond)
            biConsumer.invoke(thatFirst, thatSecond)
    }

    private constructor(`in`: Parcel) : this(
            fabIcon = `in`.readInt(),
            fabText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(`in`),
            toolBarMenu = `in`.readInt(),
            navBarColor = `in`.readInt(),
            showsFab = `in`.readByte().toInt() != 0x00,
            fabExtended = `in`.readByte().toInt() != 0x00,
            showsToolbar = `in`.readByte().toInt() != 0x00,
            toolbarTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(`in`),
            fabClickListener = null
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(fabIcon)
        TextUtils.writeToParcel(fabText, dest, 0)
        dest.writeInt(toolBarMenu)
        dest.writeInt(navBarColor)
        dest.writeByte((if (showsFab) 0x01 else 0x00).toByte())
        dest.writeByte((if (fabExtended) 0x01 else 0x00).toByte())
        dest.writeByte((if (showsToolbar) 0x01 else 0x00).toByte())
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
                    fabExtended = true,
                    showsToolbar = true,
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