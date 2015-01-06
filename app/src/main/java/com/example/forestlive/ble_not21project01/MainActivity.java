package com.example.forestlive.ble_not21project01;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    // Regular
    private Handler mHandler = null;

    // BLE
    private BluetoothAdapter mAdapter = null;
    private BluetoothManager mManager = null;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = null;
    private boolean mScanning = true;

    private static final long SCAN_PERIOD = 1000;

    // Layout
    private Button bt_scan = null;
    private ListView lv_list = null;
    private LeDeviceListAdapter mLeDeviceListAdapter = null;

    public final static String DEVICE_NAME ="device";
    public final static String DEVICE_ADDRESS ="address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLayout();
        init();
        initBLE();

    }

    private void initLayout() {
        bt_scan = (Button) findViewById(R.id.bt_scan);
        bt_scan.setOnClickListener(this);

        lv_list = (ListView) findViewById(R.id.lv_list);
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        lv_list.setAdapter(mLeDeviceListAdapter);
        lv_list.setOnItemClickListener(this);

    }

    private void init() {
        mHandler = new Handler();
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

        Toast.makeText(this, "YES", Toast.LENGTH_SHORT).show();
        for (BluetoothDevice device : mAdapter.getBondedDevices()) {
            mLeDeviceListAdapter.addDevice(device);
        }

        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String scanData = String.format("%d bytes / 0x%s", scanRecord.length,toHexString(scanRecord));

                        Log.d("OUT","Scan Dev => " + device.getName() + " " + scanData);

                        for(int k=0;k>scanRecord.length;k++){
                            System.out.printf("%02x", scanRecord[k]);
                        }
                        mLeDeviceListAdapter.addDevice(device);
                        mLeDeviceListAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mAdapter.stopLeScan(mLeScanCallback);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_scan:
                scanLeDevice(mScanning);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        scanLeDevice(mScanning);

        BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);

        Intent intent = new Intent(this,ConnectDevice.class);

        Info.CON_DEVICE_ADDRESS = device.getAddress();
        Info.CON_DEVICE_NAME = device.getName();
        startActivity(intent);
    }

    public static String toHexString(byte[] buffer) {
        StringBuilder result = new StringBuilder();
        for (byte b : buffer) {
            String temp = String.format("%x", (int) b & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            result.append(temp.toUpperCase());
        }

        return result.toString();
    }

}
