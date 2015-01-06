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
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import static com.example.forestlive.ble_not21project01.ConnectDevice.GATT_TYPE.BEING_TYPE;
import static com.example.forestlive.ble_not21project01.ConnectDevice.GATT_TYPE.CONNECT_TYPE;
import static com.example.forestlive.ble_not21project01.ConnectDevice.GATT_TYPE.DISCONNECT_TYPE;

public class ConnectDevice extends ActionBarActivity implements View.OnClickListener {

    private final String TAG = "ConnectDevice";

    private BluetoothAdapter mAdapter = null;
    private BluetoothManager mManager = null;

    private BluetoothDevice connectDev = null;

    // Connect Device
    private BluetoothGatt mGatt = null;

    // Layout
    private Button bt_connect = null;
    private Button bt_disconnect = null;
    private Button bt_send = null;
    private ListView lv_info = null;

    /**
     * UUID
     */
    public static final String UUID_SAMPLE_NAME_SERVICE = "000fefe-0000-1000-8000-00805f9b34fb";
    /**
     * 名前を配信するCHARACTERISTIC
     */
    public static final String UUID_SAMPLE_NAME_CHARACTERISTIC = "0000ffee-0000-1000-8000-00805f9b34fb";

    private ArrayAdapter<String> mListAdapter = null;
    private Handler mHandler = null;


    public enum GATT_TYPE {
        CONNECT_TYPE,
        BEING_TYPE,
        DISCONNECT_TYPE,
    }

    public enum NewGatt {
        StateChange,
        ServicesDiscovered,
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connectdevice);

        initLayout();
        initBLE();
        init();
    }

    private void init() {
        mHandler = new Handler();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGatt.disconnect();
    }

    private void setGattData(BluetoothGatt gatt, NewGatt newGatt) {
        this.mGatt = gatt;

        switch (newGatt) {
            case StateChange:
                Log.d("OUT", "StateChange");
                this.mGatt.discoverServices();
                break;
            case ServicesDiscovered:
                Log.d("OUT", "ServicesDiscovered");

                for (BluetoothGattService service : this.mGatt.getServices()) {
                    Log.d("OUT", "service : " + service.getCharacteristics());
                }
                break;
        }
    }

    private void initLayout() {
        bt_connect = (Button) findViewById(R.id.bt_connect);
        bt_connect.setOnClickListener(this);
        bt_disconnect = (Button) findViewById(R.id.bt_disconnect);
        bt_disconnect.setOnClickListener(this);
        bt_send = (Button) findViewById(R.id.bt_send);
        bt_send.setOnClickListener(this);

        lv_info = (ListView) findViewById(R.id.lv_info);

        mListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mListAdapter.add("Test");

        lv_info.setAdapter(mListAdapter);

        changeLayout(GATT_TYPE.CONNECT_TYPE);
    }


    private void initBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = bluetoothManager.getAdapter();

        if (mAdapter == null) {
            Toast.makeText(this, "error_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 接続先のBluetotohDeviceを作成する．
        connectDev = mAdapter.getRemoteDevice(Info.CON_DEVICE_ADDRESS);

    }

    private BluetoothGattCallback mGattCallBack = new BluetoothGattCallback() {

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, final int status) {
            super.onMtuChanged(gatt, mtu, status);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothGatt.GATT_SUCCESS == status) {
                        Log.d("OUT", "onMtuChanged");
                    }else {
                        Log.d("OUT", "not onMtuChanged");
                    }
                }
            });


        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.d("OUT", "OUT_onConnectionStateChange " + status + " " + gatt.getDevice().getName());

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    switch (newState) {
                        case BluetoothProfile.STATE_DISCONNECTED:
                            Log.d("OUT", "STATE_DISCONNECTED");

                            changeLayout(CONNECT_TYPE);

                            break;
                        case BluetoothProfile.STATE_CONNECTING:
                            Log.d("OUT", "STATE_CONNECTING");
                            changeLayout(BEING_TYPE);
                            break;
                        case BluetoothProfile.STATE_CONNECTED:
                            Log.d("OUT", "STATE_CONNECTED");
                            setGattData(gatt, NewGatt.StateChange);

                            changeLayout(DISCONNECT_TYPE);
                            break;
                        case BluetoothProfile.STATE_DISCONNECTING:
                            Log.d("OUT", "STATE_DISCONNECTING");
                            changeLayout(BEING_TYPE);
                            break;
                    }
                }
            });


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d("OUT", "OUT_onServicesDiscovered -> " + status + " service->" + gatt.getServices().size());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("OUT", "GATT_SUCCESS");
                setGattData(gatt, NewGatt.ServicesDiscovered);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d("OUT", "OUT_onCharacteristicRead");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("OUT", "OUT_onCharacteristicWrite");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d("OUT", "onCharacteristicChanged");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d("OUT", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d("OUT", "onDescriptorWrite");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.d("OUT", "onReliableWriteCompleted");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.d("OUT", "onReadRemoteRssi");
        }

    };

    private void changeLayout(GATT_TYPE type) {

        switch (type) {
            case CONNECT_TYPE:
                bt_connect.setEnabled(true);
                bt_connect.setTextColor(Color.BLUE);
                bt_disconnect.setEnabled(false);
                bt_disconnect.setTextColor(Color.RED);
                bt_send.setVisibility(View.GONE);
                break;
            case DISCONNECT_TYPE:
                bt_connect.setEnabled(false);
                bt_connect.setTextColor(Color.RED);
                bt_disconnect.setEnabled(true);
                bt_disconnect.setTextColor(Color.BLUE);
                bt_send.setVisibility(View.VISIBLE);
                break;

            case BEING_TYPE:
                bt_connect.setEnabled(false);
                bt_connect.setTextColor(Color.RED);
                bt_disconnect.setEnabled(false);
                bt_disconnect.setTextColor(Color.RED);
                break;
        }
    }


    public boolean writeCharacteristic() {
        //check mBluetoothGatt is available
        if (mAdapter == null || mGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        BluetoothGattService Service = mGatt.getServices().get(0);
        if (Service == null) {
            Log.e(TAG, "service not found!");
            return false;
        }

        BluetoothGattCharacteristic characteristic = null;

        for (BluetoothGattService service : mGatt.getServices()) {
            for (BluetoothGattCharacteristic chatGatt : service.getCharacteristics()) {
                characteristic = Service.getCharacteristic(chatGatt.getUuid());
            }

            if (characteristic == null) {
                Log.e(TAG, "char not found! " + characteristic);

            } else {
                Log.e(TAG, "char found! " + characteristic);
                break;
            }
        }

        if (characteristic == null) {
            Log.e(TAG, "char not found!");
            return false;
        }


        byte[] value = {(byte) 300, (byte) 100, (byte) 100};
        characteristic.setValue(value);

        boolean status = mGatt.writeCharacteristic(characteristic);
//
//
//        Log.d("OUT","status GATT -> " + status);
        return true;
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bt_connect:
                /** BluetoothGatt connectGatt(Context context, boolean autoConnect,BluetoothGattCallback callback)
                 * context : 普通に代入するだけ
                 * autoConnect : 自動的に接続するかしないか
                 * callback : 結果を返す．
                 */
                BluetoothGatt gatt = connectDev.connectGatt(getBaseContext(), false, mGattCallBack);

                break;
            case R.id.bt_disconnect:
                mGatt.disconnect();
                break;

            case R.id.bt_send:
                int size = mGatt.getServices().size();

                //                mGatt.writeDescriptor(new BluetoothGattDescriptor())
                writeCharacteristic();

                break;
        }
    }
}
