using Toybox.WatchUi;

class AutoLapPickerDelegate extends WatchUi.PickerDelegate {

    function initialize() {
        PickerDelegate.initialize();
    }
    
    function onCancel() {
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
     	var distance = values[0];
     	Trace.autolapDistance = distance;
        if(Trace.autolapDistance > 0 && $.session!=null && session.isRecording() && Activity.getActivityInfo()!=null) {
            var elapsedDistance = Activity.getActivityInfo().elapsedDistance;
            var elapsedTime = Activity.getActivityInfo().elapsedTime;
            if ( elapsedTime != null && elapsedTime > 0 && elapsedDistance != null  && elapsedDistance > 0) {
                Trace.lapInitTime = elapsedTime;
                Trace.lapInitDistance = elapsedDistance;
            }
        }
    	Application.getApp().setProperty("autolapDistance",distance);
    	WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }
}

class WormNavBreadCrumbsPickerDelegate extends WatchUi.PickerDelegate {

    function initialize() {
        PickerDelegate.initialize();
    }

	function onCancel() {
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
     	var distance = values[0];
     	Trace.breadCrumbDist = distance;
        Application.getApp().setProperty("breadCrumbDist", distance);
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }
}

class MapOrientationPickerDelegate extends WatchUi.PickerDelegate {

    function initialize() {
        PickerDelegate.initialize();
    }
    
    function onCancel() {
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
   		Transform.northHeading = values[0];
        Application.getApp().setProperty("northHeading", Transform.northHeading);
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }
}

class CenterMapPickerDelegate extends WatchUi.PickerDelegate {

    function initialize() {
       	PickerDelegate.initialize();
    }
    
    function onCancel() {
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
    	Transform.centerMap = values[0];
        Application.getApp().setProperty("centerMap", Transform.northHeading);
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }
}