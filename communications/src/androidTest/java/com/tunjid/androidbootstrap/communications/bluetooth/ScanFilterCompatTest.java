package com.tunjid.androidbootstrap.communications.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanFilter;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;

/**
 * Tests a {@link ScanFilterCompat}
 * <p>
 * Created by tj.dahunsi on 5/12/17.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@RunWith(AndroidJUnit4.class)
public class ScanFilterCompatTest {

    private ScanFilterCompat.Builder compatBuilder;
    private ScanFilter.Builder builder;

    @Before
    public void setUp() throws Exception {
        compatBuilder = new ScanFilterCompat.Builder();
        builder = new ScanFilter.Builder();
    }

    @After
    public void tearDown() throws Exception {
        compatBuilder = null;
        builder = null;
    }

    @Test
    public void getDeviceName() throws Exception {
        String name = "Device";

        ScanFilterCompat filterCompat = compatBuilder.setDeviceName(name).build();
        ScanFilter filter = builder.setDeviceName(filterCompat.getDeviceName()).build();

        assertEquals(name, filterCompat.getDeviceName());
        assertEquals(name, filter.getDeviceName());
    }

    @Test
    public void getDeviceAddress() throws Exception {
        String address = "00:43:A8:23:10:F0";

        ScanFilterCompat filterCompat = compatBuilder.setDeviceAddress(address).build();
        ScanFilter filter = builder.setDeviceAddress(filterCompat.getDeviceAddress()).build();

        assertEquals(address, filterCompat.getDeviceAddress());
        assertEquals(address, filter.getDeviceAddress());
    }

    @Test
    public void getServiceUuid() throws Exception {
        ParcelUuid serviceUuid = new ParcelUuid(UUID.randomUUID());

        ScanFilterCompat filterCompat = compatBuilder.setServiceUuid(serviceUuid).build();
        ScanFilter filter = builder.setServiceUuid(filterCompat.getServiceUuid()).build();

        assertEquals(serviceUuid, filterCompat.getServiceUuid());
        assertEquals(serviceUuid, filter.getServiceUuid());
    }

    @Test
    public void getServiceUuidMask() throws Exception {
        ParcelUuid serviceUuid = new ParcelUuid(UUID.randomUUID());
        ParcelUuid serviceUuidMask = new ParcelUuid(UUID.randomUUID());

        ScanFilterCompat filterCompat = compatBuilder.setServiceUuid(serviceUuid, serviceUuidMask).build();
        ScanFilter filter = builder.setServiceUuid(filterCompat.getServiceUuid(), filterCompat.getServiceUuidMask()).build();

        assertEquals(serviceUuid, filterCompat.getServiceUuid());
        assertEquals(serviceUuid, filter.getServiceUuid());

        assertEquals(serviceUuidMask, filterCompat.getServiceUuidMask());
        assertEquals(serviceUuidMask, filter.getServiceUuidMask());
    }

    @Test
    public void getServiceData() throws Exception {
        ParcelUuid serviceDataUuid = new ParcelUuid(UUID.randomUUID());
        byte[] serviceData = {1, 2, 3, 4};

        ScanFilterCompat filterCompat = compatBuilder.setServiceData(serviceDataUuid, serviceData).build();
        ScanFilter filter = builder.setServiceData(filterCompat.getServiceDataUuid(), filterCompat.getServiceData()).build();

        assertEquals(serviceDataUuid, filterCompat.getServiceDataUuid());
        assertEquals(serviceDataUuid, filter.getServiceDataUuid());

        assertEquals(serviceData, filterCompat.getServiceData());
        assertEquals(serviceData, filter.getServiceData());
    }

    @Test
    public void getServiceDataMask() throws Exception {
        ParcelUuid serviceDataUuid = new ParcelUuid(UUID.randomUUID());
        byte[] serviceData = {1, 2, 3, 4};
        byte[] serviceDataMask = {5, 6, 7, 8};

        ScanFilterCompat filterCompat = compatBuilder.setServiceData(serviceDataUuid, serviceData, serviceDataMask).build();
        ScanFilter filter = builder.setServiceData(filterCompat.getServiceDataUuid(), filterCompat.getServiceData(), filterCompat.getServiceDataMask()).build();

        assertEquals(serviceDataUuid, filterCompat.getServiceDataUuid());
        assertEquals(serviceDataUuid, filter.getServiceDataUuid());

        assertEquals(serviceData, filterCompat.getServiceData());
        assertEquals(serviceData, filter.getServiceData());

        assertEquals(serviceDataMask, filterCompat.getServiceDataMask());
        assertEquals(serviceDataMask, filter.getServiceDataMask());
    }

    @Test
    public void getManufacturerData() throws Exception {
        int manufacturerId = 5;
        byte[] manufacturerData = {1, 2, 3, 4};

        ScanFilterCompat filterCompat = compatBuilder.setManufacturerData(manufacturerId, manufacturerData).build();
        ScanFilter filter = builder.setManufacturerData(filterCompat.getManufacturerId(), filterCompat.getManufacturerData()).build();

        assertEquals(manufacturerId, filterCompat.getManufacturerId());
        assertEquals(manufacturerId, filter.getManufacturerId());

        assertEquals(manufacturerData, filterCompat.getManufacturerData());
        assertEquals(manufacturerData, filter.getManufacturerData());
    }

    @Test
    public void getManufacturerDataMask() throws Exception {
        int manufacturerId = 5;
        byte[] manufacturerData = {1, 2, 3, 4};
        byte[] manufacturerDataMask = {5, 6, 7, 8};

        ScanFilterCompat filterCompat = compatBuilder.setManufacturerData(manufacturerId, manufacturerData, manufacturerDataMask).build();
        ScanFilter filter = builder.setManufacturerData(filterCompat.getManufacturerId(), filterCompat.getManufacturerData(), filterCompat.getManufacturerDataMask()).build();

        assertEquals(manufacturerId, filterCompat.getManufacturerId());
        assertEquals(manufacturerId, filter.getManufacturerId());

        assertEquals(manufacturerData, filterCompat.getManufacturerData());
        assertEquals(manufacturerData, filter.getManufacturerData());

        assertEquals(manufacturerDataMask, filterCompat.getManufacturerDataMask());
        assertEquals(manufacturerDataMask, filter.getManufacturerDataMask());
    }

    @Test
    public void matches() throws Exception {

    }

}