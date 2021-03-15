package com.example.hubiottest.modelandusecase.usecase.bluetoothusecase;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.hubiottest.R;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;

public class BluetoothLeService extends Service {

    public interface CallbackToEspListFragment {
        void onServiceConnected(boolean isServiceConnected);
    }

    private CallbackToEspListFragment mCallback;

    private final static String TAG = "BluetoothLeService";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    // private final String serviceUUID = "00001800-0000-1000-8000-00805f9b34fb";
    private final String READ_NAME_HUB_UUID = "00002a00-0000-1000-8000-00805f9b34fb";
    private final String xxxUUID = "00002a25-0000-1000-8000-00805f9b34fb";
    private final String yyyUUID = "00002a01-0000-1000-8000-00805f9b34fb";
    private final String zzzUUID = "00002a04-0000-1000-8000-00805f9b34fb";
    private final String tttUUID = "00002aa6-0000-1000-8000-00805f9b34fb";

    private final UUID BLE_WRITE_SERVICE_UUID = convertFromInteger(0x18AA);
    private final UUID BLE_WRITE_CHARACTERISTIC_UUID = convertFromInteger(0x2aaa);
    private final UUID BLE_CCCD_UUID = convertFromInteger(0x2902);

    public final static String ACTION_GATT_CONNECTED =
            "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "EXTRA_DATA";

    // Implements callback methods for GATT events that the app cares about.For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction, status);
                if (mCallback != null) {
                    mCallback.onServiceConnected(true);
                }

                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server. with status " + Integer.toString(status));
                mBluetoothGatt.close();
                mBluetoothGatt = null;
                broadcastUpdate(intentAction, status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, status);

                StringBuilder serviceDiscovery;

                List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();
//                Log.e("onServicesDiscovered", "Services count: " + gattServices.size());
                serviceDiscovery = new StringBuilder("Found " + gattServices.size() + " services\n");
                for (BluetoothGattService gattService : gattServices) {
                    String serviceUUID = gattService.getUuid().toString();
                    Log.e("onServicesDiscovered", "Service uuid " + serviceUUID);
                    serviceDiscovery.append("Service uuid ").append(serviceUUID).append("\n");
                    for (BluetoothGattCharacteristic characteristic : gattService.getCharacteristics()) {
                        Log.e("onServiceDiscovered", "Characteristic uuid" + characteristic.getUuid().toString());
                    }
                }

//                broadcastUpdate(serviceDiscovery.toString());

                BluetoothGattCharacteristic characteristic =
                        gatt.getService(BLE_WRITE_SERVICE_UUID)
                                .getCharacteristic(BLE_WRITE_CHARACTERISTIC_UUID);
                characteristic.setWriteType(WRITE_TYPE_DEFAULT);

//                Log.e("TEST",
//                        "Set notification: " + gatt.setCharacteristicNotification(characteristic, true));

                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BLE_CCCD_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                Log.e("TEST", "Set Notification: " + gatt.writeDescriptor(descriptor));

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.e("TEST", "onDescriptorWrite");

            BluetoothGattCharacteristic characteristic =
                    gatt.getService(BLE_WRITE_SERVICE_UUID)
                            .getCharacteristic(BLE_WRITE_CHARACTERISTIC_UUID);
            characteristic.setValue(new byte[]{0x00, 0x15, 0x39, 0x32, 0x36, 0x34, 0x36, 0x32});

            mBluetoothGatt.writeCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("TEST", "onCharacteristicRead status: " + "GATT_READ_SUCCESS");
//                broadcastUpdate(characteristic, status);
                String s = new String(characteristic.getValue(), StandardCharsets.UTF_8);
                Log.e("TEST", "onCharacteristicRead status: " + s);
            } else if (status == BluetoothGatt.GATT_FAILURE) {
                Log.e("TEST", "onCharacteristicRead status: " + "GATT_READ_FAIL");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            switch (status) {
                case BluetoothGatt.GATT_SUCCESS:
                    Log.e("TEST", "onCharacteristicWrite status: " + "GATT_WRITE_SUCCESS");
                    break;
                case BluetoothGatt.GATT_FAILURE:
                    Log.e("TEST", "onCharacteristicWrite status: " + "GATT_WRITE_FAILURE");
                    break;
            }


            Log.e("TEST", "onCharacteristicWrite status: " + characteristic.getStringValue(0));

            if (status == BluetoothGatt.GATT_SUCCESS) {
//                readCustomCharacteristic();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            String s = new String(characteristic.getValue(), StandardCharsets.UTF_8);
            Log.e("TEST", "onCharacteristicChanged status: " + s);
//            broadcastUpdate(characteristic, 0);
        }
    };

    private void broadcastUpdate(final String action, int status) {
        final Intent intent = new Intent(action);
        intent.putExtra("status", status);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(String data) {
        final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
        intent.putExtra("status", 0);
        intent.putExtra(EXTRA_DATA, data);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final BluetoothGattCharacteristic characteristic, int status) {
        final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
        intent.putExtra("status", status);

        // Write the data as received for debug purposes
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            intent.putExtra(EXTRA_DATA, new String(data));
        } else {
            intent.putExtra(EXTRA_DATA, getResources().getString(R.string.empty_response));
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
// After using a given device, you should make sure that BluetoothGatt.close() is called
// such that resources are cleaned up properly.In this particular example, close() is
// invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
// For API level 18 and above, get a reference to BluetoothAdapter through
// BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return true;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return true;
        }

        return false;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(CallbackToEspListFragment callback, final String address) {
        mCallback = callback;
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.Try to reconnect.
        if (address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {

            } else {

            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
//        mBluetoothGatt = (new BleConnectionCompat(this)).connectGatt(device, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    private void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCustomCharacteristic() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = mBluetoothGatt.
                getService(BLE_WRITE_SERVICE_UUID);
        if (mCustomService == null) {
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.
                getCharacteristic(BLE_WRITE_CHARACTERISTIC_UUID);

        if ((mReadCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            mBluetoothGatt.readCharacteristic(mReadCharacteristic);
        } else {
            Log.w(TAG, "Failed to get Properties()");
            return;
        }

        /* Get all characteristic from hub
        List<BluetoothGattCharacteristic> mReadCharacteristic = mCustomService.
                getCharacteristics();
        for (BluetoothGattCharacteristic temp : mReadCharacteristic) {
            Log.e("Characteristic: ", temp.getUuid().toString());
            if (!mBluetoothGatt.readCharacteristic(temp)) {
                Log.w(TAG, "Failed to read characteristic");
                return;
            }
        }*/


//		Log.i(TAG, mReadCharacteristic.getStringValue(0));
        Log.i(TAG, "Trying to log received value");
    }

    public void writeCustomCharacteristic(String editCommand) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = mBluetoothGatt
                .getService(BLE_WRITE_SERVICE_UUID);
        if (mCustomService == null) {
            Log.w(TAG, "Custom BLE Service not found");

            return;
        }
        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic mWriteCharacteristic = mCustomService
                .getCharacteristic(BLE_WRITE_CHARACTERISTIC_UUID);

        for (byte a : hexStringToByteArray(editCommand)) {
            Log.e("BYTE", String.valueOf(a) + " ");
        }
        if ((mWriteCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            // set value for GattCharacteristic
//        byte[] tempBytes = {0x33,0x38, 0x35, 0x39, 0x33, 0x37, 0x00, 0x15, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36};
//            mWriteCharacteristic.setValue(new byte[]{1, 1});
            mWriteCharacteristic.setValue(hexStringToByteArray(editCommand));
            mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
        } else {
            Log.w(TAG, "Failed to write characteristic");
        }

    }

    private UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    /* s must be an even-length string. */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
