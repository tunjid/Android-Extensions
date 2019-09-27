package com.tunjid.androidx.communications.bluetooth;


import android.os.ParcelUuid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Static helper methods and constants to decode the ParcelUuid of remote devices.
 */
@SuppressWarnings("WeakerAccess")
final class BluetoothUuid {


    public static final ParcelUuid BASE_UUID =
            ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");
    /**
     * Length of bytes for 16 bit UUID
     */
    public static final int UUID_BYTES_16_BIT = 2;
    /**
     * Length of bytes for 32 bit UUID
     */
    public static final int UUID_BYTES_32_BIT = 4;
    /**
     * Length of bytes for 128 bit UUID
     */
    public static final int UUID_BYTES_128_BIT = 16;


    /**
     * Parse UUID from bytes. The {@code uuidBytes} can represent a 16-bit, 32-bit or 128-bit UUID,
     * but the returned UUID is always in 128-bit format.
     * Note UUID is little endian in Bluetooth.
     *
     * @param uuidBytes Byte representation of uuid.
     * @return {@link ParcelUuid} parsed from bytes.
     * @throws IllegalArgumentException If the {@code uuidBytes} cannot be parsed.
     */
    public static ParcelUuid parseUuidFrom(byte[] uuidBytes) {
        if (uuidBytes == null) {
            throw new IllegalArgumentException("uuidBytes cannot be null");
        }
        int length = uuidBytes.length;
        if (length != UUID_BYTES_16_BIT && length != UUID_BYTES_32_BIT &&
                length != UUID_BYTES_128_BIT) {
            throw new IllegalArgumentException("uuidBytes length invalid - " + length);
        }
        // Construct a 128 bit UUID.
        if (length == UUID_BYTES_128_BIT) {
            ByteBuffer buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN);
            long msb = buf.getLong(8);
            long lsb = buf.getLong(0);
            return new ParcelUuid(new UUID(msb, lsb));
        }
        // For 16 bit and 32 bit UUID we need to convert them to 128 bit value.
        // 128_bit_value = uuid * 2^96 + BASE_UUID
        long shortUuid;
        if (length == UUID_BYTES_16_BIT) {
            shortUuid = uuidBytes[0] & 0xFF;
            shortUuid += (uuidBytes[1] & 0xFF) << 8;
        }
        else {
            shortUuid = uuidBytes[0] & 0xFF;
            shortUuid += (uuidBytes[1] & 0xFF) << 8;
            shortUuid += (uuidBytes[2] & 0xFF) << 16;
            shortUuid += (uuidBytes[3] & 0xFF) << 24;
        }
        long msb = BASE_UUID.getUuid().getMostSignificantBits() + (shortUuid << 32);
        long lsb = BASE_UUID.getUuid().getLeastSignificantBits();
        return new ParcelUuid(new UUID(msb, lsb));
    }


//    /**
//     * Check whether the given parcelUuid can be converted to 16 bit bluetooth uuid.
//     *
//     * @return true if the parcelUuid can be converted to 16 bit uuid, false otherwise.
//     */
//    public static boolean is16BitUuid(ParcelUuid parcelUuid) {
//        UUID uuid = parcelUuid.getUuid();
//        //noinspection SimplifiableIfStatement
//        if (uuid.getLeastSignificantBits() != BASE_UUID.getUuid().getLeastSignificantBits()) {
//            return false;
//        }
//        return ((uuid.getMostSignificantBits() & 0xFFFF0000FFFFFFFFL) == 0x1000L);
//    }

//    /**
//     * Check whether the given parcelUuid can be converted to 32 bit bluetooth uuid.
//     *
//     * @return true if the parcelUuid can be converted to 32 bit uuid, false otherwise.
//     */
//    public static boolean is32BitUuid(ParcelUuid parcelUuid) {
//        UUID uuid = parcelUuid.getUuid();
//        if (uuid.getLeastSignificantBits() != BASE_UUID.getUuid().getLeastSignificantBits()) {
//            return false;
//        }
//        //noinspection SimplifiableIfStatement
//        if (is16BitUuid(parcelUuid)) {
//            return false;
//        }
//        return ((uuid.getMostSignificantBits() & 0xFFFFFFFFL) == 0x1000L);
//    }
}
