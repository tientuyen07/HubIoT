package com.example.hubiottest.modelandusecase.usecase.bluetoothusecase;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.example.hubiottest.common.BaseObservable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnOffScanBluetoothUseCase extends BaseObservable<OnOffScanBluetoothUseCase.Listener> {

    public interface Listener {
        void isBluetoothEnabled(boolean isEnabled);

        void onNotSupportBluetooth();

//        void onPairedDeviceFetched(Set<BluetoothDevice> pairedDevices);
    }

    private static final long SCAN_TIME_OUT = 12800;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private PackageManager mPackageManager;
    private BleScanListener mBleScanListener;
    private BluetoothLeScanner mBluetoothLeScanner;

    private Handler mHandler;
    private Map<String, BluetoothDevice> listDevices = new HashMap<>();
//    private Set<BluetoothDevice> pairedDevices;

    private boolean mIsScanning = false;

    private boolean hasBLE;

    public OnOffScanBluetoothUseCase(BluetoothManager bluetoothManager, PackageManager packageManager, BleScanListener bleScannerListener) {
        mBluetoothManager = bluetoothManager;
        mPackageManager = packageManager;
        mBleScanListener = bleScannerListener;
        mHandler = new Handler();
    }

    public void isSupportedAndIsOn() {
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }

        if (!mPackageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            for (Listener listener : getListeners()) {
                listener.onNotSupportBluetooth();
            }
        }

        hasBLE = mPackageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

        if (mBluetoothAdapter == null) {
            for (Listener listener : getListeners()) {
                listener.onNotSupportBluetooth();
            }
        }
        // Get paired devices
//        pairedDevices = mBluetoothAdapter.getBondedDevices();
//        for (Listener listener : getListeners()) {
//            listener.onPairedDeviceFetched(pairedDevices);
//        }
        // Init BluetoothLeScanner
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
//        mBluetoothLeScanner = BluetoothLeScannerCompat.getScanner();

        for (Listener listener : getListeners()) {
            listener.isBluetoothEnabled(mBluetoothAdapter.isEnabled());
        }
    }

    public void enableBluetooth() {
        mBluetoothAdapter.enable();
    }

    public void disableBluetooth() {
        mBluetoothAdapter.disable();
    }

    public void scanBleDevice() {
        if (!mIsScanning) {
            listDevices.clear();
            List<ScanFilter> filterList = new ArrayList<>();
            ScanSettings settings = new ScanSettings.Builder()
                    .setReportDelay(1000)
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();


            // Listener started scan
            mIsScanning = true;
            mBleScanListener.onStatusScan(true);

            mBluetoothLeScanner.startScan(filterList, settings, scanCallback);

            mHandler.postDelayed(stopScanTask, SCAN_TIME_OUT);
        } else {
            mHandler.post(stopScanTask);
        }
    }

    public void checkLocationPermission() {

    }

    // Create a BroadcastReceiver for Bluetooth scan ACTION_FOUND.
    private final BroadcastReceiver btScanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

            }
        }
    };


    /**
     * ScanCallback to get scanned Peripheral.
     */
    private final ScanCallback scanCallback = new ScanCallback() {

        @Override
        @RequiresPermission(Manifest.permission.BLUETOOTH)
        public void onScanResult(int callbackType, ScanResult result) {
            String deviceAddress = result.getDevice().getAddress();
            String deviceName = (result.getDevice().getName() == null) ? "NONE" : result.getDevice().getName();
            BluetoothDevice temp = result.getDevice();

//            if (temp != null && !TextUtils.isEmpty(deviceName)) {
            if (temp != null) {
                Log.d("TESTTTTT", "========== Device Found : " + deviceAddress);
//                if (!listDevices.containsKey(deviceAddress) && !pairedDevices.contains(temp)) {
                if (!listDevices.containsKey(deviceAddress)) {
                    listDevices.put(deviceAddress, result.getDevice());
                    mBleScanListener.onPeripheralFound(result.getDevice(), result);
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d("TESTTTTTT", "onBatchScanResults()");
            for (final ScanResult result : results){
                String deviceAddress = result.getDevice().getAddress();
                String deviceName = (result.getDevice().getName() == null) ? "NONE" : result.getDevice().getName();
                BluetoothDevice temp = result.getDevice();

//            if (temp != null && !TextUtils.isEmpty(deviceName)) {
                if (temp != null) {
                    Log.d("TESTTTTT", "========== Device Found : " + deviceAddress);
//                if (!listDevices.containsKey(deviceAddress) && !pairedDevices.contains(temp)) {
                    if (!listDevices.containsKey(deviceAddress)) {
                        listDevices.put(deviceAddress, result.getDevice());
                        mBleScanListener.onPeripheralFound(result.getDevice(), result);
                    }
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("TESTTTTTT", "onScanFailed, errorCode:" + errorCode);
            mBleScanListener.onFailure(new RuntimeException("BLE scanning failed with error code : " + errorCode));
        }
    };

    private Runnable stopScanTask = new Runnable() {

        @Override
        @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
        public void run() {
            stopScan();
            mBleScanListener.onStatusScan(false);
        }
    };

    /**
     * This method is used to start BLE scan.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH})
    public void stopScan() {

        Log.d("TESTTTTTT", "Stop BLE device scan");
        mHandler.removeCallbacks(stopScanTask);

        if (mBluetoothLeScanner != null && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            try {
                mBluetoothLeScanner.stopScan(scanCallback);
            } catch (Exception e) {
                Log.e("STOP SCAN FAILLLLLLLLLLLLLLLLL", e.toString());
                e.printStackTrace();
            }
        }
        mIsScanning = false;
        mBleScanListener.scanCompleted();
    }
}
