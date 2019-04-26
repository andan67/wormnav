package org.andan.android.connectiq.wormnav;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.exception.InvalidStateException;
import com.garmin.android.connectiq.exception.ServiceUnavailableException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IQSendMessageIntentService extends IntentService {

    public final static String INTENT_BUNDLE = "INTENT_BUNDLE";
    public final static String MESSAGE_DATA = "MESSAGE";
    public final static String RESULT_RECEIVER = "RESULT_RECEIVER";
    public final static String MESSAGE_STATUS = "MESSAGE_STATUS";
    public final static String IQ_DEVICE_NAME = "DEVICE_NAME";
    public final static String IQ_DEVICE = "IQ_DEVICE";
    public final static String IQ_APP_ID = "IQ_APP_ID";
    public final static String MESSAGE_SEND_TIME = "MESSAGE_SEND_TIME";
    public final static int MESSAGE_SEND_OK = 0;
    public final static int MESSAGE_SEND_NOT_OK = 1;


    private final String TAG = "IQSendMessage";

    public IQSendMessageIntentService() {
        super("IQSendMessageIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            Bundle intentBundle = intent.getBundleExtra(INTENT_BUNDLE);
            List<Object> message = (ArrayList<Object>) intentBundle.getSerializable(MESSAGE_DATA);
            IQDevice device = intentBundle.getParcelable(IQ_DEVICE);
            final ResultReceiver receiver = intentBundle.getParcelable(RESULT_RECEIVER);
            String appId = intentBundle.getString(IQ_APP_ID);

            IQApp app = new IQApp(appId);

            //ConnectIQ connectIQ = ConnectIQ.getInstance();
            ConnectIQ connectIQ = ConnectIQ.getInstance();

            final long sendTime = new Date().getTime();

            final Bundle bundle = new Bundle();
            bundle.putString(IQ_DEVICE_NAME, device.getFriendlyName());
            bundle.putLong(MESSAGE_SEND_TIME, sendTime);

            try {

                connectIQ.sendMessage(device, app, message, new ConnectIQ.IQSendMessageListener() {

                    // workaround to avoid double call of onMessageStatus
                    long lastSendTime = 0;
                    @Override
                    public void onMessageStatus(IQDevice device, IQApp app, ConnectIQ.IQMessageStatus status) {
                        Log.i(TAG, "onMessageStatus with status: " + status.name());
                        if ( lastSendTime != sendTime) {
                            lastSendTime = sendTime;
                            Log.i(TAG, "onMessageStatus: reciver.send");
                            bundle.putString(MESSAGE_STATUS, status.name());
                            receiver.send(MESSAGE_SEND_OK, bundle);
                        }
                    }

                });
            } catch (InvalidStateException e) {
                bundle.putString(IQSendMessageIntentService.MESSAGE_STATUS, "ConnectIQ is not in a valid state");
                receiver.send(MESSAGE_SEND_NOT_OK, bundle);
            } catch (ServiceUnavailableException e) {
                bundle.putString(IQSendMessageIntentService.MESSAGE_STATUS, "ConnectIQ service is unavailable");
                receiver.send(MESSAGE_SEND_NOT_OK, bundle);
            }
        }
    }
}