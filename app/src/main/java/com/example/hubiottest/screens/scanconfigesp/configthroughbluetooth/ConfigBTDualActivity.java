package com.example.hubiottest.screens.scanconfigesp.configthroughbluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import com.example.hubiottest.common.bluetoothutils.BluetoothSerial;
import com.example.hubiottest.screens.scanconfigesp.espdevicelist.EspListFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.example.hubiottest.common.bluetoothutils.XorCoding.xorCode;

public class ConfigBTDualActivity extends AppCompatActivity {

    private final static String TAG = ConfigBTDualActivity.class.getSimpleName();

    private TextView mDataField;

    private BluetoothSerial bluetoothSerial;
    private BluetoothDevice mmDevice;

    private String ssidPrimString = "";
    private String pwPrimString = "";
    private String ssidSecString = "";
    private String pwSecString = "";

    private Boolean doubleApEnabled = false;

    private EditText ssidPrimET;
    private EditText pwPrimET;
    private EditText ssidSecET;
    private EditText pwSecET;

    private Menu thisMenu;

    private Boolean firstView = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_control);

        final Intent intent = getIntent();
        mmDevice = intent.getParcelableExtra(EspListFragment.EXTRAS_DEVICE);
        String mDeviceName = intent.getStringExtra(EspListFragment.EXTRAS_DEVICE_NAME);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        android.app.ActionBar thisActionBar = getActionBar();

        // Sets up UI references.
        mDataField = findViewById(R.id.data_value);
        ssidPrimET = findViewById(R.id.ssidPrim);
        pwPrimET = findViewById(R.id.pwPrim);
        ssidSecET = findViewById(R.id.ssidSec);
        pwSecET = findViewById(R.id.pwSec);

        //noinspection ConstantConditions
        thisActionBar.setTitle(mDeviceName);
        thisActionBar.setDisplayHomeAsUpEnabled(true);

        clearUI();

        //MessageHandler is call when bytes are read from the serial input
        bluetoothSerial = new BluetoothSerial(this, btSerialRead, mmDevice);
    }

    private final BroadcastReceiver bluetoothDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "SerialBT disconnected");
            invalidateOptionsMenu();
        }
    };

    private final BroadcastReceiver bluetoothConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "SerialBT connected");
            readCreds();
            invalidateOptionsMenu();
        }
    };

    private final BroadcastReceiver bluetoothFailedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "SerialBT init failed");
            invalidateOptionsMenu();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        //Fired when connection is established and also fired when onResume is called if a connection is already established.
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(bluetoothConnectReceiver
                        , new IntentFilter(BluetoothSerial.BLUETOOTH_CONNECTED));
        //Fired when the connection is lost
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(bluetoothDisconnectReceiver
                        , new IntentFilter(BluetoothSerial.BLUETOOTH_DISCONNECTED));
        //Fired when connection can not be established.
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(bluetoothFailedReceiver
                        , new IntentFilter(BluetoothSerial.BLUETOOTH_FAILED));

        //onResume calls connect, it is safe
        //to call connect even when already connected
        bluetoothSerial.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothSerial.onPause();
        bluetoothSerial.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothSerial.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.control, menu);
        if ((bluetoothSerial!= null) && bluetoothSerial.connected) {
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
                bluetoothSerial.onResume();
                return true;
            case R.id.menu_disconnect:
                bluetoothSerial.onPause();
                bluetoothSerial.close();
                return true;
            case android.R.id.home:
                thisMenu.findItem(R.id.menu_connect).setActionView(null);
                firstView = false;
                bluetoothSerial.close();
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    @SuppressWarnings("unused")
    public void onClickWrite(View v){
        if (bluetoothSerial.connected) {
            // Update credentials with last edit text values
            ssidPrimString = ssidPrimET.getText().toString();
            pwPrimString = pwPrimET.getText().toString();
            ssidSecString = ssidSecET.getText().toString();
            pwSecString = pwSecET.getText().toString();

            // Create JSON object
            JSONObject wifiCreds = new JSONObject();
            try {
                if (ssidPrimString.equals("")) {
                    Toast.makeText(getApplicationContext()
                            , "Missing primary SSID entry"
                            , Toast.LENGTH_LONG).show();
                    displayData(getResources().getString(R.string.error_credentials));
                    return;
                } else {
                    wifiCreds.put("ssidPrim", ssidPrimString);
                }
                if (pwPrimString.equals("")) {
                    Toast.makeText(getApplicationContext()
                            , "Missing primary password entry"
                            , Toast.LENGTH_LONG).show();
                    displayData(getResources().getString(R.string.error_credentials));
                    return;
                } else {
                    wifiCreds.put("pwPrim", pwPrimString);
                }
                if (ssidSecString.equals("") && doubleApEnabled) {
                    Toast.makeText(getApplicationContext()
                            , "Missing secondary SSID entry"
                            , Toast.LENGTH_LONG).show();
                    displayData(getResources().getString(R.string.error_credentials));
                    return;
                } else if (ssidSecString.equals("") && !doubleApEnabled) {
                    wifiCreds.put("ssidSec", ssidPrimString);
                } else {
                    wifiCreds.put("ssidSec", ssidSecString);
                }
                if (pwSecString.equals("") && doubleApEnabled) {
                    Toast.makeText(getApplicationContext()
                            , "Missing secondary password entry"
                            , Toast.LENGTH_LONG).show();
                    displayData(getResources().getString(R.string.error_credentials));
                    return;
                } else if (pwSecString.equals("") && !doubleApEnabled) {
                    wifiCreds.put("pwSec", pwPrimString);
                } else {
                    wifiCreds.put("pwSec", pwSecString);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String dataStr = wifiCreds.toString();
            int count = dataStr.length();
            byte[] data = dataStr.getBytes();

            // Decode the data
            byte[] encryptedBuffer = xorCode(mmDevice.getName(),data,count);

            try {
                bluetoothSerial.write(encryptedBuffer,0, count);
                displayData(getResources().getString(R.string.update_config));
            } catch (IOException e) {
                displayData(getResources().getString(R.string.error_sending));
                e.printStackTrace();
            }
        } else {
            displayData(getResources().getString(R.string.error_no_connection));
        }
    }

    @SuppressWarnings("unused")
    public void onClickRead(View v){
        readCreds();
    }

    private void readCreds() {
        if (bluetoothSerial.connected) {
            // Decode the data
            byte[] data = "{\"read\":\"true\"}".getBytes();
            byte[] decodedBuffer = xorCode(mmDevice.getName(),data,data.length);
            try {
                bluetoothSerial.write(decodedBuffer,0, decodedBuffer.length);
                displayData(getResources().getString(R.string.get_config));
            } catch (IOException e) {
                displayData(getResources().getString(R.string.error_sending));
                e.printStackTrace();
            }
        } else {
            displayData(getResources().getString(R.string.error_no_connection));
        }
    }

    @SuppressWarnings("unused")
    public void onClickErase(View v){
        if ((bluetoothSerial != null) && bluetoothSerial.connected) {
            // Decode the data
            byte[] data = "{\"erase\":\"true\"}".getBytes();
            byte[] decodedBuffer = xorCode(mmDevice.getName(),data,data.length);
            try {
                bluetoothSerial.write(decodedBuffer,0, decodedBuffer.length);
                displayData(getResources().getString(R.string.erase_config));
            } catch (IOException e) {
                displayData(getResources().getString(R.string.error_sending));
                e.printStackTrace();
            }
        } else {
            displayData(getResources().getString(R.string.error_no_connection));
        }
    }

    @SuppressWarnings("unused")
    public void onClickReset(View v){
        if ((bluetoothSerial != null) && bluetoothSerial.connected) {
            // Decode the data
            byte[] data = "{\"reset\":\"true\"}".getBytes();
            byte[] decodedBuffer = xorCode(mmDevice.getName(),data,data.length);
            try {
                bluetoothSerial.write(decodedBuffer,0, decodedBuffer.length);
                displayData(getResources().getString(R.string.reset_device));
            } catch (IOException e) {
                displayData(getResources().getString(R.string.error_sending));
                e.printStackTrace();
            }
        } else {
            displayData(getResources().getString(R.string.error_no_connection));
        }
    }

    @SuppressWarnings("unused")
    public void onClickSwitch(View v){
        TextView chgHdr;
        EditText chgEt;
        Switch enaDoubleAP = findViewById(R.id.apNumSelector);
        if (enaDoubleAP.isChecked()) {
            doubleApEnabled = true;
            chgHdr = findViewById(R.id.ssidSecHdr);
            chgHdr.setVisibility(View.VISIBLE);
            chgEt = findViewById(R.id.ssidSec);
            chgEt.setVisibility(View.VISIBLE);
            chgHdr = findViewById(R.id.pwSecHdr);
            chgHdr.setVisibility(View.VISIBLE);
            chgEt = findViewById(R.id.pwSec);
            chgEt.setVisibility(View.VISIBLE);
        } else {
            doubleApEnabled = false;
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

    private final BluetoothSerial.MessageHandler btSerialRead = new BluetoothSerial.MessageHandler() {
        @Override
        public int read(final int bufferSize, byte[] buffer) {
            final byte[] readBuffer = new byte[bufferSize];
            System.arraycopy(buffer, 0, readBuffer, 0, bufferSize);
            final String data;
            data = new String(readBuffer);
            // Rest has to be done on UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Display the received data
                    thisMenu.findItem(R.id.menu_connect).setActionView(null);
                    // Encode the data
                    byte[] encodedData = xorCode(mmDevice.getName(), readBuffer, bufferSize);
                    String finalData = new String(encodedData);

                    displayData("Received:\n--\n" + data + "\n--\n" + finalData);
                    // Get stored WiFi credentials from the received data
                    JSONObject receivedConfigJSON;
                    try {
                        receivedConfigJSON = new JSONObject(finalData);
                        if (receivedConfigJSON.has("ssidPrim")) {
                            ssidPrimString = receivedConfigJSON.getString("ssidPrim");
                            ssidPrimET.setText(ssidPrimString);
                        }
                        if (receivedConfigJSON.has("pwPrim")) {
                            pwPrimString = receivedConfigJSON.getString("pwPrim");
                            pwPrimET.setText(pwPrimString);
                        }
                        if (receivedConfigJSON.has("ssidSec")) {
                            ssidSecString = receivedConfigJSON.getString("ssidSec");
                            ssidSecET.setText(ssidSecString);
                        }
                        if (receivedConfigJSON.has("pwSec")) {
                            pwSecString = receivedConfigJSON.getString("pwSec");
                            pwSecET.setText(pwSecString);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return bufferSize;
        }
    };
}
