using Toybox.Application;
using Toybox.WatchUi;
using Toybox.Timer;
using Toybox.Attention as Att;
using Trace;

var messageReceived = false;

var mailMethod;
var phoneMethod;
var crashOnMessage = false;
var trackView;
var dataView;
var lapView;
var viewDelegate;
var appTimer;
var device = "generic";
var track = null;
var session = null;

var vibrateData = [new Att.VibeProfile(  50, 250 )];

class WormNavApp extends Application.AppBase {

    private var lastPositionTime = System.getTimer();
    var lapViewCounter = 0;
    
    function initialize() {
        AppBase.initialize();
    }

    // onStart() is called on application start up
    function onStart(state) {
        System.println("onStart");

        // start page is map
        mode=TRACK_MODE;
        device = WatchUi.loadResource(Rez.Strings.device);
        System.println("Device: " + device);
        var data= Application.getApp().getProperty("trackData");
      
        if(data!=null) {
            System.println("load data from property store");
            track = new TrackModel(data);
            System.println("Created track from property store!");
        }

        if(Application.getApp().getProperty("northHeading")!=null) {
            Transform.northHeading=Application.getApp().getProperty("northHeading");
        }

        if(Application.getApp().getProperty("centerMap")!=null) {
            Transform.centerMap=Application.getApp().getProperty("centerMap");
        }

        if(Application.getApp().getProperty("autolapDistance")!=null) {
            Trace.autolapDistance = Application.getApp().getProperty("autolapDistance");
        }

        if(Application.getApp().getProperty("breadCrumbDist")!=null) {
            Trace.breadCrumbDist = Application.getApp().getProperty("breadCrumbDist");
        }
        
        if(Application.getApp().getProperty("dataScreens")!=null) {
            Data.setDataScreens(Application.getApp().getProperty("dataScreens"));
        } else {
            Data.setDataScreens(Data.dataScreensDefault);
        }
        

        Position.enableLocationEvents(Position.LOCATION_CONTINUOUS, method(:onPosition));

        // timer is used for data fields and auto lap
        appTimer = new Timer.Timer();
        appTimer.start(method(:onTimer), 1000, true);
    }

    // onStop() is called when your application is exiting
    function onStop(state) {
        Position.enableLocationEvents(Position.LOCATION_DISABLE, method(:onPosition));
    }

    // Return the initial view of your application here
    function getInitialView() {
        trackView = new TrackView();
        if(track!= null) {
             trackView.isNewTrack=true;
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
        messageReceived = true;
        mode=TRACK_MODE;
        track = new TrackModel(msg.data);
        try {
            Application.getApp().setProperty("trackData", msg.data);
            $.trackView.isNewTrack=true;
            WatchUi.requestUpdate();
        }
        catch( ex ) {
            System.println(ex.getErrorMessage());
            track=null;
            System.exit();
        }
    }

    function onPosition(info) {
        //onTimer();
           lastPositionTime = System.getTimer();
        Trace.newLatLonPosition(info.position.toRadians()[0].toFloat(),info.position.toRadians()[1].toFloat());
        if($.mode==TRACK_MODE) {
            WatchUi.requestUpdate();
        }
    }

    function onTimer() {
    
        if(lapViewCounter == 0 && Trace.isAutolap(false)) {
            // auto lap detected
            lapViewCounter = 1;
        }

        // in lapViewMode
        if(lapViewCounter>0) {
            if(lapView == null) {
                lapView = new LapView();
            }
            if(lapViewCounter==1) {
                WatchUi.pushView(lapView, viewDelegate, WatchUi.SLIDE_IMMEDIATE);
            }
            if(lapViewCounter==2) {
                if (Attention has :vibrate) {
                    Att.vibrate( vibrateData );
                }
                if (Attention has :playTone) {
                    Attention.playTone(Attention.TONE_LAP );
                }
            }
            lapViewCounter++;

            if(lapViewCounter==10) {
                lapViewCounter = 0;
                WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
            }
        }
        else if(lapViewCounter == 0 && 
                ($.mode==DATA_MODE || (System.getTimer()-lastPositionTime > 2000) )) {
            WatchUi.requestUpdate();
        }
    }
}
