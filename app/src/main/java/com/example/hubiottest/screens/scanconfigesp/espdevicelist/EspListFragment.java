package com.example.hubiottest.screens.scanconfigesp.espdevicelist;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.hubiottest.controllers.fragments.AbstractFragment;
import com.example.hubiottest.modelandusecase.usecase.bluetoothusecase.BleScanListener;
import com.example.hubiottest.modelandusecase.usecase.bluetoothusecase.BluetoothLeService;
import com.example.hubiottest.modelandusecase.usecase.bluetoothusecase.OnOffScanBluetoothUseCase;
import com.example.hubiottest.screens.common.dialogs.DialogsManager;
import com.example.hubiottest.screens.common.permissions.PermissionsHelper;
import com.example.hubiottest.screens.common.screensnavigator.ScreensNavigator;
import com.example.hubiottest.screens.common.toastshelper.ToastsHelper;
import com.example.hubiottest.screens.scanconfigesp.configthroughbluetooth.ConfigBLEDeviceActivity;
import com.example.hubiottest.screens.scanconfigesp.configthroughbluetooth.ConfigBTDualActivity;

import java.util.ArrayList;
import java.util.List;


import static android.content.Context.BIND_AUTO_CREATE;

public class EspListFragment extends AbstractFragment
        implements EspListViewMvc.Listener,
        OnOffScanBluetoothUseCase.Listener,
        PermissionsHelper.Listener, BluetoothLeService.CallbackToEspListFragment {
    private static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_CODE_LOCATION = 1001;
    public static final String EXTRAS_DEVICE = "DEVICE";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final int REQUEST_CODE = 1001;
    private boolean firstPermissionGranted = true;

    public static EspListFragment newInstance() {
        return new EspListFragment();
    }

    private EspListViewMvc mViewMvc;
    private ScreensNavigator mScreensNavigator;
    private OnOffScanBluetoothUseCase mControllerBluetooth;
    private PermissionsHelper mPermissionsHelper;
    private DialogsManager mDialogsManager;
    private BleScanListener mBleScanListener;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothDevice mBluetoothDevice;
    private LocationManager locationManager;
    private ToastsHelper mToastsHelper;

    private List<BluetoothDevice> deviceList = new ArrayList<>();
//    private Set<BluetoothDevice> mPairedDevice;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBleScanListener = getBleScanListener();
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        mScreensNavigator = getControllerCompositionRoot().getScreensNavigator();
        mControllerBluetooth = getControllerCompositionRoot().getControllerBluetooth(mBleScanListener);
        mPermissionsHelper = getControllerCompositionRoot().getPermissionsHelper();
        mDialogsManager = getControllerCompositionRoot().getDialogsManager();
        mToastsHelper = getControllerCompositionRoot().getToastsHelper();

        requestLocation();
        // Broadcast Bluetooth State Change
        IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        requireActivity().registerReceiver(mBroadcastBluetoothOnOffStateChange, intentFilter1);

        // Gatt service
        Intent gattServiceIntent = new Intent(requireActivity(), BluetoothLeService.class);
        requireActivity().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        // Gatt Update Status Receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        requireActivity().registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    private BleScanListener getBleScanListener() {
        return new BleScanListener() {
            @Override
            public void scanStartFailed() {

            }

            @Override
            public void onStatusScan(boolean statusScanning) {
                mViewMvc.showBarLoading(statusScanning);
            }

            @Override
            public void onPeripheralFound(BluetoothDevice device, ScanResult scanResult) {
                deviceList.add(device);
                mViewMvc.bindEsp(deviceList);
            }

            @Override
            public void scanCompleted() {
                mViewMvc.showBarLoading(false);
            }

            @Override
            public void onFailure(Exception e) {

            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewMvc = getControllerCompositionRoot().getViewMvcFactory().getEspListViewMvc(container);
        return mViewMvc.getRootView();
    }

    @Override
    public void onStart() {
        super.onStart();
        mViewMvc.registerListener(this);
        mControllerBluetooth.registerListener(this);
        mPermissionsHelper.registerListener(this);
        mControllerBluetooth.isSupportedAndIsOn();

    }

    @Override
    public void onStop() {
        super.onStop();
        mViewMvc.unregisterListener(this);
        mControllerBluetooth.unregisterListener(this);
        mPermissionsHelper.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireActivity().unregisterReceiver(mBroadcastBluetoothOnOffStateChange);
        mBluetoothLeService = null;
    }

    @Override
    public void onEspClicked(BluetoothDevice bleDevice) {
        mControllerBluetooth.stopScan();
        mBluetoothDevice = bleDevice;
        Log.e("TEST", bleDevice.getAddress() + "");
        mBluetoothLeService.connect(this, mBluetoothDevice.getAddress());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onNavigateUpClicked() {
        mScreensNavigator.navigateUp();
    }

    @Override
    public void onScanButtonClicked() {
        if (!isLocationOn()) {
            mToastsHelper.showToast("Location is off! Can not search ble devices");
        }
        deviceList.clear();
        mViewMvc.bindEsp(deviceList);

        mViewMvc.showTextViewAvailable(true);
        mControllerBluetooth.scanBleDevice();
    }

    @Override
    public void onSwitchOnOffClicked(boolean isOn) {
        if (isOn) {
            mControllerBluetooth.enableBluetooth();
            mViewMvc.setStatusTurningOnOff(BluetoothAdapter.STATE_TURNING_ON);
        } else {
            mControllerBluetooth.disableBluetooth();
            mViewMvc.setStatusTurningOnOff(BluetoothAdapter.STATE_TURNING_OFF);
        }
    }


    @Override
    public void isBluetoothEnabled(boolean isEnabled) {
        mViewMvc.bindViewSwitchPrefer(isEnabled);
        mViewMvc.setStatusTurningOnOff(isEnabled ? BluetoothAdapter.STATE_ON : BluetoothAdapter.STATE_OFF);
        mViewMvc.showRecyclerView(isEnabled);

        // Bluetooth is off, wait with scan until user switched it on
//        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
    }

    @Override
    public void onBleScanning() {
        if (!isLocationOn()) {
            mToastsHelper.showToast("Please turn on location to scan ble devices");
        } else {
            if (!firstPermissionGranted) {
                mControllerBluetooth.scanBleDevice();
            }
        }
    }

    @Override
    public void onNotSupportBluetooth() {

    }

//    @Override
//    public void onPairedDeviceFetched(Set<BluetoothDevice> setPairedDevice) {
//        Log.e("TEST PAIREDDDDDDDDDDDDDDDD", setPairedDevice.size() + "");
//        if (setPairedDevice.size() > 0) {
//            mViewMvc.showTextViewPaired(true);
//        }
////        mPairedDevice = setPairedDevice;
////        mViewMvc.bindPairedDevices(new ArrayList<>(mPairedDevice));
//    }

    @Override
    public void onPermissionGranted(String permission, int requestCode) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
            case REQUEST_CODE:
                mDialogsManager.showPermissionGrantedDialog(null);
                firstPermissionGranted = false;
                break;
        }
    }

    @Override
    public void onPermissionDeclined(String permission, int requestCode) {
        if (requestCode == REQUEST_CODE) {
            mDialogsManager.showDeclinedDialog(null);
            firstPermissionGranted = false;
        }
    }

    @Override
    public void onPermissionDeclinedDontAskAgain(String permission, int requestCode) {
        if (requestCode == REQUEST_CODE) {
            mDialogsManager.showPermissionDeclinedCantAskMoreDialog(null);
            firstPermissionGranted = false;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private final BroadcastReceiver mBroadcastBluetoothOnOffStateChange = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            assert action != null;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                mViewMvc.setStatusTurningOnOff(state);
            }
        }
    };

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (mBluetoothLeService.initialize()) {
                requireActivity().finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothLeService.ACTION_GATT_CONNECTED:
                        Log.e("TESTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT", BluetoothLeService.ACTION_GATT_CONNECTED);
//                        Log.e("TEN cua thiet bi:", mBluetoothDevice.getName()==null?"NULL":mBluetoothDevice.getName());
                        mViewMvc.showTextViewPaired(true);
//                        if (mPairedDevice.size() > 0 && !mPairedDevice.contains(mBluetoothDevice)) {
//                            mPairedDevice.add(mBluetoothDevice);
//                        } else if (mPairedDevice.size() == 0) {
//                            mPairedDevice.add(mBluetoothDevice);
//                        }

                        if (mBluetoothDevice != null) {
                            startActivityConfigEsp(mBluetoothDevice);
                        }else {
                            Log.e("TESTTTTTTTTTTTTTTT", "mBluetoothDevice null");
                        }
                        break;
                    case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                        Bundle extras = intent.getExtras();
                        int result = 0;
                        if (extras != null) {
                            result = extras.getInt("status");
                        }
                        if (result == 133) { // connection failed!!!!

//                            Toast.makeText(getContext()
//                                    , "Server connection failed\nRetry to connect again\nOr try to reset the ESP32"
//                                    , Toast.LENGTH_LONG).show();
                        }
//                        mPairedDevice.removeIf(device -> device.getAddress().equals(mBluetoothDevice.getAddress()));
//                        if (mPairedDevice.size() == 0) {
//                            mViewMvc.showTextViewPaired(false);
//                        }
//                        mViewMvc.bindPairedDevices(new ArrayList<>(mPairedDevice));
                        break;
                    case BluetoothLeService.ACTION_DATA_AVAILABLE:
                        Log.e("TESTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT", BluetoothLeService.ACTION_DATA_AVAILABLE);
                        break;
                    case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                        Log.e("TESTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT", BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
                        break;
                }
            }
        }
    };

    private void startActivityConfigEsp(BluetoothDevice mBluetoothDevice) {
        Log.e("startActivityConfigEsp", String.valueOf(mBluetoothDevice.getType()));
        if ((mBluetoothDevice.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC)
                || (mBluetoothDevice.getType() == BluetoothDevice.DEVICE_TYPE_DUAL)) {
            final Intent intent = new Intent(requireActivity(), ConfigBTDualActivity.class);
            intent.putExtra(EXTRAS_DEVICE_NAME, mBluetoothDevice.getName());
            intent.putExtra(EXTRAS_DEVICE, mBluetoothDevice);

            startActivity(intent);
        } else if (mBluetoothDevice.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
            final Intent intent = new Intent(requireActivity(), ConfigBLEDeviceActivity.class);
            intent.putExtra(EXTRAS_DEVICE, mBluetoothDevice);
            intent.putExtra(EXTRAS_DEVICE_NAME, mBluetoothDevice.getName());
            intent.putExtra(EXTRAS_DEVICE_ADDRESS, mBluetoothDevice.getAddress());

            startActivity(intent);
        } else {
            final Intent intent = new Intent(requireActivity(), ConfigBLEDeviceActivity.class);
            intent.putExtra(EXTRAS_DEVICE, mBluetoothDevice);
            intent.putExtra(EXTRAS_DEVICE_NAME, mBluetoothDevice.getName());
            intent.putExtra(EXTRAS_DEVICE_ADDRESS, mBluetoothDevice.getAddress());

            startActivity(intent);
        }
    }

    private void requestLocation() {
        if (mPermissionsHelper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
//            mDialogsManager.showPermissionGrantedDialog(null);
            firstPermissionGranted = false;
        } else {
            mPermissionsHelper.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_CODE);
        }
    }

    private boolean isLocationOn() {
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }


        if (!gps_enabled && !network_enabled) {
            return false;
        }
        return true;
    }

    @Override
    public void onServiceConnected(boolean isServiceConnected) {
        mViewMvc.showTextViewPaired(true);
//                        if (mPairedDevice.size() > 0 && !mPairedDevice.contains(mBluetoothDevice)) {
//                            mPairedDevice.add(mBluetoothDevice);
//                        } else if (mPairedDevice.size() == 0) {
//                            mPairedDevice.add(mBluetoothDevice);
//                        }

        if (mBluetoothDevice != null) {
            startActivityConfigEsp(mBluetoothDevice);
        }
    }
}
