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

class Route(val destination: CharSequence, val description: CharSequence) : Parcelable {
    constructor(parcel: Parcel) : this(
            TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel),
            TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        TextUtils.writeToParcel(destination, parcel, 0)
        TextUtils.writeToParcel(description, parcel, 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Route> {
        override fun createFromParcel(parcel: Parcel): Route = Route(parcel)

        override fun newArray(size: Int): Array<Route?> = arrayOfNulls(size)
    }
}
