package com.example.hubiottest.screens.scanconfigesp.espdevicelist.esplistitem;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hubiottest.R;
import com.example.hubiottest.screens.common.views.BaseObservableViewMvc;

public class EspItemViewMvcImpl extends BaseObservableViewMvc<EspItemViewMvc.Listener>
        implements EspItemViewMvc {
    private TextView deviceName;
    private TextView deviceAddress;

    private BluetoothDevice mBleDevice;

    public EspItemViewMvcImpl(LayoutInflater inflater, ViewGroup parent) {
        setRootView(inflater.inflate(R.layout.layout_a_esp, parent, false));
        deviceName = findViewById(R.id.device_name);
        deviceAddress = findViewById(R.id.device_address);

        getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Listener listener : getListeners()) {
                    listener.onEspClicked(mBleDevice);
                }
            }
        });
    }

    @Override
    public void bindEsp(BluetoothDevice bleDevice) {
        mBleDevice = bleDevice;
        deviceName.setText((mBleDevice.getName() == null) ? "NONE" : mBleDevice.getName().toUpperCase());
        deviceAddress.setText(mBleDevice.getAddress().toUpperCase());
    }
}
