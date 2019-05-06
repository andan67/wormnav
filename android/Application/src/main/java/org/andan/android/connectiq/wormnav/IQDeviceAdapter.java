/**
 * Copyright (C) 2015 Garmin International Ltd.
 * Subject to Garmin SDK License Agreement and Wearables Application Developer Agreement.
 */
package org.andan.android.connectiq.wormnav;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.IQDevice.IQDeviceStatus;

import java.util.List;

public class IQDeviceAdapter extends ArrayAdapter<IQDevice> {

    private LayoutInflater mInflater;

    public IQDeviceAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_2);

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        IQDevice device = getItem(position);
        String friendly = device.getFriendlyName();
        ((TextView)convertView.findViewById(android.R.id.text1)).setText((friendly == null) ? device.getDeviceIdentifier() + "" : device.getFriendlyName());
        ((TextView)convertView.findViewById(android.R.id.text2)).setText(device.getStatus().name());

        return convertView;
    }

    public void setDevices(List<IQDevice> devices) {
        clear();
        addAll(devices);
        notifyDataSetChanged();
    }

    public synchronized void updateDeviceStatus(IQDevice device, IQDeviceStatus status) {

        int numItems = this.getCount();
        for(int i = 0; i < numItems; i++) {
            IQDevice local = getItem(i);
            if (local.getDeviceIdentifier() == device.getDeviceIdentifier()) {
                local.setStatus(status);
                notifyDataSetChanged();
                return;
            }
        }
    }
}
