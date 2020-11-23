package org.andan.android.connectiq.wormnav;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.junit.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TransmissionProtocolEntryTest {

    @Test
    public void serdeTest() {

        long t1 = 11111111;
        long t2 = 22222222;
       /* try {
            SimpleDateFormat sf = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            t1 = sf.parse("2020-11-23 19:11:11").getTime();
            t2 = sf.parse("2020-11-23 19:22:22").getTime();
        }
        catch (Exception ex){

        }*/

        ArrayList<TransmissionProtocolEntry> transmissionProtocolEntries = new ArrayList<>();
        TransmissionProtocolEntry transmissionProtocolEntry1 = new TransmissionProtocolEntry();

        transmissionProtocolEntry1.setTrackName("Track Name 1");
        transmissionProtocolEntry1.setNoTrackPointsOriginal(100);
        transmissionProtocolEntry1.setTrackLengthOriginal(1.1);
        transmissionProtocolEntry1.setOptimized(false);
        transmissionProtocolEntry1.setNoTrackPointsSent(100);
        transmissionProtocolEntry1.setTrackLengthSent(1.1);
        transmissionProtocolEntry1.setDeviceName("Device 1");
        transmissionProtocolEntry1.setSendTime(t1);
        transmissionProtocolEntry1.setStatusMessage("Message 1");
        transmissionProtocolEntry1.setStatusCode(1);

        TransmissionProtocolEntry transmissionProtocolEntry2 = new TransmissionProtocolEntry();
        transmissionProtocolEntry2.setTrackName("Track Name 2");
        transmissionProtocolEntry2.setNoTrackPointsOriginal(220);
        transmissionProtocolEntry2.setTrackLengthOriginal(2.2);
        transmissionProtocolEntry2.setOptimized(true);
        transmissionProtocolEntry2.setNoTrackPointsSent(200);
        transmissionProtocolEntry2.setTrackLengthSent(2.0);
        transmissionProtocolEntry2.setDeviceName("Device 2");
        transmissionProtocolEntry2.setSendTime(t2);
        transmissionProtocolEntry2.setStatusMessage("Message 2");
        transmissionProtocolEntry2.setStatusCode(2);

        transmissionProtocolEntries.add(transmissionProtocolEntry1);
        transmissionProtocolEntries.add(transmissionProtocolEntry2);

        Gson gson = new Gson();

        String transmissionProtocolEntriesJson = gson.toJson(transmissionProtocolEntries);
        System.out.println(transmissionProtocolEntriesJson);

        ArrayList<TransmissionProtocolEntry> transmissionProtocolEntries2 = gson.fromJson(transmissionProtocolEntriesJson, new TypeToken<ArrayList<TransmissionProtocolEntry>>() {}.getType());
        System.out.println(gson.toJson(transmissionProtocolEntries2));
    }

}