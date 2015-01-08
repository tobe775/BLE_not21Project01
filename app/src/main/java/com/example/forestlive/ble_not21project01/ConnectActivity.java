package com.example.forestlive.ble_not21project01;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


public class ConnectActivity extends ActionBarActivity implements View.OnClickListener, BLECentralManager.onCentraListener {

    private final String TAG = "ConnectActivity";

    private Handler mHandler = null;

    private BLECentralManager mCentralManager = null;

    // Layout
    private Button bt_connect = null;
    private Button bt_disconnect = null;
    private Button bt_send = null;
    private Button bt_write = null;
    private ListView lv_info = null;
    private ArrayAdapter<String> mListAdapter = null;


    private enum LayoutType {
        View,
        List,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connectdevice);

        init();
        initLayout();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCentralManager.onDestroy();
        finish();
    }

    private void init() {
        mHandler = new Handler();
        mCentralManager = new BLECentralManager(this, this);
    }

    private void initLayout() {
        // Button
        bt_connect = (Button) findViewById(R.id.bt_connect);
        bt_connect.setOnClickListener(this);
        bt_disconnect = (Button) findViewById(R.id.bt_disconnect);
        bt_disconnect.setOnClickListener(this);
        bt_send = (Button) findViewById(R.id.bt_read);
        bt_send.setOnClickListener(this);
        bt_write = (Button) findViewById(R.id.bt_write);
        bt_write.setOnClickListener(this);

        // List
        lv_info = (ListView) findViewById(R.id.lv_info);
        mListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        lv_info.setAdapter(mListAdapter);
    }

    synchronized private void changeLayout(final LayoutType ltype, final Object obj) {
        Log.d("OUT", "changeLayout");

        mHandler.post(new Runnable() {
            public void run() {
                Log.d("OUT", "changeLayout3");
                if (ltype == LayoutType.View) {
                    Log.d(TAG, "state -> View ");
                    GattState state = (GattState) obj;

                    switch (state) {
                        case ConnectType:
                            bt_connect.setEnabled(false);
                            bt_connect.setTextColor(Color.RED);
                            bt_disconnect.setEnabled(true);
                            bt_disconnect.setTextColor(Color.BLUE);

                            bt_send.setEnabled(true);
                            bt_write.setEnabled(true);
                            break;

                        case DisconnectType:
                            bt_connect.setEnabled(true);
                            bt_connect.setTextColor(Color.BLUE);
                            bt_disconnect.setEnabled(false);
                            bt_disconnect.setTextColor(Color.RED);

                            bt_send.setEnabled(false);
                            bt_write.setEnabled(false);
                            break;

                        case BeingTYPE:
                            bt_connect.setEnabled(false);
                            bt_connect.setTextColor(Color.RED);
                            bt_disconnect.setEnabled(false);
                            bt_disconnect.setTextColor(Color.RED);

                            bt_send.setEnabled(false);
                            bt_write.setEnabled(false);
                            break;
                    }
                } else if (ltype == LayoutType.List) {
                    Log.d(TAG, "state -> List ");
                    Log.d(TAG, "LayoutType str -> " + obj);
                    mListAdapter.add(obj.toString());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bt_connect:
                mCentralManager.connect();
                changeLayout(LayoutType.View, GattState.BeingTYPE);
                break;
            case R.id.bt_disconnect:
                mCentralManager.discconect();
                break;

            case R.id.bt_read:
                mCentralManager.readCharacteristic();
                break;

            case R.id.bt_write:
                mCentralManager.writeCharacteristic();
                break;
        }
    }

    @Override
    public void onStateChange(GattState type) {
        changeLayout(LayoutType.View, type);
    }

    @Override
    public void onCharacteristicRead(String str) {
        Log.d(TAG, "str -> " + str);
        changeLayout(LayoutType.List, str);
    }

    @Override
    public void onCharacteristicWrite(String str) {
        changeLayout(LayoutType.List, str);
    }
}
