/**
 * Copyright (C) 2015 Garmin International Ltd.
 * Subject to Garmin SDK License Agreement and Wearables Application Developer Agreement.
 */
package org.andan.android.connectiq.wormnav;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import org.osmdroid.util.GeoPoint;

public class DeviceBrowserActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {



    public static final String MY_APP = "91da4791-78e5-4e58-b1e6-e6f423bb1984";

    public static final String TRACK_NAME="TRACK_NAME";
    public static final String TRACK_LENGTH="TRACK_LENGTH";
    public static final String GEO_POINTS="GEO_POINTS";

    private ConnectIQ mConnectIQ;
    private TextView mEmptyView;
    private IQDeviceAdapter mAdapter;
    private boolean mSdkReady = false;

    private ListView mListView;
    private TextView mDeviceName;
    private TextView mDeviceStatus;
    private TextView mOpenAppButton;
    private TextView mMessageStatus;
    private IQDevice mDevice;
    private List<IQDevice> mDeviceList;
    private IQApp mMyApp;
    private boolean mAppIsOpen;

    private float[] mTrackBoundingBox;
    private String mTrackName;
    private float mTrackLength;
    private int mTrackNumberOfPoints;
    private int maxPathWpt;
    private double maxPathError;

    private ArrayList<GeoPoint> mGeoPoints;
    private float[] mTrackPoints;

    private final String TAG = "DeviceBrowser";

    private IQDeviceEventListener mDeviceEventListener = new IQDeviceEventListener() {

        @Override
        public void onDeviceStatusChanged(IQDevice device, IQDeviceStatus status) {
            Log.d(TAG,"onDeviceStatusChanged():"  + status.name() );
            mAdapter.updateDeviceStatus(device, status);
        }

    };

    private ConnectIQListener mListener = new ConnectIQListener() {

        @Override
        public void onInitializeError(IQSdkErrorStatus errStatus) {
            if( null != mEmptyView )
                mEmptyView.setText(R.string.initialization_error + errStatus.name());
            mSdkReady = false;
        }

        @Override
        public void onSdkReady() {
            Log.d(TAG,"onSdkReady()");
            loadDevices();
            mSdkReady = true;
        }

        @Override
        public void onSdkShutDown() {
            Log.d(TAG,"onSdkShutDown()");
            mSdkReady = false;
        }

    };

    private ConnectIQ.IQOpenApplicationListener mOpenAppListener = new ConnectIQ.IQOpenApplicationListener() {
        @Override
        public void onOpenApplicationResponse(IQDevice device, IQApp app, ConnectIQ.IQOpenApplicationStatus status) {
            Toast.makeText(getApplicationContext(), "App Status: " + status.name(), Toast.LENGTH_SHORT).show();

            if (status == ConnectIQ.IQOpenApplicationStatus.APP_IS_ALREADY_RUNNING) {
                mAppIsOpen = true;
                mOpenAppButton.setText(R.string.open_app_already_open);
            } else {
                mAppIsOpen = false;
                mOpenAppButton.setText(R.string.open_app_open);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate:");
        setContentView(R.layout.activity_device_browser);
        mListView = (ListView) findViewById(android.R.id.list);

        mAdapter = new IQDeviceAdapter(this);
        mListView.setAdapter(mAdapter);
        View header = getLayoutInflater().inflate(R.layout.activity_device_browser_header2, null);
        mListView.addHeaderView(header);
        mListView.setOnItemClickListener(this);

        // Here we are specifying that we want to use a WIRELESS bluetooth connection.
        // We could have just called getInstance() which would by default create a version
        // for WIRELESS, unless we had previously gotten an instance passing TETHERED
        // as the connection type.
        //mConnectIQ = ConnectIQ.getInstance(this, IQConnectType.WIRELESS);

        Log.i(TAG,"onCreate: initialize ConnectIQ");
        if(!Utils.isDeviceEmulator()) mConnectIQ = ConnectIQ.getInstance(this, IQConnectType.WIRELESS);
        else mConnectIQ = ConnectIQ.getInstance(this, IQConnectType.TETHERED);
        // Initialize the SDK
        mConnectIQ.initialize(this, true, mListener);

        mEmptyView = (TextView)findViewById(android.R.id.empty);

        mMessageStatus = (TextView)findViewById(R.id.message_status_value_textView);

        mTrackName = getIntent().getStringExtra(TRACK_NAME);
        mTrackLength = getIntent().getFloatExtra(TRACK_LENGTH,0);
        mGeoPoints = getIntent().getParcelableArrayListExtra(GEO_POINTS);

        Log.d(TAG, "onCreate:" + mTrackLength);

    }

    @Override
    protected void onPause() {
        super.onPause();

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
            Log.d(TAG,"onResume()");
            if(mDeviceList == null) loadDevices();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // It is a good idea to unregister everything and shut things down to
        // release resources and prevent unwanted callbacks.
        Log.i(TAG,"onDestroy: shutdown ConnectIQ");
        try {
            mConnectIQ.unregisterAllForEvents();
            mConnectIQ.shutdown(this);
        } catch (InvalidStateException e) {
            // This is usually because the SDK was already shut down
            // so no worries.
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
        if (id == R.id.load_devices) {
            Log.d(TAG,"onOptionsItemSelected");
            loadDevices();
            /*
            try {
                mConnectIQ.unregisterAllForEvents();
                mConnectIQ.shutdown(this);
                mConnectIQ = ConnectIQ.getInstance(this, IQConnectType.TETHERED);
                // Initialize the SDK
                mConnectIQ.initialize(this, true, mListener);

            } catch (InvalidStateException e) {
                // This is usually because the SDK was already shut down
                // so no worries.
            }
            */
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView l, View v, int position, long id) {
        // -1 because of header
        if(position>0 && mSdkReady) {
            mDevice = mAdapter.getItem(position - 1);

            displaySendToDeviceDialog();
        }
    }

    public void loadDevices() {
        // Retrieve the list of known devices
        Log.d(TAG,"loadDevices");
        try {
            if(mDeviceList != null && mDeviceList.size()>0) {
                for (IQDevice device : mDeviceList) {
                    mConnectIQ.unregisterForDeviceEvents(device);
                }
            }
            // get new list
            mDeviceList = mConnectIQ.getKnownDevices();
            Log.d(TAG,mDeviceList.toString());
            if (mDeviceList != null) {
                mAdapter.setDevices(mDeviceList);
                // Let's register for device status updates.  By doing so we will
                // automatically get a status update for each device so we do not
                // need to call getStatus()
                for (IQDevice device : mDeviceList) {
                    mConnectIQ.registerForDeviceEvents(device, mDeviceEventListener);
                    //mAdapter.updateDeviceStatus(device, device.getStatus());
                    Log.d(TAG,device.getFriendlyName());
                    Log.d(TAG,device.getStatus().name());
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
            if( null != mEmptyView )
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
        textView.setText(String.format("%.3f", mTrackLength/1000));


        final EditText maxWptEditText = trackSendToDeviceLayout.findViewById(R.id.dialog_send_to_device_reduceMaxPoints);
        final EditText maxError = trackSendToDeviceLayout.findViewById(R.id.dialog_send_to_device_reduceMaxError);

        final CheckBox reduceCheckBox = trackSendToDeviceLayout.findViewById(R.id.reduceTrackCheckbox);

        if(Data.useDefaultOptimization) {
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

                        Intent intentService = new Intent(DeviceBrowserActivity.this, IQSendMessageIntentService.class);
                        maxPathWpt = 0;
                        if (reduceCheckBox.isChecked()) {
                            if (!maxWptEditText.getText().toString().isEmpty()) {
                                maxPathWpt = Integer.valueOf(maxWptEditText.getText().toString());
                            }
                            if (!maxError.getText().toString().isEmpty()) {
                                maxPathError = Double.valueOf(maxError.getText().toString());
                            }
                        }
                        float[][] trackData = SendToDeviceUtility.generateTrackPointsAndBoundingBox(mGeoPoints, maxPathWpt, maxPathError);
                        mTrackBoundingBox = trackData[0];
                        mTrackPoints = trackData[1];
                        mTrackNumberOfPoints = mTrackPoints.length/2;

                        List<Object> message = new ArrayList<>();
                        // Create lists from arrays
                        List<Object> dataAsList = new ArrayList<>();
                        for(int i=0; i< mTrackBoundingBox.length; i++) dataAsList.add(mTrackBoundingBox[i]);
                        message.add(dataAsList);
                        message.add(mTrackName);
                        message.add(mTrackLength);
                        message.add(mTrackNumberOfPoints);
                        dataAsList = new ArrayList();
                        for(int i=0; i< mTrackPoints.length; i++) dataAsList.add(mTrackPoints[i]);
                        message.add(dataAsList);

                        Bundle messageBundle = new Bundle();
                        messageBundle.putParcelable(IQSendMessageIntentService.IQ_DEVICE, mDevice);
                        messageBundle.putString(IQSendMessageIntentService.IQ_APP_ID,MY_APP );
                        messageBundle.putSerializable(IQSendMessageIntentService.MESSAGE_DATA, (Serializable) message);
                        messageBundle.putParcelable(IQSendMessageIntentService.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                super.onReceiveResult(resultCode, resultData);
                                // workaround to handle bug(?) that onMessageStatus is called twise with first status 'SUCCESS' and second status n'FAILUER_UNKNOWN'
                                long sendTime =  resultData.getLong(IQSendMessageIntentService.MESSAGE_SEND_TIME);
                                String deviceName = resultData.getString(IQSendMessageIntentService.IQ_DEVICE_NAME);
                                String status = resultData.getString(IQSendMessageIntentService.MESSAGE_STATUS);
                                //long sendTime = resultData.getLong(IQSendMessageIntentService.MESSAGE_SEND_TIME);
                                Log.i(TAG, "message status:" + status);
                                final String sendTimeAsString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sendTime);
                                if (resultCode == IQSendMessageIntentService.MESSAGE_SEND_OK) {
                                    mMessageStatus.setText("Last message sent to '" + deviceName +
                                            "' at '" + sendTimeAsString + "' with status '" + status + "'.");
                                } else {
                                    mMessageStatus.setText("Last message sent to '" + deviceName +
                                            "' at '" + sendTimeAsString + "' failed for reason '" + status + "'.");
                                }
                            }
                        });

                        intentService.putExtra(IQSendMessageIntentService.INTENT_BUNDLE, messageBundle);
                        DeviceBrowserActivity.this.startService(intentService);

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


}
