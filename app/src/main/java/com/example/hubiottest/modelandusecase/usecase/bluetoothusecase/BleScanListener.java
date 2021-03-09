package com.example.hubiottest.modelandusecase.usecase.bluetoothusecase;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

/**
 * Interface for BLE device scanning.
 */
public interface BleScanListener {

    /**
     * Callback to inform user BT is off so not able to start scanning.
     */
    void scanStartFailed();

    void onStatusScan(boolean statusScanning);

    /**
     * Called when any BLE peripheral will be found.
     *
     * @param device     BluetoothDevice
     * @param scanResult Scan result.
     */
    void onPeripheralFound(BluetoothDevice device, ScanResult scanResult);

    /**
     * Callback method for scan completed.
     */
    void scanCompleted();

    /**
     * Failed to scan for BLE Bluetooth Devices.
     *
     * @param e Exception
     */
    void onFailure(Exception e);
}
