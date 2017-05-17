package com.tunjid.androidbootstrap.communications.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Criteria for filtering result from Bluetooth LE scans. A {@link ScanFilterCompat} allows clients to
 * restrict scan results to only those that are of interest to them.
 * <p>
 * Current filtering on the following fields are supported:
 * <li>Service UUIDs which identify the bluetooth gatt services running on the device.
 * <li>Name of remote Bluetooth LE device.
 * <li>Mac address of the remote device.
 * <li>Service data which is the data associated with a service.
 * <li>Manufacturer specific data which is the data associated with a particular manufacturer.
 *
 * @see BLEScanner
 * @see ScanResultCompat
 */
@SuppressWarnings("WeakerAccess")
public final class ScanFilterCompat implements Parcelable {

    @Nullable
    private final String mDeviceName;

    @Nullable
    private final String mDeviceAddress;

    @Nullable
    private final ParcelUuid mServiceUuid;
    @Nullable
    private final ParcelUuid mServiceUuidMask;

    @Nullable
    private final ParcelUuid mServiceDataUuid;
    @Nullable
    private final byte[] mServiceData;
    @Nullable
    private final byte[] mServiceDataMask;

    private final int mManufacturerId;
    @Nullable
    private final byte[] mManufacturerData;
    @Nullable
    private final byte[] mManufacturerDataMask;

    private ScanFilterCompat(@Nullable String name, @Nullable String deviceAddress,
                             @Nullable ParcelUuid uuid, @Nullable ParcelUuid uuidMask,
                             @Nullable ParcelUuid serviceDataUuid,
                             @Nullable byte[] serviceData, @Nullable byte[] serviceDataMask,
                             int manufacturerId,
                             @Nullable byte[] manufacturerData, @Nullable byte[] manufacturerDataMask) {
        mDeviceName = name;
        mServiceUuid = uuid;
        mServiceUuidMask = uuidMask;
        mDeviceAddress = deviceAddress;
        mServiceDataUuid = serviceDataUuid;
        mServiceData = serviceData;
        mServiceDataMask = serviceDataMask;
        mManufacturerId = manufacturerId;
        mManufacturerData = manufacturerData;
        mManufacturerDataMask = manufacturerDataMask;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mDeviceName == null ? 0 : 1);
        if (mDeviceName != null) {
            dest.writeString(mDeviceName);
        }
        dest.writeInt(mDeviceAddress == null ? 0 : 1);
        if (mDeviceAddress != null) {
            dest.writeString(mDeviceAddress);
        }
        dest.writeInt(mServiceUuid == null ? 0 : 1);
        if (mServiceUuid != null) {
            dest.writeParcelable(mServiceUuid, flags);
            dest.writeInt(mServiceUuidMask == null ? 0 : 1);
            if (mServiceUuidMask != null) {
                dest.writeParcelable(mServiceUuidMask, flags);
            }
        }
        dest.writeInt(mServiceDataUuid == null ? 0 : 1);
        if (mServiceDataUuid != null) {
            dest.writeParcelable(mServiceDataUuid, flags);
            dest.writeInt(mServiceData == null ? 0 : 1);
            if (mServiceData != null) {
                dest.writeInt(mServiceData.length);
                dest.writeByteArray(mServiceData);

                dest.writeInt(mServiceDataMask == null ? 0 : 1);
                if (mServiceDataMask != null) {
                    dest.writeInt(mServiceDataMask.length);
                    dest.writeByteArray(mServiceDataMask);
                }
            }
        }
        dest.writeInt(mManufacturerId);
        dest.writeInt(mManufacturerData == null ? 0 : 1);
        if (mManufacturerData != null) {
            dest.writeInt(mManufacturerData.length);
            dest.writeByteArray(mManufacturerData);

            dest.writeInt(mManufacturerDataMask == null ? 0 : 1);
            if (mManufacturerDataMask != null) {
                dest.writeInt(mManufacturerDataMask.length);
                dest.writeByteArray(mManufacturerDataMask);
            }
        }
    }

    /**
     * A {@link android.os.Parcelable.Creator} to create {@link ScanFilterCompat} from parcel.
     */
    public static final Creator<ScanFilterCompat>
            CREATOR = new Creator<ScanFilterCompat>() {

        @Override
        public ScanFilterCompat[] newArray(int size) {
            return new ScanFilterCompat[size];
        }

        @Override
        public ScanFilterCompat createFromParcel(Parcel in) {
            Builder builder = new Builder();
            if (in.readInt() == 1) {
                builder.setDeviceName(in.readString());
            }
            if (in.readInt() == 1) {
                builder.setDeviceAddress(in.readString());
            }
            if (in.readInt() == 1) {
                ParcelUuid uuid = in.readParcelable(ParcelUuid.class.getClassLoader());
                builder.setServiceUuid(uuid);
                if (in.readInt() == 1) {
                    ParcelUuid uuidMask = in.readParcelable(
                            ParcelUuid.class.getClassLoader());
                    builder.setServiceUuid(uuid, uuidMask);
                }
            }
            if (in.readInt() == 1) {
                ParcelUuid servcieDataUuid =
                        in.readParcelable(ParcelUuid.class.getClassLoader());
                if (in.readInt() == 1) {
                    int serviceDataLength = in.readInt();
                    byte[] serviceData = new byte[serviceDataLength];
                    in.readByteArray(serviceData);
                    if (in.readInt() == 0) {
                        builder.setServiceData(servcieDataUuid, serviceData);
                    }
                    else {
                        int serviceDataMaskLength = in.readInt();
                        byte[] serviceDataMask = new byte[serviceDataMaskLength];
                        in.readByteArray(serviceDataMask);
                        builder.setServiceData(
                                servcieDataUuid, serviceData, serviceDataMask);
                    }
                }
            }

            int manufacturerId = in.readInt();
            if (in.readInt() == 1) {
                int manufacturerDataLength = in.readInt();
                byte[] manufacturerData = new byte[manufacturerDataLength];
                in.readByteArray(manufacturerData);
                if (in.readInt() == 0) {
                    builder.setManufacturerData(manufacturerId, manufacturerData);
                }
                else {
                    int manufacturerDataMaskLength = in.readInt();
                    byte[] manufacturerDataMask = new byte[manufacturerDataMaskLength];
                    in.readByteArray(manufacturerDataMask);
                    builder.setManufacturerData(manufacturerId, manufacturerData,
                            manufacturerDataMask);
                }
            }

            return builder.build();
        }
    };

    /**
     * Returns the filter set the device name field of Bluetooth advertisement data.
     */
    @Nullable
    String getDeviceName() {
        return mDeviceName;
    }

    /**
     * Returns the filter set on the service uuid.
     */
    @Nullable
    ParcelUuid getServiceUuid() {
        return mServiceUuid;
    }

    @Nullable
    ParcelUuid getServiceUuidMask() {
        return mServiceUuidMask;
    }

    @Nullable
    String getDeviceAddress() {
        return mDeviceAddress;
    }

    @Nullable
    byte[] getServiceData() {
        return mServiceData;
    }

    @Nullable
    byte[] getServiceDataMask() {
        return mServiceDataMask;
    }

    @Nullable
    ParcelUuid getServiceDataUuid() {
        return mServiceDataUuid;
    }

    /**
     * Returns the manufacturer id. -1 if the manufacturer filter is not set.
     */
    int getManufacturerId() {
        return mManufacturerId;
    }

    @Nullable
    byte[] getManufacturerData() {
        return mManufacturerData;
    }

    @Nullable
    byte[] getManufacturerDataMask() {
        return mManufacturerDataMask;
    }

    /**
     * Check if the scan filter matches a {@code scanResult}. A scan result is considered as a match
     * if it matches all the field filters.
     */
    boolean matches(ScanResultCompat scanResultCompat) {
        if (scanResultCompat == null) {
            return false;
        }
        BluetoothDevice device = scanResultCompat.getDevice();
        // Device match.
        if (mDeviceAddress != null
                && (device == null || !mDeviceAddress.equals(device.getAddress()))) {
            return false;
        }

        ScanRecordCompat scanRecordCompat = scanResultCompat.getScanRecord();

        // Scan record is null but there exist filters on it.
        if (scanRecordCompat == null
                && (mDeviceName != null || mServiceUuid != null || mManufacturerData != null
                || mServiceData != null)) {
            return false;
        }

        if (scanRecordCompat == null) return false;

        // Local name match.
        if (mDeviceName != null && !mDeviceName.equals(scanRecordCompat.getDeviceName())) {
            return false;
        }

        // UUID match.
        if (mServiceUuid != null && !matchesServiceUuids(mServiceUuid, mServiceUuidMask,
                scanRecordCompat.getServiceUuids())) {
            return false;
        }

        // Service data match
        if (mServiceDataUuid != null) {
            if (!matchesPartialData(mServiceData, mServiceDataMask,
                    scanRecordCompat.getServiceData(mServiceDataUuid))) {
                return false;
            }
        }

        // Manufacturer data match.
        if (mManufacturerId >= 0) {
            if (!matchesPartialData(mManufacturerData, mManufacturerDataMask,
                    scanRecordCompat.getManufacturerSpecificData(mManufacturerId))) {
                return false;
            }
        }
        // All filters match.
        return true;
    }

    // Check if the uuid pattern is contained in a list of parcel uuids.
    private boolean matchesServiceUuids(ParcelUuid uuid, ParcelUuid parcelUuidMask,
                                        List<ParcelUuid> uuids) {
        if (uuid == null) {
            return true;
        }
        if (uuids == null) {
            return false;
        }

        for (ParcelUuid parcelUuid : uuids) {
            UUID uuidMask = parcelUuidMask == null ? null : parcelUuidMask.getUuid();
            if (matchesServiceUuid(uuid.getUuid(), uuidMask, parcelUuid.getUuid())) {
                return true;
            }
        }
        return false;
    }

    // Check if the uuid pattern matches the particular service uuid.
    private boolean matchesServiceUuid(UUID uuid, UUID mask, UUID data) {
        if (mask == null) {
            return uuid.equals(data);
        }
        //noinspection SimplifiableIfStatement
        if ((uuid.getLeastSignificantBits() & mask.getLeastSignificantBits()) !=
                (data.getLeastSignificantBits() & mask.getLeastSignificantBits())) {
            return false;
        }
        return ((uuid.getMostSignificantBits() & mask.getMostSignificantBits()) ==
                (data.getMostSignificantBits() & mask.getMostSignificantBits()));
    }

    // Check whether the data pattern matches the parsed data.
    private boolean matchesPartialData(byte[] data, byte[] dataMask, byte[] parsedData) {
        if (parsedData == null || parsedData.length < data.length) {
            return false;
        }
        if (dataMask == null) {
            for (int i = 0; i < data.length; ++i) {
                if (parsedData[i] != data[i]) {
                    return false;
                }
            }
            return true;
        }
        for (int i = 0; i < data.length; ++i) {
            if ((dataMask[i] & parsedData[i]) != (dataMask[i] & data[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "BluetoothLeScanFilter [mDeviceName=" + mDeviceName + ", mDeviceAddress="
                + mDeviceAddress
                + ", mUuid=" + mServiceUuid + ", mUuidMask=" + mServiceUuidMask
                + ", mServiceDataUuid=" + String.valueOf(mServiceDataUuid) + ", mServiceData="
                + Arrays.toString(mServiceData) + ", mServiceDataMask="
                + Arrays.toString(mServiceDataMask) + ", mManufacturerId=" + mManufacturerId
                + ", mManufacturerData=" + Arrays.toString(mManufacturerData)
                + ", mManufacturerDataMask=" + Arrays.toString(mManufacturerDataMask) + "]";
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{mDeviceName, mDeviceAddress, mManufacturerId,
                Arrays.hashCode(mManufacturerData),
                Arrays.hashCode(mManufacturerDataMask),
                mServiceDataUuid,
                Arrays.hashCode(mServiceData),
                Arrays.hashCode(mServiceDataMask),
                mServiceUuid, mServiceUuidMask});
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ScanFilterCompat other = (ScanFilterCompat) obj;
        return equals(mDeviceName, other.mDeviceName) &&
                equals(mDeviceAddress, other.mDeviceAddress) &&
                mManufacturerId == other.mManufacturerId &&
                deepEquals(mManufacturerData, other.mManufacturerData) &&
                deepEquals(mManufacturerDataMask, other.mManufacturerDataMask) &&
                equals(mServiceDataUuid, other.mServiceDataUuid) &&
                deepEquals(mServiceData, other.mServiceData) &&
                deepEquals(mServiceDataMask, other.mServiceDataMask) &&
                equals(mServiceUuid, other.mServiceUuid) &&
                equals(mServiceUuidMask, other.mServiceUuidMask);
    }

    /**
     * Builder class for {@link ScanFilterCompat}.
     */
    public static final class Builder {

        private String mDeviceName;
        private String mDeviceAddress;

        private ParcelUuid mServiceUuid;
        private ParcelUuid mUuidMask;

        private ParcelUuid mServiceDataUuid;
        private byte[] mServiceData;
        private byte[] mServiceDataMask;

        private int mManufacturerId = -1;
        private byte[] mManufacturerData;
        private byte[] mManufacturerDataMask;

        private Builder() {

        }

        /**
         * Set filter on device name.
         */
        public Builder setDeviceName(String deviceName) {
            mDeviceName = deviceName;
            return this;
        }

        /**
         * Set filter on device address.
         *
         * @param deviceAddress The device Bluetooth address for the filter. It needs to be in the
         *                      format of "01:02:03:AB:CD:EF". The device address can be validated using
         *                      {@link BluetoothAdapter#checkBluetoothAddress}.
         * @throws IllegalArgumentException If the {@code deviceAddress} is invalid.
         */
        public Builder setDeviceAddress(String deviceAddress) {
            if (deviceAddress != null && !BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
                throw new IllegalArgumentException("invalid device address " + deviceAddress);
            }
            mDeviceAddress = deviceAddress;
            return this;
        }

        /**
         * Set filter on service uuid.
         */
        public Builder setServiceUuid(ParcelUuid serviceUuid) {
            mServiceUuid = serviceUuid;
            mUuidMask = null; // clear uuid mask
            return this;
        }

        /**
         * Set filter on partial service uuid. The {@code uuidMask} is the bit mask for the
         * {@code serviceUuid}. Set any bit in the mask to 1 to indicate a match is needed for the
         * bit in {@code serviceUuid}, and 0 to ignore that bit.
         *
         * @throws IllegalArgumentException If {@code serviceUuid} is {@code null} but
         *                                  {@code uuidMask} is not {@code null}.
         */
        public Builder setServiceUuid(ParcelUuid serviceUuid, ParcelUuid uuidMask) {
            if (mUuidMask != null && mServiceUuid == null) {
                throw new IllegalArgumentException("uuid is null while uuidMask is not null!");
            }
            mServiceUuid = serviceUuid;
            mUuidMask = uuidMask;
            return this;
        }

        /**
         * Set filtering on service data.
         *
         * @throws IllegalArgumentException If {@code serviceDataUuid} is null.
         */
        public Builder setServiceData(ParcelUuid serviceDataUuid, byte[] serviceData) {
            if (serviceDataUuid == null) {
                throw new IllegalArgumentException("serviceDataUuid is null");
            }
            mServiceDataUuid = serviceDataUuid;
            mServiceData = serviceData;
            mServiceDataMask = null; // clear service data mask
            return this;
        }

        /**
         * Set partial filter on service data. For any bit in the mask, set it to 1 if it needs to
         * match the one in service data, otherwise set it to 0 to ignore that bit.
         * <p>
         * The {@code serviceDataMask} must have the same length of the {@code serviceData}.
         *
         * @throws IllegalArgumentException If {@code serviceDataUuid} is null or
         *                                  {@code serviceDataMask} is {@code null} while {@code serviceData} is not or
         *                                  {@code serviceDataMask} and {@code serviceData} has different length.
         */
        public Builder setServiceData(ParcelUuid serviceDataUuid,
                                      byte[] serviceData, byte[] serviceDataMask) {
            if (serviceDataUuid == null) {
                throw new IllegalArgumentException("serviceDataUuid is null");
            }
            if (mServiceDataMask != null) {
                if (mServiceData == null) {
                    throw new IllegalArgumentException(
                            "serviceData is null while serviceDataMask is not null");
                }
                // Since the mServiceDataMask is a bit mask for mServiceData, the lengths of the two
                // byte array need to be the same.
                if (mServiceData.length != mServiceDataMask.length) {
                    throw new IllegalArgumentException(
                            "size mismatch for service data and service data mask");
                }
            }
            mServiceDataUuid = serviceDataUuid;
            mServiceData = serviceData;
            mServiceDataMask = serviceDataMask;
            return this;
        }

        /**
         * Set filter on on manufacturerData. A negative manufacturerId is considered as invalid id.
         * <p>
         * Note the first two bytes of the {@code manufacturerData} is the manufacturerId.
         *
         * @throws IllegalArgumentException If the {@code manufacturerId} is invalid.
         */
        public Builder setManufacturerData(int manufacturerId, byte[] manufacturerData) {
            if (manufacturerData != null && manufacturerId < 0) {
                throw new IllegalArgumentException("invalid manufacture id");
            }
            mManufacturerId = manufacturerId;
            mManufacturerData = manufacturerData;
            mManufacturerDataMask = null; // clear manufacturer data mask
            return this;
        }

        /**
         * Set filter on partial manufacture data. For any bit in the mask, set it the 1 if it needs
         * to match the one in manufacturer data, otherwise set it to 0.
         * <p>
         * The {@code manufacturerDataMask} must have the same length of {@code manufacturerData}.
         *
         * @throws IllegalArgumentException If the {@code manufacturerId} is invalid, or
         *                                  {@code manufacturerData} is null while {@code manufacturerDataMask} is not,
         *                                  or {@code manufacturerData} and {@code manufacturerDataMask} have different
         *                                  length.
         */
        public Builder setManufacturerData(int manufacturerId, byte[] manufacturerData,
                                           byte[] manufacturerDataMask) {
            if (manufacturerData != null && manufacturerId < 0) {
                throw new IllegalArgumentException("invalid manufacture id");
            }
            if (mManufacturerDataMask != null) {
                if (mManufacturerData == null) {
                    throw new IllegalArgumentException(
                            "manufacturerData is null while manufacturerDataMask is not null");
                }
                // Since the mManufacturerDataMask is a bit mask for mManufacturerData, the lengths
                // of the two byte array need to be the same.
                if (mManufacturerData.length != mManufacturerDataMask.length) {
                    throw new IllegalArgumentException(
                            "size mismatch for manufacturerData and manufacturerDataMask");
                }
            }
            mManufacturerId = manufacturerId;
            mManufacturerData = manufacturerData;
            mManufacturerDataMask = manufacturerDataMask;
            return this;
        }

        /**
         * Build {@link ScanFilterCompat}.
         *
         * @throws IllegalArgumentException If the filter cannot be built.
         */
        public ScanFilterCompat build() {
            return new ScanFilterCompat(mDeviceName, mDeviceAddress,
                    mServiceUuid, mUuidMask,
                    mServiceDataUuid, mServiceData, mServiceDataMask,
                    mManufacturerId, mManufacturerData, mManufacturerDataMask);
        }
    }

    private static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    private static boolean deepEquals(Object a, Object b) {
        return a == b || !(a == null || b == null) && deepEquals0(a, b);
    }

    private static boolean deepEquals0(Object e1, Object e2) {
        Class<?> cl1 = e1.getClass().getComponentType();
        Class<?> cl2 = e2.getClass().getComponentType();

        if (cl1 != cl2) {
            return false;
        }
        if (e1 instanceof Object[])
            return Arrays.deepEquals((Object[]) e1, (Object[]) e2);
        else if (cl1 == byte.class)
            return Arrays.equals((byte[]) e1, (byte[]) e2);
        else if (cl1 == short.class)
            return Arrays.equals((short[]) e1, (short[]) e2);
        else if (cl1 == int.class)
            return Arrays.equals((int[]) e1, (int[]) e2);
        else if (cl1 == long.class)
            return Arrays.equals((long[]) e1, (long[]) e2);
        else if (cl1 == char.class)
            return Arrays.equals((char[]) e1, (char[]) e2);
        else if (cl1 == float.class)
            return Arrays.equals((float[]) e1, (float[]) e2);
        else if (cl1 == double.class)
            return Arrays.equals((double[]) e1, (double[]) e2);
        else if (cl1 == boolean.class)
            return Arrays.equals((boolean[]) e1, (boolean[]) e2);
        else
            return e1.equals(e2);
    }
}
