package com.moko.support.d;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.d.callback.MokoScanDeviceCallback;
import com.moko.support.d.entity.DeviceInfo;
import com.moko.support.d.entity.OrderServices;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public final class MokoBleScanner {

    private MokoLeScanHandler mMokoLeScanHandler;
    private MokoScanDeviceCallback mMokoScanDeviceCallback;

    private Context mContext;

    public MokoBleScanner(Context context) {
        mContext = context;
    }

    public void startScanDevice(MokoScanDeviceCallback callback) {
        mMokoScanDeviceCallback = callback;
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            XLog.i("Start scan");
        }
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        List<ScanFilter> scanFilterList = new ArrayList<>();
        ScanFilter.Builder deviceInfoBuilder = new ScanFilter.Builder();
        deviceInfoBuilder.setServiceData(new ParcelUuid(OrderServices.SERVICE_ADV_DEVICE.getUuid()), null);
        scanFilterList.add(deviceInfoBuilder.build());
        ScanFilter.Builder triggerBuilder = new ScanFilter.Builder();
        triggerBuilder.setServiceData(new ParcelUuid(OrderServices.SERVICE_ADV_TRIGGER.getUuid()), null);
        scanFilterList.add(triggerBuilder.build());
        ScanFilter.Builder uidBuilder = new ScanFilter.Builder();
        uidBuilder.setServiceData(new ParcelUuid(OrderServices.SERVICE_ADV_UID.getUuid()), null);
        scanFilterList.add(uidBuilder.build());
        ScanFilter.Builder iBeaconBuilder = new ScanFilter.Builder();
        iBeaconBuilder.setServiceData(new ParcelUuid(OrderServices.SERVICE_ADV_IBEACON.getUuid()), null);
        scanFilterList.add(iBeaconBuilder.build());
        ScanFilter.Builder appleiBeaconBuilder = new ScanFilter.Builder();
        appleiBeaconBuilder.setManufacturerData(0x004C, null);
        scanFilterList.add(appleiBeaconBuilder.build());
        mMokoLeScanHandler = new MokoLeScanHandler(callback);
        scanner.startScan(scanFilterList, settings, mMokoLeScanHandler);
        callback.onStartScan();
    }

    public void stopScanDevice() {
        if (mMokoLeScanHandler != null && mMokoScanDeviceCallback != null) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                XLog.i("End scan");
            }
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mMokoLeScanHandler);
            mMokoScanDeviceCallback.onStopScan();
            mMokoLeScanHandler = null;
            mMokoScanDeviceCallback = null;
        }
    }

    public static class MokoLeScanHandler extends ScanCallback {

        private MokoScanDeviceCallback callback;

        public MokoLeScanHandler(MokoScanDeviceCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result != null) {
                BluetoothDevice device = result.getDevice();
                byte[] scanRecord = result.getScanRecord().getBytes();
                String name = result.getScanRecord().getDeviceName();
                int rssi = result.getRssi();
                if (scanRecord.length == 0 || rssi == 127) {
                    return;
                }
                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.name = name;
                deviceInfo.rssi = rssi;
                deviceInfo.mac = device.getAddress();
                String scanRecordStr = MokoUtils.bytesToHexString(scanRecord);
                deviceInfo.scanRecord = scanRecordStr;
                deviceInfo.scanResult = result;
                callback.onScanDevice(deviceInfo);
            }
        }
    }
}
