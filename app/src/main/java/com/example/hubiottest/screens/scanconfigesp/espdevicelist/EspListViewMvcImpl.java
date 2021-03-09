package com.example.hubiottest.screens.scanconfigesp.espdevicelist;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hubiottest.R;
import com.example.hubiottest.screens.common.ViewMvcFactory;
import com.example.hubiottest.screens.common.toolbar.ToolbarViewMvc;
import com.example.hubiottest.screens.common.views.BaseObservableViewMvc;

import java.util.ArrayList;
import java.util.List;

public class EspListViewMvcImpl extends BaseObservableViewMvc<EspListViewMvc.Listener>
        implements EspListViewMvc,
        EspRecyclerAdapter.Listener {
    private ProgressBar barLoadingScanning;
    private Switch switchOnOff;
    private TextView tvStatusOnOff;
    private TextView tvGuideBluetooth;
//    private TextView tvPairedDevices;
    private TextView tvAvailableDevices;

    private RecyclerView recyclerViewEspList;
//    private RecyclerView recyclerPairedDevices;

    private EspRecyclerAdapter espAdapter;
    private EspRecyclerAdapter pairedAdapter;

    private ToolbarViewMvc mToolbarViewMvc;
    private Toolbar mToolbar;


    public EspListViewMvcImpl(LayoutInflater inflater, @Nullable ViewGroup parent, ViewMvcFactory viewMvcFactory) {
        View view = inflater.inflate(R.layout.fragment_listespdevices, parent, false);
        setRootView(view);

        barLoadingScanning = findViewById(R.id.bar_loading_scanning);
        switchOnOff = findViewById(R.id.btn_switch_on_off_bluetooth);
        tvStatusOnOff = findViewById(R.id.btn_status_on_off_bluetooth);
        tvGuideBluetooth = findViewById(R.id.tv_guide_bluetooth);
//        tvPairedDevices = findViewById(R.id.tv_paired_devices);
//        tvPairedDevices.setVisibility(View.GONE);
        tvAvailableDevices = findViewById(R.id.tv_available_devices);
        tvAvailableDevices.setVisibility(View.GONE);

        recyclerViewEspList = findViewById(R.id.rv_esp_list);
        recyclerViewEspList.setLayoutManager(new LinearLayoutManager(getContext()));

//        recyclerPairedDevices = findViewById(R.id.rv_paired_devices);
//        recyclerPairedDevices.setLayoutManager(new LinearLayoutManager(getContext()));

        espAdapter = new EspRecyclerAdapter(this, viewMvcFactory);
        recyclerViewEspList.setAdapter(espAdapter);
        pairedAdapter = new EspRecyclerAdapter(this, viewMvcFactory);
//        recyclerPairedDevices.setAdapter(pairedAdapter);

        mToolbar = findViewById(R.id.toolbar);
        mToolbarViewMvc = viewMvcFactory.getToolbarViewMvc(parent);
        initToolbar();

        switchOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Listener listener : getListeners()) {
                    listener.onSwitchOnOffClicked(switchOnOff.isChecked());
                }
            }
        });
    }

    private void initToolbar() {
        mToolbar.addView(mToolbarViewMvc.getRootView());
        mToolbarViewMvc.setTitle("Scan Esp");
    }

    private void showButtonScan() {
        mToolbarViewMvc.enableScanButtonAndListen(new ToolbarViewMvc.ScanButtonClickListener() {
            @Override
            public void onScanButtonClicked() {
                for (Listener listener : getListeners()) {
                    listener.onScanButtonClicked();
                }
            }
        });
    }

    private void disableButtonScan() {
        mToolbarViewMvc.disableScanButton();
    }

    public void bindEsp(List<BluetoothDevice> bleDevices) {
        espAdapter.bindEsps(bleDevices);
    }

//    @Override
//    public void bindPairedDevices(List<BluetoothDevice> bluetoothDevices) {
//        pairedAdapter.bindEsps(bluetoothDevices);
//    }

    @Override
    public void showTextViewPaired(boolean visible) {
//        tvPairedDevices.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showTextViewAvailable(boolean visible) {
        tvAvailableDevices.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showRecyclerView(boolean isEnabled) {
        recyclerViewEspList.setVisibility(isEnabled ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onEspClicked(BluetoothDevice bleDevice) {
        for (Listener listener : getListeners()) {
            listener.onEspClicked(bleDevice);
        }
    }

    @Override
    public void showBarLoading(boolean visible) {
        barLoadingScanning.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mToolbarViewMvc.setScanButton(visible);
    }

    @Override
    public void bindViewSwitchPrefer(boolean isEnabled) {
        tvStatusOnOff.setText(isEnabled ? "On" : "Off");
        switchOnOff.setChecked(isEnabled);
        barLoadingScanning.setVisibility(View.INVISIBLE);
    }


    @Override
    public void setStatusTurningOnOff(int bluetoothState) {
        switch (bluetoothState) {
            case BluetoothAdapter.STATE_ON:
                tvGuideBluetooth.setText(R.string.guide_when_on_bluetooth);
                switchOnOff.setChecked(true);
                tvStatusOnOff.setText("On");
                showButtonScan();
                recyclerViewEspList.setVisibility(View.VISIBLE);
                espAdapter.bindEsps(new ArrayList<>());
                for (Listener listener: getListeners()){
                    listener.onBleScanning();
                }
                break;
            case BluetoothAdapter.STATE_OFF:
                tvGuideBluetooth.setText(R.string.guide_when_off_bluetooth);
                switchOnOff.setChecked(false);
                tvStatusOnOff.setText("Off");
                showTextViewAvailable(false);
                showBarLoading(false);
                recyclerViewEspList.setVisibility(View.INVISIBLE);
                disableButtonScan();
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                tvGuideBluetooth.setText(R.string.turning_on_bluetooth);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                tvGuideBluetooth.setText(R.string.turning_off_bluetooth);
                break;
        }
    }


}
