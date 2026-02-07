/**
 * Copyright (C) 2015 Garmin International Ltd.
 * Subject to Garmin SDK License Agreement and Wearables Application Developer Agreement.
 */
package org.andan.android.connectiq.wormnav;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.ConnectIQ.ConnectIQListener;
import com.garmin.android.connectiq.ConnectIQ.IQConnectType;
import com.garmin.android.connectiq.ConnectIQ.IQDeviceEventListener;
import com.garmin.android.connectiq.ConnectIQ.IQSdkErrorStatus;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.IQDevice.IQDeviceStatus;
import com.garmin.android.connectiq.exception.InvalidStateException;
import com.garmin.android.connectiq.exception.ServiceUnavailableException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceBrowserActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {


    public static final String MY_APP = "91da4791-78e5-4e58-b1e6-e6f423bb1984";

    public static final String TRACK_NAME = "TRACK_NAME";
    public static final String TRACK_LENGTH = "TRACK_LENGTH";

    public static final String TRANSMISSION_LOG_ENTRIES = "TRANSMISSION_PROTOCOL_ENTRIES";

    private SharedPreferences mPreferences;

    private ConnectIQ mConnectIQ;
    private TextView mEmptyView;
    private IQDeviceAdapter mAdapter;
    private boolean mSdkReady = false;

    private ListView mListView;
    private TextView mMessageStatus;
    private IQDevice mDevice;
    private List<IQDevice> mDeviceList;
    private IQApp mMyApp;
    private ExecutorService mExecutorService = null;
    private Handler mMainThreadHandler = null;
    private String mTrackName;
    private float mTrackLength;
    private int maxPathWpt;
    private double maxPathError;

    private List<TransmissionLogEntry> mTransmissionLogEntries;
    Gson gson;

    private List<GeoPoint> mGeoPoints;

    private final String TAG = "DeviceBrowser";

    private IQDeviceEventListener mDeviceEventListener = new IQDeviceEventListener() {

        @Override
        public void onDeviceStatusChanged(IQDevice device, IQDeviceStatus status) {
            Log.d(TAG, "onDeviceStatusChanged():" + status.name());
            mAdapter.updateDeviceStatus(device, status);
        }

    };

    private ConnectIQListener mListener = new ConnectIQListener() {

        @Override
        public void onInitializeError(IQSdkErrorStatus errStatus) {
            if (null != mEmptyView)
                mEmptyView.setText(R.string.initialization_error + errStatus.name());
            mSdkReady = false;
        }

        @Override
        public void onSdkReady() {
            Log.d(TAG, "onSdkReady()");
            loadDevices();
            mSdkReady = true;
        }

        @Override
        public void onSdkShutDown() {
            Log.d(TAG, "onSdkShutDown()");
            mSdkReady = false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate:");

        mPreferences = getPreferences(MODE_PRIVATE);

        String ts = mPreferences.getString(TRANSMISSION_LOG_ENTRIES, "");
        gson = new Gson();
        if (!ts.isEmpty()) {
            mTransmissionLogEntries = gson.fromJson(ts, new TypeToken<ArrayList<TransmissionLogEntry>>() {
            }.getType());
        } else {
            mTransmissionLogEntries = new ArrayList<>();
        }

        setContentView(R.layout.activity_device_browser);
        mListView = (ListView) findViewById(android.R.id.list);

        mAdapter = new IQDeviceAdapter(this);
        mListView.setAdapter(mAdapter);
        View header = getLayoutInflater().inflate(R.layout.activity_device_browser_header, null);
        mListView.addHeaderView(header);
        mListView.setOnItemClickListener(this);

        // Here we are specifying that we want to use a WIRELESS bluetooth connection.
        // We could have just called getInstance() which would by default create a version
        // for WIRELESS, unless we had previously gotten an instance passing TETHERED
        // as the connection type.
        //mConnectIQ = ConnectIQ.getInstance(this, IQConnectType.WIRELESS);

        Log.i(TAG, "onCreate: initialize ConnectIQ");
        if (!Utils.isDeviceEmulator())
            mConnectIQ = ConnectIQ.getInstance(this, IQConnectType.WIRELESS);
        else mConnectIQ = ConnectIQ.getInstance(this, IQConnectType.TETHERED);
        // Initialize the SDK
        mConnectIQ.initialize(this, true, mListener);

        mEmptyView = (TextView) findViewById(android.R.id.empty);

        mMessageStatus = (TextView) findViewById(R.id.message_status_value_textView);

        mTrackName = getIntent().getStringExtra(TRACK_NAME);
        mTrackLength = getIntent().getFloatExtra(TRACK_LENGTH, 0);
        mGeoPoints = Data.geoPointsForDevice;
        if (mGeoPoints == null || mGeoPoints.size() == 0) {
            Toast.makeText(getApplicationContext(), "No points to sent", Toast.LENGTH_SHORT).show();
            finish();
        }
        mExecutorService = Executors.newSingleThreadExecutor();
        mMainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        if (mTransmissionLogEntries != null) {
            preferencesEditor.putString(TRANSMISSION_LOG_ENTRIES, gson.toJson(mTransmissionLogEntries));
        }
        preferencesEditor.apply();
        try {
            if (mDeviceList != null) {
                // Let's register for device status updates.  By doing so we will
                // automatically get a status update for each device so we do not
                // need to call getStatus()
                for (IQDevice device : mDeviceList) {
                    mConnectIQ.unregisterForDeviceEvents(device);
                }
                mDeviceList = null;
            }

        } catch (InvalidStateException e) {
            // This generally means you forgot to call initialize(), but since
            // we are in the callback for initialize(), this should never happen
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSdkReady) {
            Log.d(TAG, "onResume()");
            if (mDeviceList == null) loadDevices();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExecutorService != null) {
            mExecutorService.shutdown();
            mExecutorService = null;
        }
        if (mMainThreadHandler != null) {
            mMainThreadHandler = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_browser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.load_devices:
                Log.d(TAG, "onOptionsItemSelected");
                loadDevices();
                break;
            case R.id.show_log_entries:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.transmission_log)).setCancelable(true);
                ArrayList<String> logStrings = TransmissionLogEntry.toStringArray(mTransmissionLogEntries);
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, logStrings);
                builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getApplicationContext(),"You have selected " + logStrings.get(which),Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                AlertDialog dialog = builder.create();
                // Set the divider color of alert dialog list view
                ListView listView = dialog.getListView();
                listView.setDivider(new ColorDrawable(Color.GRAY));
                listView.setDividerHeight(2);
                dialog.show();

        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView l, View v, int position, long id) {
        // -1 because of header
        if (position > 0 && mSdkReady) {
            mDevice = mAdapter.getItem(position - 1);
            if (true || mDevice.getStatus() == IQDeviceStatus.CONNECTED) {
                displaySendToDeviceDialog();
            }
        }
    }

    public void loadDevices() {
        // Retrieve the list of known devices
        Log.d(TAG, "loadDevices");
        try {
            if (mDeviceList != null && mDeviceList.size() > 0) {
                for (IQDevice device : mDeviceList) {
                    mConnectIQ.unregisterForDeviceEvents(device);
                }
            }
            // get new list
            mDeviceList = mConnectIQ.getKnownDevices();
            Log.d(TAG, mDeviceList.toString());
            if (mDeviceList != null) {
                mAdapter.setDevices(mDeviceList);
                // Let's register for device status updates.  By doing so we will
                // automatically get a status update for each device so we do not
                // need to call getStatus()
                for (IQDevice device : mDeviceList) {
                    device.setStatus(mConnectIQ.getDeviceStatus(device));
                    mConnectIQ.registerForDeviceEvents(device, mDeviceEventListener);
                    //mAdapter.updateDeviceStatus(device, device.getStatus());
                    Log.d(TAG, device.getFriendlyName());
                    Log.d(TAG, device.getStatus().name());
                    //mAdapter.notifyDataSetInvalidated();
                }
            }

        } catch (InvalidStateException e) {
            // This generally means you forgot to call initialize(), but since
            // we are in the callback for initialize(), this should never happen
        } catch (ServiceUnavailableException e) {
            // This will happen if for some reason your app was not able to connect
            // to the ConnectIQ service running within Garmin Connect Mobile.  This
            // could be because Garmin Connect Mobile is not installed or needs to
            // be upgraded.
            if (null != mEmptyView)
                mEmptyView.setText(R.string.service_unavailable);
        }
    }

    private void displaySendToDeviceDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View trackSendToDeviceLayout = inflater.inflate(R.layout.send_to_device_dialog, null);
        TextView textView;

        textView = (TextView) trackSendToDeviceLayout.findViewById(R.id.dialog_send_to_device_track_name_value);
        textView.setText(mTrackName);

        textView = (TextView) trackSendToDeviceLayout.findViewById(R.id.dialog_send_to_device_track_length_value);
        textView.setText(String.format("%.3f", mTrackLength / 1000));

        final CheckBox invertCourseCheckBox = trackSendToDeviceLayout.findViewById(R.id.invertRoute);

        final CheckBox sendElevationDataCheckBox = trackSendToDeviceLayout.findViewById(R.id.sendElevationData);
        sendElevationDataCheckBox.setChecked(Data.useDefaultSendElevationData);

        final CheckBox reduceCheckBox = trackSendToDeviceLayout.findViewById(R.id.reduceTrackCheckbox);
        final EditText maxWptEditText = trackSendToDeviceLayout.findViewById(R.id.dialog_send_to_device_reduceMaxPoints);
        final EditText maxError = trackSendToDeviceLayout.findViewById(R.id.dialog_send_to_device_reduceMaxError);

        if (Data.useDefaultOptimization) {
            maxPathWpt = Data.defaultMaxPathWpt;
            maxPathError = Data.defaultMaxPathError;
        } else {
            maxPathWpt = mGeoPoints.size();
            maxPathError = 10d;
        }
        reduceCheckBox.setChecked(Data.useDefaultOptimization);
        maxWptEditText.setEnabled(Data.useDefaultOptimization);
        maxError.setEnabled(Data.useDefaultOptimization);

        maxWptEditText.setText(String.valueOf(maxPathWpt));
        maxError.setText(String.valueOf(maxPathError));


        reduceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maxWptEditText.setEnabled(isChecked);
                maxError.setEnabled(isChecked);
            }
        });

        mMyApp = new IQApp(MY_APP);

        String cancelText = getResources().getString(R.string.dialog_cancel);
        String dialogTitle = getResources().getString(R.string.dialog_send_to_device_title);
        String okText = getResources().getString(R.string.dialog_send_to_device_apply);

        builder.setTitle(dialogTitle)
                .setIcon(R.drawable.bar_sync)
                .setView(trackSendToDeviceLayout)
                .setCancelable(true)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        maxPathWpt = 0;
                        if (reduceCheckBox.isChecked()) {
                            if (!maxWptEditText.getText().toString().isEmpty()) {
                                maxPathWpt = Integer.valueOf(maxWptEditText.getText().toString());
                            }
                            if (!maxError.getText().toString().isEmpty()) {
                                maxPathError = Double.valueOf(maxError.getText().toString());
                            }
                        }
                        List<Object> message = SendToDeviceUtility.generateMessageForDevice(mGeoPoints, mTrackLength, mTrackName, maxPathWpt, maxPathError,
                                invertCourseCheckBox.isChecked(), sendElevationDataCheckBox.isChecked());
                        final TransmissionLogEntry logEntry = new TransmissionLogEntry();
                        logEntry.setTrackName(mTrackName);
                        logEntry.setTrackLengthOriginal(mTrackLength);
                        logEntry.setNoTrackPointsOriginal(mGeoPoints.size());

                        logEntry.setOptimized(maxPathWpt > 0);
                        logEntry.setTrackLengthSent((float) ((List<Object>) message.get(0)).get(1));
                        logEntry.setNoTrackPointsSent((int) ((List<Object>) message.get(0)).get(2));

                        final long sendTime = new Date().getTime();

                        if (mExecutorService != null) {
                            mExecutorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    int statusCode = SendToDeviceUtility.MESSAGE_SEND_OK;
                                    try {

                                        ConnectIQ connectIQ = ConnectIQ.getInstance();
                                        connectIQ.sendMessage(mDevice, mMyApp, message, new ConnectIQ.IQSendMessageListener() {

                                            long lastSendTime = 0;

                                            @Override
                                            public void onMessageStatus(IQDevice device, IQApp app, ConnectIQ.IQMessageStatus status) {
                                                if (lastSendTime != sendTime) {
                                                    lastSendTime = sendTime;
                                                    Log.i(TAG, "onMessageStatus with status: " + status.name());
                                                    onMessageSend(logEntry, mDevice.getFriendlyName(), sendTime,
                                                            status == ConnectIQ.IQMessageStatus.SUCCESS ? SendToDeviceUtility.MESSAGE_SEND_OK : SendToDeviceUtility.MESSAGE_SEND_NOT_OK,
                                                            status.name());
                                                }
                                            }


                                        });
                                    } catch (InvalidStateException e) {
                                        Log.e(TAG, "ConnectIQ is not in a valid state");
                                        onMessageSend(logEntry, mDevice.getFriendlyName(),
                                                sendTime, SendToDeviceUtility.MESSAGE_SEND_NOT_OK, "ConnectIQ is not in a valid state");
                                    } catch (ServiceUnavailableException e) {
                                        Log.e(TAG, "ConnectIQ service is unavailable");
                                        onMessageSend(logEntry, mDevice.getFriendlyName(),
                                                sendTime, SendToDeviceUtility.MESSAGE_SEND_NOT_OK, "ConnectIQ service is unavailable");
                                    }
                                }
                            });
                        }
                    }
                })
                .setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        final AlertDialog alert = builder.create();

        if (alert.getWindow() != null) {
            alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        alert.show();
    }

    private void onMessageSend(TransmissionLogEntry logEntry, String deviceName, long sendTime, int statusCode, String statusMessage) {
        if (mMainThreadHandler != null) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    logEntry.setSendTime(sendTime);
                    logEntry.setDeviceName(deviceName);
                    logEntry.setStatusCode(statusCode);
                    logEntry.setStatusMessage(statusMessage);
                    logEntry.addToConstraintList(mTransmissionLogEntries, 20);
                    mMessageStatus.setText(logEntry.toLogString());
                }
            });
        }
    }
}
