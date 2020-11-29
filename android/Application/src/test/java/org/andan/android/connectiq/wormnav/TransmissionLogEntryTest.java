package org.andan.android.connectiq.wormnav;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.*;

import java.util.ArrayList;

public class TransmissionLogEntryTest {

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

        ArrayList<TransmissionLogEntry> transmissionLogEntries = new ArrayList<>();
        TransmissionLogEntry transmissionLogEntry1 = new TransmissionLogEntry();

        transmissionLogEntry1.setTrackName("Track Name 1");
        transmissionLogEntry1.setNoTrackPointsOriginal(100);
        transmissionLogEntry1.setTrackLengthOriginal(1.1);
        transmissionLogEntry1.setOptimized(false);
        transmissionLogEntry1.setNoTrackPointsSent(100);
        transmissionLogEntry1.setTrackLengthSent(1.1);
        transmissionLogEntry1.setDeviceName("Device 1");
        transmissionLogEntry1.setSendTime(t1);
        transmissionLogEntry1.setStatusMessage("Message 1");
        transmissionLogEntry1.setStatusCode(1);

        TransmissionLogEntry transmissionLogEntry2 = new TransmissionLogEntry();
        transmissionLogEntry2.setTrackName("Track Name 2");
        transmissionLogEntry2.setNoTrackPointsOriginal(220);
        transmissionLogEntry2.setTrackLengthOriginal(2.2);
        transmissionLogEntry2.setOptimized(true);
        transmissionLogEntry2.setNoTrackPointsSent(200);
        transmissionLogEntry2.setTrackLengthSent(2.0);
        transmissionLogEntry2.setDeviceName("Device 2");
        transmissionLogEntry2.setSendTime(t2);
        transmissionLogEntry2.setStatusMessage("Message 2");
        transmissionLogEntry2.setStatusCode(2);

        transmissionLogEntries.add(transmissionLogEntry1);
        transmissionLogEntries.add(transmissionLogEntry2);

        Gson gson = new Gson();

        String transmissionLogEntriesJson = gson.toJson(transmissionLogEntries);
        System.out.println(transmissionLogEntriesJson);

        ArrayList<TransmissionLogEntry> transmissionLogEntries2 = gson.fromJson(transmissionLogEntriesJson, new TypeToken<ArrayList<TransmissionLogEntry>>() {}.getType());
        System.out.println(gson.toJson(transmissionLogEntries2));
    }
}