package com.tunjid.androidx.core.components

import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tunjid.androidx.core.delegates.intentExtras
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

data class DummyParcelable(val value: String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) = parcel.writeString(value)

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<DummyParcelable> {
        override fun createFromParcel(parcel: Parcel): DummyParcelable = DummyParcelable(parcel)

        override fun newArray(size: Int): Array<DummyParcelable?> = arrayOfNulls(size)
    }
}

private var Intent.defaultFalseFlag by intentExtras(false)
private var Intent.defaultTrueFlag by intentExtras(true)
private var Intent.dummyParcelable by intentExtras<DummyParcelable?>()

@RunWith(AndroidJUnit4::class)
class BundleDelegateTest {

    private var intent = Intent()

    @Before
    fun setup() {
        intent = Intent()
    }

    @Test
    fun testIntentWrite() {
        assertFalse(intent.defaultFalseFlag)
        assertTrue(intent.defaultTrueFlag)
        assertNull(intent.dummyParcelable)

        val dummy = DummyParcelable("hello")

        intent.defaultFalseFlag = true
        intent.defaultTrueFlag = false
        intent.dummyParcelable = dummy

        assertTrue(intent.defaultFalseFlag)
        assertFalse(intent.defaultTrueFlag)
        assertEquals(dummy, intent.dummyParcelable)
    }
}