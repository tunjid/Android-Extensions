package com.tunjid.androidx.model

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils

/**
 * Routes in the sample app
 *
 *
 * Created by tj.dahunsi on 5/30/17.
 */

sealed class RouteItem {
    object Spacer : RouteItem()
    data class Destination(
            val destination: CharSequence,
            val description: CharSequence
    ) : RouteItem(), Parcelable {
        constructor(parcel: Parcel) : this(
                TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
                TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel))

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            TextUtils.writeToParcel(destination, parcel, 0)
            TextUtils.writeToParcel(description, parcel, 0)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Destination> {
            override fun createFromParcel(parcel: Parcel): Destination = Destination(parcel)

            override fun newArray(size: Int): Array<Destination?> = arrayOfNulls(size)
        }
    }
}
