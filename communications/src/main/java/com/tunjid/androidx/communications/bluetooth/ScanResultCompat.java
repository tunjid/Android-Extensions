package com.tunjid.androidx.communications.bluetooth;


import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import java.util.Arrays;

/**
 * Copy of Android ScanResult class added in API level 21
 * <p>
 * Created by tj.dahunsi on 5/9/17.
 */

public final class ScanResultCompat implements Parcelable {
    // Remote bluetooth device.
    private BluetoothDevice mDevice;

    // Scan record, including advertising data and scan response data.
    @Nullable
    private ScanRecordCompat mScanRecordCompat;

    // Received signal strength.
    private int mRssi;

    // Device timestamp when the result was last seen.
    private long mTimestampNanos;

    /**
     * Constructor of scan result.
     *
     * @param device           Remote bluetooth device that is found.
     * @param scanRecordCompat Scan record including both advertising data and scan response data.
     * @param rssi             Received signal strength.
     * @param timestampNanos   Device timestamp when the scan result was observed.
     */
    public ScanResultCompat(BluetoothDevice device, ScanRecordCompat scanRecordCompat, int rssi,
                            long timestampNanos) {
        mDevice = device;
        mScanRecordCompat = scanRecordCompat;
        mRssi = rssi;
        mTimestampNanos = timestampNanos;
    }

    private ScanResultCompat(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mDevice != null) {
            dest.writeInt(1);
            mDevice.writeToParcel(dest, flags);
        }
        else {
            dest.writeInt(0);
        }
        if (mScanRecordCompat != null) {
            dest.writeInt(1);
            dest.writeByteArray(mScanRecordCompat.getBytes());
        }
        else {
            dest.writeInt(0);
        }
        dest.writeInt(mRssi);
        dest.writeLong(mTimestampNanos);
    }

    private void readFromParcel(Parcel in) {
        if (in.readInt() == 1) {
            mDevice = BluetoothDevice.CREATOR.createFromParcel(in);
        }
        if (in.readInt() == 1) {
            mScanRecordCompat = ScanRecordCompat.parseFromBytes(in.createByteArray());
        }
        mRssi = in.readInt();
        mTimestampNanos = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Returns the remote bluetooth device identified by the bluetooth device address.
     */
    public BluetoothDevice getDevice() {
        return mDevice;
    }

    /**
     * Returns the scan record, which is a combination of advertisement and scan response.
     */
    @Nullable
    public ScanRecordCompat getScanRecord() {
        return mScanRecordCompat;
    }

    /**
     * Returns the received signal strength in dBm. The valid range is [-127, 127].
     */
    public int getRssi() {
        return mRssi;
    }

    /**
     * Returns timestamp since boot when the scan record was observed.
     */
    public long getTimestampNanos() {
        return mTimestampNanos;
    }

    @Override
    public int hashCode() {
        Object[] objects = {mDevice, mRssi, mScanRecordCompat, mTimestampNanos};
        return Arrays.hashCode(objects);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ScanResultCompat other = (ScanResultCompat) obj;
        return equals(mDevice, other.mDevice) && (mRssi == other.mRssi) &&
                equals(mScanRecordCompat, other.mScanRecordCompat)
                && (mTimestampNanos == other.mTimestampNanos);
    }

    @Override
    public String toString() {
        return "ScanEvent{" + "mDevice=" + mDevice + ", mScanRecord="
                + mScanRecordCompat + ", mRssi=" + mRssi + ", mTimestampNanos="
                + mTimestampNanos + '}';
    }

    private static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static final Parcelable.Creator<ScanResultCompat> CREATOR = new Creator<ScanResultCompat>() {
        @Override
        public ScanResultCompat createFromParcel(Parcel source) {
            return new ScanResultCompat(source);
        }

        @Override
        public ScanResultCompat[] newArray(int size) {
            return new ScanResultCompat[size];
        }
    };

}

