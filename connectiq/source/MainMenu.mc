
using Toybox.WatchUi;
using Toybox.System;
using Toybox.ActivityRecording;
using Trace;


class MainMenuDelegate extends WatchUi.MenuInputDelegate {
    
    hidden var menu;
    
    function initialize(_menu) {
        MenuInputDelegate.initialize();
        menu = _menu;
    }

    function onMenuItem(item) {
        var defaultValue = null;
        var valueList = null;
        var labelList = null;
        var idList = null;
        var newMenu;

        switch(menu.getSelectedId()) {
            
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
                idList = null;
                labelList = WatchUi.loadResource(Rez.Strings.orient_opts);
                valueList = null;
                break;
            case :background:
                defaultValue = $.isDarkMode;
                idList = null;
                labelList = WatchUi.loadResource(Rez.Strings.color_opts);
                valueList = [true, false];
                break;
            case :autolap:
                defaultValue = Trace.autolapDistance;
                idList = null;
                labelList = ["off","100m","200m","400m","500m","1km","2km","5km"];
                valueList = [0.0,100.0,200.0,400.0,500.0,1000.0,2000.0,5000.0];
                break;
            case :breadcrumbs:
                defaultValue = null;
                idList = [:bc_set, :bc_clear, :bc_number, :bc_distance];
                labelList = WatchUi.loadResource(Rez.Strings.bc_labels);
                valueList = null; 
                break;
            case :activity:
                defaultValue = $.activityType;
                idList = null;
                labelList = WatchUi.loadResource(Rez.Strings.activities);
                valueList = [ActivityRecording.SPORT_GENERIC, ActivityRecording.SPORT_RUNNING, 
                                    ActivityRecording.SPORT_WALKING, ActivityRecording.SPORT_CYCLING]; 
                break;
            case :screens:
                defaultValue = null;
                idList = null;
                labelList = WatchUi.loadResource(Rez.Strings.ds_labels);
                valueList = null; 
                break;
            case :course:
                defaultValue = null;
                idList = [:track_update, :track_info, :track_del];
                labelList = WatchUi.loadResource(Rez.Strings.course_labels);
                valueList = null;
                break;
            default:
                return false;
        }
        newMenu = new ListMenu(menu.getSelectedId(), menu.getSelectedLabel(), idList, labelList, valueList, defaultValue);
        WatchUi.pushView(newMenu, new ListMenuDelegate (newMenu, new GenericMenuDelegate(newMenu)), WatchUi.SLIDE_UP);
        return true;
    }
}

class GenericMenuDelegate extends WatchUi.MenuInputDelegate {
    hidden var menu;
    
    function initialize(_menu) {
        MenuInputDelegate.initialize();
        menu = _menu;
    }

    function onMenuItem(item) {
        var value = menu.getSelectedValue();
        switch(menu.id) {
            case :orient:
                if(value == 0) {
                    Transform.northHeading = false;
                    Transform.centerMap = false;
                } else {
                    Transform.northHeading = true;
                    Transform.centerMap = (value == 2);
                }
                Application.getApp().setProperty("northHeading", Transform.northHeading);
                Application.getApp().setProperty("centerMap", Transform.centerMap);
                break;
            case :background:
                $.isDarkMode = value;
                Application.getApp().setProperty("darkMode", $.isDarkMode);
                if($.trackView != null) {
                    trackView.setDarkMode($.isDarkMode);        
                }
                if($.dataView != null) {
                    dataView.setDarkMode($.isDarkMode);
                }
                break;
            case :autolap:
                Trace.setAutolapDistance(value);
                Application.getApp().setProperty("autolapDistance",value);
                break;
            case :breadcrumbs:
                switch(menu.getSelectedId()) {
		            case :bc_set:
		                Trace.putBreadcrumbLastPosition();
		                break;
		            case :bc_clear:
		                Trace.reset();
		                break;
		            case :bc_number:
		                var defaultValue = Trace.breadCrumbNumber;
                        var newMenu = new ListMenu(:bc_number, menu.getSelectedId(), null,
                                null, [1,2,5,10,20,50,100], defaultValue);
                        WatchUi.pushView(newMenu, new ListMenuDelegate (newMenu, new GenericMenuDelegate(newMenu)), WatchUi.SLIDE_UP);
		                break;
		            case :bc_distance:
		                defaultValue = Trace.breadCrumbDist;
		                newMenu = new ListMenu(:bc_distance, menu.getSelectedId(), null,
                                ["off","50m","100m","200m","500m","1km","2km","5km","10km","20km"], 
                                [0.0,50.0,100.0,200.0,500.0,1000.0,2000.0,5000.0,10000.0,20000.0], defaultValue);
                        WatchUi.pushView(newMenu, new ListMenuDelegate (newMenu, new GenericMenuDelegate(newMenu)), WatchUi.SLIDE_UP);
                        break;
		        }   
                break;
            case :bc_distance:
                Trace.breadCrumbDist = value;
                Application.getApp().setProperty("breadCrumbDist", Trace.breadCrumbDist);
                break;
            case :bc_number:
                Trace.setBreadCrumbNumber(value);
                Application.getApp().setProperty("breadCrumbNumber", Trace.breadCrumbNumber);
                break;
            case :activity:
                $.activityType = value;
                Application.getApp().setProperty("activityType", $.activityType);
                Data.setMaxHeartRate();
                break;
            case :course:
                var newMenu;
                switch(menu.getSelectedId()) {
                    case :track_update:
                        newMenu = new ListMenu(:track_update, menu.getSelectedId(), null,
                                ["1s", "2s", "5s", "10s", "15s", "30s", "60s"], 
                                [1,2,5,10,15,30,60], $.trackViewPeriod);
                        WatchUi.pushView(newMenu, new ListMenuDelegate (newMenu, new GenericMenuDelegate(newMenu)), WatchUi.SLIDE_UP);
		                return true;
		            case :track_del:
		                if(track!=null) {
		                    var message = menu.getSelectedId();
		                    
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
            case :track_update:
                $.trackViewPeriod = value;
                Application.getApp().setProperty("trackViewPeriod", $.trackViewPeriod);
                break;
            case :screens:
                if(value == 0) {
	                Data.setDataScreens(Data.dataScreensDefault);
	                Application.getApp().setProperty("dataScreens", Data.getDataScreens());
	                WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
	                return true;
	            }
	            // value denotes screen
	            newMenu = new ListMenu(:DataScreens, "# " + WatchUi.loadResource(Rez.Strings.fields), 
	                    null, 
	                    ["Off", "1", "2", "3", "4"], 
	                    [0,1,2,3,4], Data.dataScreens[value - 1].size());
	            WatchUi.pushView(newMenu, new ListMenuDelegate (newMenu, new NumberDataFieldsDelegate (newMenu, value - 1)), WatchUi.SLIDE_UP);
	            return true;
            default:
                break;
        }
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
        return true;
    }
}

class NumberDataFieldsDelegate extends WatchUi.MenuInputDelegate {
    hidden var screen;
    hidden var menu;

    function initialize(_menu, _screen ) {
        MenuInputDelegate.initialize ();
        screen = _screen;
        menu = _menu;
    }

    function onMenuItem(item) {
        //Application.getApp().setProperty(mPicker.getPropertyKey(),values[0]);
        //WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
        var nDataFields = menu.getSelectedValue();

        if(nDataFields == 0) {
            Data.setDataScreen(screen,[]);
            Application.getApp().setProperty("dataScreens",Data.getDataScreens());
            WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
        } else {
            var defaultValue = 0 < Data.dataScreens[screen].size()? Data.dataScreens[screen][0] : 0;
            var view = new ListMenu(:DataFields, WatchUi.loadResource(Rez.Strings.field) + " 1/" + nDataFields, null, Data.dataFieldMenuLabels, null, defaultValue);
            WatchUi.pushView(view, new ListMenuDelegate (view, new DataFieldDelegate (view, screen, 0, nDataFields)), WatchUi.SLIDE_UP); 
        }
    }
}

class DataFieldDelegate extends WatchUi.MenuInputDelegate {
    hidden var screen;
    hidden var fieldIdx;
    hidden var nFields;
    hidden var menu;
    hidden var dataFields = [];

    function initialize(_menu, _screen, _fieldIdx, _nFields) {
        MenuInputDelegate.initialize ();
        screen = _screen;
        fieldIdx = _fieldIdx;
        nFields = _nFields;
        menu = _menu;
    }

    function onMenuItem(item) {
        dataFields.add(menu.getSelectedValue());
        fieldIdx++;
        if(fieldIdx < nFields) {
            //fieldIdx++;
            var defaultValue = fieldIdx < Data.dataScreens[screen].size()? Data.dataScreens[screen][fieldIdx] : 0;
            menu = new ListMenu(:DataFields, WatchUi.loadResource(Rez.Strings.field) + " " + fieldIdx +"/"  + nFields, null, Data.dataFieldMenuLabels, null, defaultValue);
            WatchUi.switchToView(menu, new ListMenuDelegate (menu, self), WatchUi.SLIDE_UP);
                        
        } else
        {
            Data.setDataScreen(screen, dataFields);
            Application.getApp().setProperty("dataScreens",Data.getDataScreens());
            WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
            WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
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
