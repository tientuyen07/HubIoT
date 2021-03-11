package com.example.hubiottest.screens.scanconfigesp.configthroughbluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hubiottest.R;
import com.example.hubiottest.modelandusecase.usecase.bluetoothusecase.BluetoothLeService;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;

public class ConfigBLEDeviceActivity extends AppCompatActivity {
    private final static String TAG = "ESP32WIFI_BLE_CTRL";

    private TextView mDataField;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothDevice mmDevice;

    private String ssidPrimString = "";
    private String pwPrimString = "";

    private EditText ssidPrimET;
    private EditText pwPrimET;
    private EditText ssidSecET;
    private EditText pwSecET;

    private Menu thisMenu;

    private Boolean firstView = true;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
// Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(null, mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.This can be a result of read
    //or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothLeService.ACTION_GATT_CONNECTED:
                        mConnected = true;
                        invalidateOptionsMenu();
                        break;
                    case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                        Bundle extras = intent.getExtras();
                        int result = 0;
                        if (extras != null) {
                            result = extras.getInt("status");
                        }
                        if (result == 133) { // connection failed!!!!
                            Log.e(TAG, "Server connection failed");
                            Toast.makeText(getApplicationContext()
                                    , "Server connection failed\nRetry to connect again\nOr try to reset the ESP32"
                                    , Toast.LENGTH_LONG).show();
                        }
                        mConnected = false;
                        invalidateOptionsMenu();
                        clearUI();
                        break;
                    case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                        Log.d(TAG, "Discovery finished");
                        if(mBluetoothLeService != null) {
//                            mBluetoothLeService.readCustomCharacteristic();
                        }
                    case BluetoothLeService.ACTION_DATA_AVAILABLE:
                        thisMenu.findItem(R.id.menu_connect).setActionView(null);
                        String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                        if (data != null) {
                            // Decode the data
//                            byte[] decodedData = xorCode(mmDevice.getName(),data.getBytes(),data.length());
                            //String finalData = new String(decodedData);
                            Log.e("ConfigBLEDeviceActivity", data);
                            displayData("Received:\n--\n" + data + "\n--\n");

                            // Get stored WiFi credentials from the received data
//                            JSONObject receivedConfigJSON;
//                            try {
//                                receivedConfigJSON = new JSONObject(finalData);
//                                if (receivedConfigJSON.has("ssidPrim")) {
//                                    ssidPrimString = receivedConfigJSON.getString("ssidPrim");
//                                    ssidPrimET.setText(ssidPrimString);
//                                }
//                                if (receivedConfigJSON.has("pwPrim")) {
//                                    pwPrimString = receivedConfigJSON.getString("pwPrim");
//                                    pwPrimET.setText(pwPrimString);
//                                }
//                                if (receivedConfigJSON.has("ssidSec")) {
//                                    ssidSecString = receivedConfigJSON.getString("ssidSec");
//                                    ssidSecET.setText(ssidSecString);
//                                }
//                                if (receivedConfigJSON.has("pwSec")) {
//                                    pwSecString = receivedConfigJSON.getString("pwSec");
//                                    pwSecET.setText(pwSecString);
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
                        }
                        break;
                }
            }
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_control);

//        final Intent intent = getIntent();
//        String mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
//        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
//        mmDevice = intent.getParcelableExtra(EXTRAS_DEVICE);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

//        android.app.ActionBar thisActionBar = getActionBar();

        // Sets up UI references.
        mDataField = findViewById(R.id.data_value);
        ssidPrimET = findViewById(R.id.ssidPrim);
        pwPrimET = findViewById(R.id.pwPrim);
        ssidSecET = findViewById(R.id.ssidSec);
        pwSecET = findViewById(R.id.pwSec);

        //noinspection ConstantConditions
//        getActionBar().setTitle(mDeviceName);
//        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(null, mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.control, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            if (firstView) {
                menu.findItem(R.id.menu_connect).setActionView(R.layout.progress_bar);
                firstView = false;
            } else {
                menu.findItem(R.id.menu_connect).setActionView(null);
            }
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        thisMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                thisMenu.findItem(R.id.menu_connect).setActionView(R.layout.progress_bar);
                mBluetoothLeService.connect(null, mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                thisMenu.findItem(R.id.menu_connect).setActionView(null);
                return true;
            case android.R.id.home:
                thisMenu.findItem(R.id.menu_connect).setActionView(null);
                firstView = false;
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @SuppressWarnings("unused")
    public void onClickWrite(View v){
        if(mBluetoothLeService != null) {

            // Update credentials with last edit text values
            ssidPrimString = ssidPrimET.getText().toString();
            pwPrimString = pwPrimET.getText().toString();

            // Create JSON object
            /*JSONObject wifiCreds = new JSONObject();
            try {
                wifiCreds.put("cmd", "change wifi");
                if (ssidPrimString.equals("")) {
                    Toast.makeText(getApplicationContext()
                            , "Missing primary SSID entry"
                            , Toast.LENGTH_LONG).show();
                    displayData(getResources().getString(R.string.error_credentials));
                    return;
                } else {
                    wifiCreds.put("ssid", ssidPrimString);
                }
                if (pwPrimString.equals("")) {
                    Toast.makeText(getApplicationContext()
                            , "Missing primary password entry"
                            , Toast.LENGTH_LONG).show();
                    displayData(getResources().getString(R.string.error_credentials));
                    return;
                } else {
                    wifiCreds.put("pwd", pwPrimString);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }*/
//            byte[] decodedData = xorCode(mmDevice.getName()
//                    ,wifiCreds.toString().getBytes()
//                    ,wifiCreds.toString().length());
            JSONObject wifiCreds = new JSONObject();
            try {
                wifiCreds.put("hash", "333835393337");
                wifiCreds.put("cmd", "0017");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mBluetoothLeService.writeCustomCharacteristic(wifiCreds.toString());
            displayData(getResources().getString(R.string.update_config));
        }
    }

    @SuppressWarnings("unused")
    public void onClickRead(View v){
        thisMenu.findItem(R.id.menu_connect).setActionView(R.layout.progress_bar);
        if(mBluetoothLeService != null) {
            mBluetoothLeService.readCustomCharacteristic();
        }
    }

    @SuppressWarnings("unused")
    public void onClickErase(View v){
        thisMenu.findItem(R.id.menu_connect).setActionView(R.layout.progress_bar);
        if(mBluetoothLeService != null) {
            // Create JSON object
            JSONObject wifiCreds = new JSONObject();
            try {
                wifiCreds.put("erase", true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            byte[] decodedData = xorCode(mmDevice.getName()
//                    ,wifiCreds.toString().getBytes()
//                    ,wifiCreds.toString().length());
            mBluetoothLeService.writeCustomCharacteristic(wifiCreds.toString());
            displayData(getResources().getString(R.string.erase_config));
        }
    }

    @SuppressWarnings("unused")
    public void onClickReset(View v){
        thisMenu.findItem(R.id.menu_connect).setActionView(R.layout.progress_bar);
        if(mBluetoothLeService != null) {
            // Create JSON object
            JSONObject wifiCreds = new JSONObject();
            try {
                wifiCreds.put("reset", true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            byte[] decodedData = xorCode(mmDevice.getName()
//                    ,wifiCreds.toString().getBytes()
//                    ,wifiCreds.toString().length());
            mBluetoothLeService.writeCustomCharacteristic(wifiCreds.toString());
            displayData(getResources().getString(R.string.erase_config));
        }
    }

    @SuppressWarnings("unused")
    public void onClickSwitch(View v){
        TextView chgHdr;
        EditText chgEt;
        Switch enaDoubleAP = findViewById(R.id.apNumSelector);
        if (enaDoubleAP.isChecked()) {
            chgHdr = findViewById(R.id.ssidSecHdr);
            chgHdr.setVisibility(View.VISIBLE);
            chgEt = findViewById(R.id.ssidSec);
            chgEt.setVisibility(View.VISIBLE);
            chgHdr = findViewById(R.id.pwSecHdr);
            chgHdr.setVisibility(View.VISIBLE);
            chgEt = findViewById(R.id.pwSec);
            chgEt.setVisibility(View.VISIBLE);
        } else {
            chgHdr = findViewById(R.id.ssidSecHdr);
            chgHdr.setVisibility(View.INVISIBLE);
            chgEt = findViewById(R.id.ssidSec);
            chgEt.setVisibility(View.INVISIBLE);
            chgHdr = findViewById(R.id.pwSecHdr);
            chgHdr.setVisibility(View.INVISIBLE);
            chgEt = findViewById(R.id.pwSec);
            chgEt.setVisibility(View.INVISIBLE);
        }
    }
}
