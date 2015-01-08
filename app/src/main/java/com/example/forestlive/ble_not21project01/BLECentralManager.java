package com.example.forestlive.ble_not21project01;

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
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

/**
 * Created by forestlive on 2015/01/07.
 */
public class BLECentralManager {

    private final String TAG = "BLECentralManager";

    public interface onCentraListener {
        public enum GattState {
            ConnectType,
            BeingTYPE,
            DisconnectType,
        }

        public void onStateChange(GattState type);

        public void onCharacteristicRead(String str);

        public void onCharacteristicWrite(String str);

    }

    private onCentraListener mListener = null;
    private Context mContext = null;

    // Bluetooth
    private BluetoothAdapter mAdapter = null;
    private BluetoothDevice mDevice = null;

    // Connect Device
    private BluetoothGatt mGatt = null;
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic = null;


    public BLECentralManager(Context context, onCentraListener listener) {
        this.mContext = context;
        this.mListener = listener;

        initBLE();
    }


    private Boolean initBLE() {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mContext, "ble_not_supported", Toast.LENGTH_SHORT).show();
            return false;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = bluetoothManager.getAdapter();

        if (mAdapter == null) {
            Toast.makeText(mContext, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 接続先のBluetotohDeviceを作成する．
        mDevice = mAdapter.getRemoteDevice(Info.CON_DEVICE_ADDRESS);

        return true;
    }

    public void onDestroy() {
        mGatt.disconnect();
        mGatt.close();

        if (mGatt != null) {
            mGatt = null;
        }
    }

    /**
     * BluetoothGatt connectGatt(Context context, boolean autoConnect,BluetoothGattCallback callback)
     * context : 普通に代入するだけ
     * autoConnect : 自動的に接続するかしないか
     * callback : 結果を返す．
     */
    public void connect() {
        mGatt = mDevice.connectGatt(mContext, false, mGattCallBack);
    }

    public void discconect() {
        mGatt.disconnect();
    }

    public void readCharacteristic() {
        BluetoothGattCharacteristic characteristic = null;
        characteristic = createCharacter();
        characteristic.setValue("!!");
        if (mGatt.readCharacteristic(characteristic)) {
            Log.d(TAG, "readCharacteristic success ");
        } else {
            Log.d(TAG, "readCharacteristic failed");
        }
    }

    public void writeCharacteristic() {
        BluetoothGattCharacteristic characteristic = null;
        characteristic = createCharacter();

        if (characteristic != null) {
            characteristic.setValue("Hello Server".getBytes());
            if (mGatt.writeCharacteristic(characteristic)) {
                Log.d(TAG, "writeCharacteristic success ");
            } else {
                Log.d(TAG, "writeCharacteristic failed");
            }
        }
    }

    private BluetoothGattCharacteristic createCharacter() {
        BluetoothGattService nameService = null;
        BluetoothGattCharacteristic characteristic = null;

        nameService = mGatt.getService(UUID.fromString(Info.UUID_SAMPLE_NAME_SERVICE));

        if (nameService == null) {
            Log.d(TAG, "nameservice not found...");
            return null;
        }

        characteristic = nameService.getCharacteristic(UUID.fromString(Info.UUID_SAMPLE_NAME_CHARACTERISTIC));
        if (characteristic == null) {
            Log.d(TAG, "characteristic not found...");
            return null;
        }

        byte[] value = {0x00, (byte) (0xB9) , 0x0D, (byte) (0x90), 0x2F};
        if(!characteristic.setValue(value)){
            Log.d(TAG, "Couldn't set characteristic's local value");
        }

        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);


        return characteristic;
    }


    private BluetoothGattCallback mGattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.d(TAG, "OUT_onConnectionStateChange " + status + " " + gatt.getDevice().getName());

            switch (newState) {
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "STATE_DISCONNECTED");
                    mListener.onStateChange(onCentraListener.GattState.DisconnectType);
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    Log.i(TAG, "STATE_CONNECTING");
                    mListener.onStateChange(onCentraListener.GattState.BeingTYPE);
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "STATE_CONNECTED");
                    mListener.onStateChange(onCentraListener.GattState.ConnectType);
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    Log.i(TAG, "STATE_DISCONNECTING");
                    mListener.onStateChange(onCentraListener.GattState.BeingTYPE);
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "onServicesDiscovered");

            readCharacteristic();
            mGatt = gatt;
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,  int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "OUT_onCharacteristicRead");

            mListener.onCharacteristicRead(characteristic.getStringValue(0));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "OUT_onCharacteristicWrite ");


            mListener.onCharacteristicWrite(new String(characteristic.getStringValue(0)));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "onCharacteristicChanged");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.d(TAG, "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.d(TAG, "onReadRemoteRssi");
        }
    };
}
