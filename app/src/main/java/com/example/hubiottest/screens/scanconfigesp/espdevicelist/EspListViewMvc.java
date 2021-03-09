package com.example.hubiottest.screens.scanconfigesp.espdevicelist;

import android.bluetooth.BluetoothDevice;

import com.example.hubiottest.screens.common.views.ObservableViewMvc;

import java.util.List;

public interface EspListViewMvc extends ObservableViewMvc<EspListViewMvc.Listener> {
    interface Listener {
        void onEspClicked(BluetoothDevice bleDevice);

        void onNavigateUpClicked();

        void onScanButtonClicked();

        void onSwitchOnOffClicked(boolean isOn);

        void onBleScanning();
    }

    void showBarLoading(boolean visible);

    void bindViewSwitchPrefer(boolean bluetoothState);

    void setStatusTurningOnOff(int bluetoothState);

    void bindEsp(List<BluetoothDevice> bleDevice);

    void showRecyclerView(boolean isEnabled);

//    void bindPairedDevices(List<BluetoothDevice> bleDevice);

    void showTextViewPaired(boolean visible);

    void showTextViewAvailable(boolean visible);
}
