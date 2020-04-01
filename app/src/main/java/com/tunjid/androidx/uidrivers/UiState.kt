package com.tunjid.androidx.uidrivers

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.dynamicanimation.animation.SpringAnimation
import com.tunjid.androidx.view.util.InsetFlags
import kotlin.reflect.KMutableProperty0

fun KMutableProperty0<UiState>.update(updater: UiState.() -> UiState) = set(updater.invoke(get()))

typealias ToolbarState = Triple<Int, Boolean, CharSequence>
typealias FabState = Pair<Int, CharSequence>

val UiState.toolbarState get() = ToolbarState(toolBarMenu, toolbarInvalidated, toolbarTitle)
val UiState.fabState get() = FabState(fabIcon, fabText)

data class UiState(
        @MenuRes
        val toolBarMenu: Int,
        val toolbarShows: Boolean,
        val toolbarInvalidated: Boolean,
        val toolbarTitle: CharSequence,
        @DrawableRes
        val fabIcon: Int,
        val fabShows: Boolean,
        val fabExtended: Boolean,
        val fabText: CharSequence,
        @ColorInt
        val backgroundColor: Int,
        val snackbarText: CharSequence,
        @ColorInt
        val navBarColor: Int,
        val lightStatusBar: Boolean,
        val showsBottomNav: Boolean,
        val insetFlags: InsetFlags,
        val fabClickListener: ((View) -> Unit)?,
        val fabTransitionOptions: (SpringAnimation.() -> Unit)?
) : Parcelable {

    private constructor(`in`: Parcel) : this(
            toolBarMenu = `in`.readInt(),
            toolbarShows = `in`.readByte().toInt() != 0x00,
            toolbarInvalidated = `in`.readByte().toInt() != 0x00,
            toolbarTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(`in`),
            fabIcon = `in`.readInt(),
            fabShows = `in`.readByte().toInt() != 0x00,
            fabExtended = `in`.readByte().toInt() != 0x00,
            fabText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(`in`),
            backgroundColor = `in`.readInt(),
            snackbarText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(`in`),
            navBarColor = `in`.readInt(),
            lightStatusBar = `in`.readByte().toInt() != 0x00,
            showsBottomNav = `in`.readByte().toInt() != 0x00,
            insetFlags = InsetFlags(
                    hasLeftInset = `in`.readByte().toInt() != 0x00,
                    hasTopInset = `in`.readByte().toInt() != 0x00,
                    hasRightInset = `in`.readByte().toInt() != 0x00,
                    hasBottomInset = `in`.readByte().toInt() != 0x00
            ),
            fabClickListener = null,
            fabTransitionOptions = null
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(toolBarMenu)
        dest.writeByte((if (toolbarShows) 0x01 else 0x00).toByte())
        dest.writeByte((if (toolbarInvalidated) 0x01 else 0x00).toByte())
        TextUtils.writeToParcel(toolbarTitle, dest, 0)
        dest.writeInt(fabIcon)
        dest.writeByte((if (fabShows) 0x01 else 0x00).toByte())
        dest.writeByte((if (fabExtended) 0x01 else 0x00).toByte())
        TextUtils.writeToParcel(fabText, dest, 0)
        dest.writeInt(backgroundColor)
        TextUtils.writeToParcel(snackbarText, dest, 0)
        dest.writeInt(navBarColor)
        dest.writeByte((if (showsBottomNav) 0x01 else 0x00).toByte())
        dest.writeByte((if (insetFlags.hasLeftInset) 0x01 else 0x00).toByte())
        dest.writeByte((if (insetFlags.hasTopInset) 0x01 else 0x00).toByte())
        dest.writeByte((if (insetFlags.hasRightInset) 0x01 else 0x00).toByte())
        dest.writeByte((if (insetFlags.hasBottomInset) 0x01 else 0x00).toByte())
    }

    companion object {

        fun freshState(): UiState = UiState(
                fabIcon = 0,
                fabText = "",
                toolBarMenu = 0,
                navBarColor = Color.BLACK,
                lightStatusBar = false,
                showsBottomNav = true,
                fabShows = true,
                fabExtended = true,
                backgroundColor = Color.TRANSPARENT,
                toolbarShows = true,
                snackbarText = "",
                toolbarInvalidated = false,
                toolbarTitle = "",
                fabClickListener = null,
                fabTransitionOptions = null,
                insetFlags = InsetFlags.ALL
        )

        @JvmField
        @Suppress("unused")
        val CREATOR: Parcelable.Creator<UiState> = object : Parcelable.Creator<UiState> {
            override fun createFromParcel(`in`: Parcel): UiState = UiState(`in`)

            override fun newArray(size: Int): Array<UiState?> = arrayOfNulls(size)
        }
    }
}