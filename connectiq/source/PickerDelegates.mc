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
         Trace.setAutolapDistance(distance);
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

class ActivityTypeDelegate extends WatchUi.PickerDelegate {

    function initialize() {
           PickerDelegate.initialize();
    }

    function onCancel() {
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
        $.activityType = values[0];
        Application.getApp().setProperty("activityType", $.activityType);
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }
}