using Toybox.Application;
using Toybox.WatchUi;
using Toybox.Timer;
using Toybox.Attention as Att;
using Trace;

var messageReceived = false;

var mailMethod;
var phoneMethod;
var crashOnMessage = false;
var mainView;
var dataView;
var lapView;
var viewDelegate;
var appTimer;

var track = null;
var session = null;

var lapViewCounter = 0;
var vibrateData = [new Att.VibeProfile(  50, 250 )];

class WormNavApp extends Application.AppBase {

    function initialize() {
        AppBase.initialize();
    }

    // onStart() is called on application start up
    function onStart(state) {
        System.println("onStart");

        // start page is map
        mode=TRACK_MODE;

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
        mainView = new WormNavView();
        if(track!= null) {
             mainView.isNewTrack=true;
        }
        viewDelegate = new WormNavDelegate();
        phoneMethod = method(:onPhone);
        if(Communications has :registerForPhoneAppMessages) {
            Communications.registerForPhoneAppMessages(phoneMethod);
        } else {
            Communications.setMailboxListener(mailMethod);
        }
        return [mainView, viewDelegate];
    }

    function onPhone(msg) {
        System.println("onPhone(msg)");
        messageReceived = true;
        mode=TRACK_MODE;
       	// test data field
       	if(msg.data.size() == 5) {
       		System.println("No data fields defined");
       	} else {
       		System.print("Defined data fields: ");
       		System.println(msg.data[5]);
       	}
       	System.println(msg.data);
        track = new TrackModel(msg.data);
        try {
            Application.getApp().setProperty("trackData", msg.data);
            $.mainView.isNewTrack=true;
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
        Transform.isTrackCentered = false;
        Transform.setPosition(info);
        Trace.new_pos(info.position.toRadians()[0],info.position.toRadians()[1]);
        if($.mode==TRACK_MODE) {
            WatchUi.requestUpdate();
        }
    }

     function onTimer() {
        if(lapViewCounter == 0 && Trace.isAutolap()) {
            lapViewCounter = 1;
        }

        // in lapViewMode
        if(lapViewCounter>0) {
            if(lapView == null) {
                lapView = new WormNavLapView();
            }
            if($.lapViewCounter==1) {
                WatchUi.pushView(lapView, viewDelegate, WatchUi.SLIDE_IMMEDIATE);
            }
            if($.lapViewCounter==2) {
                if (Attention has :vibrate) {
                    Att.vibrate( vibrateData );
                }
                if (Attention has :playTone) {
                    Attention.playTone(Attention.TONE_LAP );
                }
            }
            lapViewCounter++;

            if($.lapViewCounter==10) {
                $.lapViewCounter = 0;
                WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
            }
        }
        else if($.lapViewCounter == 0 && $.mode==DATA_MODE) {
            WatchUi.requestUpdate();
        }
    }
}
