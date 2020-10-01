using Toybox.WatchUi;
using Toybox.System;
using Toybox.ActivityRecording;
using Trace;


class MainMenuDelegate extends WatchUi.MenuInputDelegate {

    function initialize() {
        MenuInputDelegate.initialize();
    }

    function onMenuItem(item) {
        var picker;

        var factory;
        var defaultValue;
        switch(item) {
            case :delete:
                if(track!=null) {
                    var message = "Continue?";
                    var dialog = new WatchUi.Confirmation(message);
                    WatchUi.pushView(
                        dialog,
                        new DeleteConfirmationDelegate(),
                        WatchUi.SLIDE_IMMEDIATE
                    );
                }
                return true;
            case :north:
                defaultValue = Transform.northHeading;
                 factory =  new GenericListItemFactory(
                     [true,false],["North\nup","Move-\nment"],{:font => Graphics.FONT_MEDIUM});
                 picker = new GenericListItemPicker("Map orientation", [factory], [defaultValue], null);
                WatchUi.pushView(picker, new MapOrientationPickerDelegate(), WatchUi.SLIDE_IMMEDIATE);
                return true;
             case :center:
                 defaultValue = Transform.centerMap;
                 factory =  new GenericListItemFactory(
                     [true,false],["on","off"],{:font => Graphics.FONT_LARGE});
                 picker = new GenericListItemPicker("Center map", [factory], [defaultValue], null);
                WatchUi.pushView(picker, new CenterMapPickerDelegate(), WatchUi.SLIDE_IMMEDIATE);
                return true;
            case :autolap:
                 defaultValue = Trace.autolapDistance;
                 factory = new GenericListItemFactory(
                    [0.0,100.0,200.0,400.0,500.0,1000.0,2000.0,5000.0],
                     ["off","100m","200m","400m","500m","1km","2km","5km"],
                    null);
                picker = new GenericListItemPicker("Auto lap", [factory], [defaultValue], null);
                 WatchUi.pushView(picker, new AutoLapPickerDelegate(), WatchUi.SLIDE_IMMEDIATE);
                return true;
            case :breadCrumbs:
                defaultValue = Trace.breadCrumbDist;
                factory = new GenericListItemFactory(
                    [0.0,100.0,200.0,400.0,500.0,1000.0,2000.0,5000.0],
                     ["off","100m","200m","400m","500m","1km","2km","5km"],
                    null);
                picker = new GenericListItemPicker("Bread crumbs", [factory], [defaultValue], null);
                WatchUi.pushView(picker, new WormNavBreadCrumbsPickerDelegate(), WatchUi.SLIDE_UP);
                return true;
            case :activityType:
                defaultValue = $.activityType;
                factory = new GenericListItemFactory(
                    [ActivityRecording.SPORT_GENERIC, ActivityRecording.SPORT_RUNNING, ActivityRecording.SPORT_WALKING, ActivityRecording.SPORT_CYCLING],
                    ["Generic","Running","Walking","Cycling"],
                    {:font => Graphics.FONT_SMALL});
                picker = new GenericListItemPicker("Activity type", [factory], [defaultValue], null);
                WatchUi.pushView(picker, new ActivityTypeDelegate(), WatchUi.SLIDE_UP);
                return true;
            case :screens:
                WatchUi.pushView(new Rez.Menus.DataScreens(), new DataMenuDelegate(), WatchUi.SLIDE_UP);
                return true;
            default:
                return false;
        }
    }
}

class DeleteConfirmationDelegate extends WatchUi.ConfirmationDelegate {
    function initialize() {
        ConfirmationDelegate.initialize();
    }

    function onResponse(response) {
        if (response == WatchUi.CONFIRM_NO) {
            System.println("Cancel");
        } else {
            System.println("Confirm");
            track=track.clean();
            Transform.setViewCenter(Trace.lat_last_pos,Trace.lon_last_pos);
            track=null;
            Trace.reset();
        }
    }
}

