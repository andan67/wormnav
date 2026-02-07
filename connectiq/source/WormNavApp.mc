using Toybox.Application;
using Toybox.WatchUi;
using Toybox.Timer;
using Toybox.Attention as Att;
using Toybox.ActivityRecording;
using Toybox.Sensor;

using Track;
using Data;

var crashOnMessage = false;
var trackView;
var dataView;
var lapView;
var viewDelegate;
var session = null;
var sessionEvent = 0;
var activityType = ActivityRecording.SPORT_RUNNING;

var trackViewPeriod = 1;
var trackViewLargeFont = true;
var trackElevationPlot = true;
var trackStorage = true;
var lapViewPeriod = 10;
var trackViewCounter = 0;
var appTimerTicks = 0;

var isDarkMode = false;

var msgData = null;
var newTrackReceived = false;

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

        Data.setMaxHeartRate();
        // explicit enablement of heart rate sensor seems to be required to detect an external HRM
        Sensor.setEnabledSensors([Sensor.SENSOR_HEARTRATE]);

        var trackStats = getProperty("trackStats");
        if(trackStats != null) {
             Track.newTrack(trackStats,
                getProperty("trackXData"),
                getProperty("trackYData"),
                getProperty("trackEleData")
            );
        }

        if(getProperty("northHeading") != null) {
            Track.northHeading = getProperty("northHeading");
        }

        if(getProperty("centerMap") != null) {
            Track.centerMap=getProperty("centerMap");
        }

        if(getProperty("autolapDistance") != null) {
            Track.autolapDistance = getProperty("autolapDistance");
        }

        if(getProperty("breadCrumbNumber") != null) {
            Track.breadCrumbNumber = getProperty("breadCrumbNumber");
        }

        if(getProperty("breadCrumbDist") != null) {
            Track.breadCrumbDist = getProperty("breadCrumbDist");
        }

        if(getProperty("dataScreens") != null) {
            Data.setDataScreens(getProperty("dataScreens"));
        } else {
            Data.setDataScreens(Data.dataScreensDefault);
        }

        if(getProperty("activityType") != null) {
            activityType = getProperty("activityType");
        }

        if(getProperty("trackViewPeriod") != null) {
            trackViewPeriod = getProperty("trackViewPeriod");
        }

        if(getProperty("trackViewLargeFont") != null) {
            trackViewLargeFont = getProperty("trackViewLargeFont");
        }

        if(getProperty("trackElevationPlot") != null) {
            trackElevationPlot = getProperty("trackElevationPlot");
        }

        if(getProperty("trackStorage") != null) {
            trackStorage = getProperty("trackStorage");
        }

        if(getProperty("isDarkMode") != null) {
            isDarkMode = getProperty("isDarkMode");
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
        trackView = null;
        dataView = null;
        Track.deleteTrack();

        Position.enableLocationEvents(Position.LOCATION_DISABLE, method(:onPosition));
    }

    // Return the initial view of your application here
    function getInitialView() {
        trackView = new TrackView();
        if(Track.hasTrackData) {
            trackView.isNewTrack = true;
        }
        viewDelegate = new WormNavDelegate();
        if(Communications has :registerForPhoneAppMessages) {
            Communications.registerForPhoneAppMessages( method(:onPhone));
        }
        return [trackView, viewDelegate];
    }

    function clearTrackStorage() {
        setProperty("trackStats", null );
        setProperty("trackXData", null);
        setProperty("trackYData", null);
        setProperty("trackEleData", null);
    }

    function onPhone(msg) {
        try {
            // quick check if message is in correct format
            msgData = msg.data;
            if(msgData[0][2] instanceof Lang.Number) {
                newTrackReceived = true;
            }
        } catch( ex ) {
            msgData = null;
        }
    }

    // handles screen updates
    function onTimer() {
        appTimerTicks += 1;

        if(newTrackReceived) {
            // handle new track event here as this results in less peak memory
            newTrackReceived = false;
            Track.deleteTrack();
            clearTrackStorage();
            Track.newTrack(msgData[0], msgData[1],  msgData[2], msgData.size() == 4 ? msgData[3] : null );
            msgData = null;
            if(trackStorage) {
                setProperty("trackStats", Track.trackStats);
                setProperty("trackXData", Track.xArray);
                setProperty("trackYData", Track.yArray);
                if(Track.hasElevationData) {
                   setProperty("trackEleData", Track.eleArray);
                }
            }
            trackView.isNewTrack = true;
            trackView.showElevationPlot = false;
            page = -1;
            WatchUi.switchToView(trackView, viewDelegate, WatchUi.SLIDE_IMMEDIATE);
            return;
        }

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
        } else {
            WatchUi.requestUpdate();
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
