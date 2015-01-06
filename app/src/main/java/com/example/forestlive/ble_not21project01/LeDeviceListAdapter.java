package com.example.forestlive.ble_not21project01;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by forestlive on 2014/12/29.
 */
public class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
//    private LayoutInflater mInflator;
    private Context mContext = null;

    public LeDeviceListAdapter(Context context) {
        super();

        this.mContext = context;
        mLeDevices = new ArrayList<BluetoothDevice>();
//        mInflator = DeviceScanActivity.this.getLayoutInflater();
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = View.inflate(mContext,R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(i);

        if (device.getUuids() != null) {
            Log.d("OUT", "null? " + device.getUuids());
        } else {
            Log.d("OUT", "null! " + device.getUuids());
        }

        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText("unknown_device");
        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}