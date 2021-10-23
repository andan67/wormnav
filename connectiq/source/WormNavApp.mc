using Toybox.Application;
using Toybox.WatchUi;
using Toybox.Timer;
using Toybox.Attention as Att;
using Toybox.ActivityRecording;
using Toybox.Sensor;

using Track;
using Data;

var mailMethod;
var phoneMethod;
var crashOnMessage = false;
var trackView;
var dataView;
var lapView;
var viewDelegate;
var device = null;
var track = null;
var session = null;
var sessionEvent = 0;
var activityType = ActivityRecording.SPORT_RUNNING;

var trackViewPeriod = 1;
var trackViewLargeFont = false;
var trackElevationPlot = false;
var dataViewPeriod = 1;
var lapViewPeriod = 10;
var trackViewCounter = 0;
var appTimerTicks = 0;

var isDarkMode = false;


// page == -1 -> track view with track
// page == 0 -> track view with elevation plot
// page >= 1 -> data view with sceen #page
var page = -1;

class WormNavApp extends Application.AppBase {
    var lapViewTicker = 0;
    var sessionEventTicker = 0;

    var appTimer;
    var vibrateData = [new Att.VibeProfile(  50, 250 )];

    function initialize() {
        AppBase.initialize();
    }

    // onStart() is called on application start up
    function onStart(state) {
        System.println("onStart");

        // vivoactive stands for all devices with round screen shape
        // ToDo: Implement resource/annotation based method to set device dependent settings
        if(System.getDeviceSettings().screenShape == System.SCREEN_SHAPE_ROUND) {
            device = "vivoactive";
        } else {
            device = "generic";
        }

        System.println("Device: " + device);

        Data.setMaxHeartRate();


        var data = Application.getApp().getProperty("trackData");

        // explicit enablement of heart rate sensor seems to be required to detect an external HRM
        Sensor.setEnabledSensors([Sensor.SENSOR_HEARTRATE]);

        if(data != null) {
            System.println("load data from property store");
            track = new TrackModel(data);
        }

        if(Application.getApp().getProperty("northHeading") != null) {
            Track.northHeading=Application.getApp().getProperty("northHeading");
        }

        if(Application.getApp().getProperty("centerMap") != null) {
            Track.centerMap=Application.getApp().getProperty("centerMap");
        }

        if(Application.getApp().getProperty("autolapDistance") != null) {
            Track.autolapDistance = Application.getApp().getProperty("autolapDistance");
        }

        if(Application.getApp().getProperty("breadCrumbNumber") != null) {
            Track.breadCrumbNumber = Application.getApp().getProperty("breadCrumbNumber");
        }

        if(Application.getApp().getProperty("breadCrumbDist") != null) {
            Track.breadCrumbDist = Application.getApp().getProperty("breadCrumbDist");
        }

        if(Application.getApp().getProperty("dataScreens") != null) {
            Data.setDataScreens(Application.getApp().getProperty("dataScreens"));
        } else {
            Data.setDataScreens(Data.dataScreensDefault);
        }

        if(Application.getApp().getProperty("activityType") != null) {
            activityType = Application.getApp().getProperty("activityType");
        }

        if(Application.getApp().getProperty("trackViewPeriod") != null) {
            trackViewPeriod = Application.getApp().getProperty("trackViewPeriod");
        }

        if(Application.getApp().getProperty("trackViewLargeFont") != null) {
            trackViewLargeFont = Application.getApp().getProperty("trackViewLargeFont");
        }

        if(Application.getApp().getProperty("trackElevationPlot") != null) {
            trackElevationPlot = Application.getApp().getProperty("trackElevationPlot");
        }

        if(Application.getApp().getProperty("isDarkMode") != null) {
            isDarkMode = Application.getApp().getProperty("isDarkMode");
        }

        Position.enableLocationEvents(Position.LOCATION_CONTINUOUS, method(:onPosition));

        // timer is used for data fields and auto lap
        appTimer = new Timer.Timer();
        appTimer.start(method(:onTimer), 1000, true);
    }


    function onPosition(info) {
        // Delegates callback to module function
        Track.onPosition(info);
    }

    // onStop() is called when your application is exiting
    function onStop(state) {
        Position.enableLocationEvents(Position.LOCATION_DISABLE, method(:onPosition));
    }

    // Return the initial view of your application here
    function getInitialView() {
        trackView = new TrackView();
        if(track !=  null) {
            trackView.isNewTrack = true;
        }
        viewDelegate = new WormNavDelegate();
        phoneMethod = method(:onPhone);
        if(Communications has :registerForPhoneAppMessages) {
            Communications.registerForPhoneAppMessages(phoneMethod);
        } else {
            Communications.setMailboxListener(mailMethod);
        }
        return [trackView, viewDelegate];
    }

    function onPhone(msg) {
        System.println("onPhone(msg)");
        try {
            // Property should be explicitly deleted. 
            // Otherwise OOM excception is thrown when message contains elevation data (due to complexity of msg?)
            if (track != null) {
                track.clear();
                track = null;
            }
            Application.getApp().deleteProperty("trackData");
            Application.getApp().setProperty("trackData", msg.data);
            track = new TrackModel(msg.data);
            $.trackView.isNewTrack = true;
            page = -1;
            $.trackView.showElevationPlot = false;
            WatchUi.switchToView($.trackView, viewDelegate, WatchUi.SLIDE_IMMEDIATE);
            //WatchUi.requestUpdate();
        }
        catch( ex ) {
            System.println(ex.getErrorMessage());
            track = null;
            System.exit();
        }
    }

    // handles screen updates
    function onTimer() {
        appTimerTicks += 1;

        // used to handle session start/stop events in UI that live for 2 seconds
        if(sessionEvent > 0) {
            if(sessionEventTicker == 2) {
                // disable event
                sessionEvent = 0;
                sessionEventTicker = 0;
            } else {
                sessionEventTicker += 1;
            }
        }

        if(lapViewTicker == 0 && Track.isAutolap(false)) {
            // auto lap detected
            lapViewTicker = 1;
        }

        // in lapViewMode
        if(lapViewTicker > 0) {
            if(lapView == null) {
                lapView = new LapView();
            }
            if(lapViewTicker == 1) {
                WatchUi.pushView(lapView, viewDelegate, WatchUi.SLIDE_IMMEDIATE);
            }
            if(lapViewTicker == 2) {
                if (Attention has :vibrate) {
                    Att.vibrate( vibrateData );
                }
                if (Attention has :playTone) {
                    Attention.playTone(Attention.TONE_LAP );
                }
            }
            lapViewTicker++;

            if(lapViewTicker == lapViewPeriod) {
                lapViewTicker = 0;
                WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
            }
        }
        else if(page <= 0)  {
            // track view active
            if(trackViewCounter %  trackViewPeriod == 0) {
                WatchUi.requestUpdate();
            }
            trackViewCounter += 1;
        }
    }

     // split string str by separator char c into string array
    function split(str,c) {
        if(str != null && c != null) {
            var result = [];
            var charArray = str.toCharArray();
            var start = 0;
            var pos = 0;
            while (pos <= charArray.size()) {
                if(pos == charArray.size() || charArray[pos] == c) {
                    if(pos > start) {
                        result.add(str.substring(start, pos));
                    }
                    pos += 1;
                    start = pos;
                }
                pos += 1;
            }
            return result;
        }
        return null;
    }

}
