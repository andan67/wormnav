class AutoLapPickerDelegate extends GenericListItemPickerDelegate {

    function initialize(picker) {
        GenericListItemPickerDelegate.initialize(picker);
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
    	Application.getApp().setProperty(autolapDistance,distance);
    	WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }
}

class WormNavBreadCrumbsPickerDelegate extends GenericListItemPickerDelegate {

    function initialize(picker) {
        GenericListItemPickerDelegate.initialize(picker);
    }

    function onAccept(values) {
     	var distance = values[0];
     	Trace.breadCrumbDist = distance;
        Application.getApp().setProperty("breadCrumbDist", Trace.autolapDistance);
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }
}

