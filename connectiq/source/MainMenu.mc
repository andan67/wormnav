
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
    	var text_label;
        
        var distanceValues = [0.0,100.0,200.0,400.0,500.0,1000.0,2000.0,5000.0];
        var distanceLabels = ["off","100m","200m","400m","500m","1km","2km","5km"];

        switch(item) {
            
            case :delete:
                if(track!=null) {
                    var message = WatchUi.loadResource(Rez.Strings.msg_continue);
                    var dialog = new WatchUi.Confirmation(message);
                    WatchUi.pushView(
                        dialog,
                        new DeleteConfirmationDelegate(),
                        WatchUi.SLIDE_IMMEDIATE
                    );
                }
                return true;
            case :orient:
                if(Transform.northHeading) {
                    defaultValue = Transform.centerMap? 2 : 1;
                } else {
                    defaultValue = 0;
                }
                factory =  new GenericListItemFactory(
                    [0,1,2],
                    WatchUi.loadResource(Rez.Strings.orient_opts),
                    {:font => $.device.equals("vivoactive") ? Graphics.FONT_SMALL: Graphics.FONT_MEDIUM});
				text_label = WatchUi.loadResource(Rez.Strings.mm_orient_s);
				picker = new GenericListItemPicker(text_label, [factory], [defaultValue]);
                WatchUi.pushView(picker, new GenericPickerDelegate(:orient), WatchUi.SLIDE_IMMEDIATE);
                return true;
            case :background:
                defaultValue = $.isDarkMode;
                factory =  new GenericListItemFactory(
                    [true,false],
                     WatchUi.loadResource(Rez.Strings.color_opts),
                    {:font => Graphics.FONT_SMALL});
                text_label = WatchUi.loadResource(Rez.Strings.mm_bgc);
                picker = new GenericListItemPicker(text_label, [factory], [defaultValue]);
                WatchUi.pushView(picker, new GenericPickerDelegate(:background), WatchUi.SLIDE_IMMEDIATE);
                return true;    
            case :autolap:
                defaultValue = Trace.autolapDistance;
                factory = new GenericListItemFactory(
                    distanceValues,
                    distanceLabels,
                    null);
                text_label = WatchUi.loadResource(Rez.Strings.mm_al);
				picker = new GenericListItemPicker(text_label, [factory], [defaultValue]);
                WatchUi.pushView(picker, new GenericPickerDelegate(:autolap), WatchUi.SLIDE_IMMEDIATE);
                return true;
            case :breadcrumbs:
                WatchUi.pushView(new Rez.Menus.BreadcrumbsMenu(), new BreadcrumbsMenuDelegate(), WatchUi.SLIDE_UP);
                return true;
            case :activity:
                defaultValue = $.activityType;
                factory = new GenericListItemFactory(
                    [ActivityRecording.SPORT_GENERIC, ActivityRecording.SPORT_RUNNING, ActivityRecording.SPORT_WALKING, ActivityRecording.SPORT_CYCLING],
                    WatchUi.loadResource(Rez.Strings.activities),
                    {:font => $.device.equals("vivoactive") ? Graphics.FONT_XTINY: Graphics.FONT_SMALL});
                text_label = WatchUi.loadResource(Rez.Strings.mm_type);
				picker = new GenericListItemPicker(text_label, [factory], [defaultValue]);
				WatchUi.pushView(picker, new GenericPickerDelegate(:activity), WatchUi.SLIDE_UP);
                return true;
            case :screens:
                WatchUi.pushView(new Rez.Menus.DataScreens(), new DataMenuDelegate(), WatchUi.SLIDE_UP);
                return true;
            case :course:
                WatchUi.pushView(new Rez.Menus.TrackMenu(), new TrackMenuDelegate(), WatchUi.SLIDE_UP);
                return true;       
            default:
                return false;
        }
    }
}

class GenericPickerDelegate extends WatchUi.PickerDelegate {
    hidden var mItem;

    function initialize(item) {
        PickerDelegate.initialize();
        mItem = item;
    }

    function onCancel() {
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
        switch(mItem) {
            case :orient:
                if(values[0] == 0) {
                    Transform.northHeading = false;
                    Transform.centerMap = false;
                } else {
                    Transform.northHeading = true;
                    Transform.centerMap = (values[0] == 2);
                }
                Application.getApp().setProperty("northHeading", Transform.northHeading);
                Application.getApp().setProperty("centerMap", Transform.centerMap);
                break;
            case :background:
                $.isDarkMode = values[0];
                Application.getApp().setProperty("darkMode", $.isDarkMode);
                if($.trackView != null) {
                    trackView.setDarkMode($.isDarkMode);        
                }
                if($.dataView != null) {
                    dataView.setDarkMode($.isDarkMode);
                }
                break;            
            case :autolap:
                Trace.setAutolapDistance(values[0]);
                Application.getApp().setProperty("autolapDistance",values[0]);
            case :bc_distance:
                Trace.breadCrumbDist = values[0];
                Application.getApp().setProperty("breadCrumbDist", Trace.breadCrumbDist);
                break;
             case :bc_number:
                Trace.setBreadCrumbNumber(values[0]);
                Application.getApp().setProperty("breadCrumbNumber", Trace.breadCrumbNumber);
                break;    
            case :activity:
                $.activityType = values[0];
                Application.getApp().setProperty("activityType", $.activityType);
                Data.setMaxHeartRate();
                break;
            case :track_update:
                $.trackViewPeriod = values[0];
    			Application.getApp().setProperty("trackViewPeriod", $.trackViewPeriod);
                break;
            default:
                break;
        }
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }
}

class DataMenuDelegate extends WatchUi.MenuInputDelegate {

    var dataMenuContext = null;
    function initialize() {
        MenuInputDelegate.initialize();
    }

    function onMenuItem(item) {
        var screen = 0;
        switch ( item ) {
            case :ds0:
                Data.setDataScreens(Data.dataScreensDefault);
                Application.getApp().setProperty("dataScreens",Data.getDataScreens());
                WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
                return true;
            case :ds1:
                screen=0;
                break;
            case :ds2:
                screen=1;
                break;
            case :ds3:
                screen=2;
                break;
            default:
                return false;
        }
        var defaultValue = Data.dataScreens[screen].size();
        var factory =  new GenericListItemFactory([0,1,2,3,4],["Off", "1", "2", "3", "4"],{:font => Graphics.FONT_MEDIUM});
        var picker = new GenericListItemPicker(WatchUi.loadResource(Rez.Strings.num_df), [factory], [defaultValue]);
        WatchUi.pushView(picker, new NumberDataFieldsPickerDelegate(screen), WatchUi.SLIDE_IMMEDIATE);
        return true;
    }

}


class NumberDataFieldsPickerDelegate extends WatchUi.PickerDelegate {
    hidden var mContext;

    function initialize(context) {
        PickerDelegate.initialize();
        mContext = context;
    }

    function onCancel() {
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
        //Application.getApp().setProperty(mPicker.getPropertyKey(),values[0]);
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
        var screen = mContext;
        var nDataFields = values[0];

        if(nDataFields==0) {
             Data.setDataScreen(screen,[]);
             Application.getApp().setProperty("dataScreens",Data.getDataScreens());
             WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
        } else {
            var factories = new [nDataFields];
            var defaults = new [nDataFields];
            for(var i=0; i<nDataFields; i+=1) {
                factories[i] = new GenericListItemFactory(
                    null, Data.dataFieldMenuLabels, {:font => $.device.equals("vivoactive") ? Graphics.FONT_XTINY: Graphics.FONT_SMALL});                
                defaults[i] = i < Data.dataScreens[screen].size()? Data.dataScreens[screen][i] : 0;
            }            
            WatchUi.pushView(
                new GenericListItemPicker(WatchUi.loadResource(Rez.Strings.fields), factories, defaults),
                new DataFieldsPickerDelegate(mContext), WatchUi.SLIDE_IMMEDIATE);
        }
    }
}

class DataFieldsPickerDelegate extends WatchUi.PickerDelegate {
    hidden var mContext;

    function initialize(context) {
        PickerDelegate.initialize();
        mContext = context;
    }

    function onCancel() {
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
        var screen = mContext;
        Data.setDataScreen(screen, values);
        Application.getApp().setProperty("dataScreens",Data.getDataScreens());
      
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }
}

class TrackMenuDelegate extends WatchUi.MenuInputDelegate {

    function initialize() {
        MenuInputDelegate.initialize();
    }

    function onMenuItem(item) {
        switch(item) {
            case :track_update:
                WatchUi.pushView(
                    new GenericListItemPicker(
                        WatchUi.loadResource(Rez.Strings.update), 
                        [new GenericListItemFactory([1,2,5,10,15,30,60],["1s", "2s", "5s", "10s", "15s", "30s", "60s"], null)],
                        [$.trackViewPeriod]), 
                    new GenericPickerDelegate(:track_update), WatchUi.SLIDE_IMMEDIATE);
                return true;
            case :track_del:
                if(track!=null) {
                    //var message = "Delete track?";
                    var message = WatchUi.loadResource(Rez.Strings.msg_delete_track);
                    
                    var dialog = new WatchUi.Confirmation(message);
                    WatchUi.pushView(
                        dialog,
                        new DeleteConfirmationDelegate(),
                        WatchUi.SLIDE_IMMEDIATE
                    );
                }
                return true;	
            case :track_info:
                if(track!=null) {
                    WatchUi.pushView(new TrackInfoView(),new TrackInfoDelegate(), WatchUi.SLIDE_IMMEDIATE);	
                }
                return true;
        }	
    }
}

class BreadcrumbsMenuDelegate extends WatchUi.MenuInputDelegate {
    var picker;
    var factory;
    var defaultValue;
    var text_label;

    var distanceValues = [0.0,50.0,100.0,200.0,500.0,1000.0,2000.0,5000.0,10000.0,20000.0];
    var distanceLabels = ["off","50m","100m","200m","500m","1km","2km","5km","10km","20km"];


    function initialize() {
        MenuInputDelegate.initialize();
    }

    function onMenuItem(item) {
        switch(item) {
            case :bc_set:
                Trace.putBreadcrumbLastPosition();
                return true;
            case :bc_clear:
                Trace.reset();
                return true;
            case :bc_number:
                defaultValue = Trace.breadCrumbNumber;
                factory = new GenericListItemFactory(
                    [1,2,5,10,20,50,100],
                    null,
                    null);
                text_label = WatchUi.loadResource(Rez.Strings.bc_number);
                picker = new GenericListItemPicker(text_label, [factory], [defaultValue]);
                WatchUi.pushView(picker, new GenericPickerDelegate(:bc_number), WatchUi.SLIDE_UP);
                return true;
            case :bc_distance:
                defaultValue = Trace.breadCrumbDist;
                factory = new GenericListItemFactory(
                    distanceValues,
                    distanceLabels,
                    null);
                text_label = WatchUi.loadResource(Rez.Strings.bc_distance);
				picker = new GenericListItemPicker(text_label, [factory], [defaultValue]);
                WatchUi.pushView(picker, new GenericPickerDelegate(:bc_distance), WatchUi.SLIDE_UP);
                return true;
        }	
    }
}

class DeleteConfirmationDelegate extends WatchUi.ConfirmationDelegate {
    
    function initialize() {
        ConfirmationDelegate.initialize();
    }
    
    function onResponse(response) {
        if (response == WatchUi.CONFIRM_NO) {
        } else {
            $.track=$.track.clean();
            Transform.setViewCenter(Trace.lat_last_pos,Trace.lon_last_pos);
            $.track=null;
            //Trace.reset();
        }
    }
}

class TrackInfoDelegate extends WatchUi.BehaviorDelegate {

    function initialize() {
        BehaviorDelegate.initialize();
    }   

    function onBack() {
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }
}
