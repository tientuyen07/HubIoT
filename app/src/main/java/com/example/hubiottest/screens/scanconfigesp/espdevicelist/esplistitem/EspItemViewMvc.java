package com.example.hubiottest.screens.scanconfigesp.espdevicelist.esplistitem;

import android.bluetooth.BluetoothDevice;

import com.example.hubiottest.screens.common.views.ObservableViewMvc;

public interface EspItemViewMvc extends ObservableViewMvc<EspItemViewMvc.Listener> {
    public interface Listener{
        void onEspClicked(BluetoothDevice device);
    }

    void bindEsp(BluetoothDevice bleDevice);
}
