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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private final String TAG = "MainActivity";

    // Regular
    private Handler mHandler = null;

    // BLE
    private BluetoothAdapter mAdapter = null;
    private BluetoothManager mManager = null;
    private boolean mScanning = true;

    private final long SCAN_PERIOD = 1000;

    // Layout
    private Button bt_scan = null;
    private ListView lv_list = null;
    private LeDeviceListAdapter mLeDeviceListAdapter = null;

    public final static String DEVICE_NAME = "device";
    public final static String DEVICE_ADDRESS = "address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        initLayout();
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

        for (BluetoothDevice device : mAdapter.getBondedDevices()) {
            mLeDeviceListAdapter.addDevice(device);
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String scanData = String.format("%d bytes / 0x%s", scanRecord.length, toHexString(scanRecord));


                    for (int k = 0; k > scanRecord.length; k++) {
                        System.out.printf("%02x", scanRecord[k]);
                    }
                    synchronized (this) {

                        mLeDeviceListAdapter.addDevice(device);
                        mLeDeviceListAdapter.notifyDataSetChanged();
                    }
                }
            });

            String scanData = String.format("%d bytes / 0x%s", scanRecord.length, toHexString(scanRecord));


            for (int k = 0; k > scanRecord.length; k++) {
                System.out.printf("%02x", scanRecord[k]);
            }

        }
    };

    synchronized private void scanLeDevice(boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = true;
                    mAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = false;
            mAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = true;
            mAdapter.stopLeScan(mLeScanCallback);
        }
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
        Intent intent = null;

        intent = new Intent(this, ConnectActivity.class);
        Info.CON_DEVICE_ADDRESS = mLeDeviceListAdapter.getDevice(position).getAddress();
        Info.CON_DEVICE_NAME = mLeDeviceListAdapter.getDevice(position).getName();

        startActivity(intent);
    }
}
