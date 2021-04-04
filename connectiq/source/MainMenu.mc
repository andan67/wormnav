
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
        
    	var distanceValues = [0.0,100.0,200.0,400.0,500.0,1000.0,2000.0,5000.0];
    	var distanceLabels = ["off","100m","200m","400m","500m","1km","2km","5km"];
    	
    	var text_label;
        
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
            case :north:
                defaultValue = Transform.northHeading;
                 factory =  new GenericListItemFactory(
                     [true,false],
                     WatchUi.loadResource(Rez.Strings.north_opts),
                     {:font => Graphics.FONT_MEDIUM});
				text_label = WatchUi.loadResource(Rez.Strings.main_menu_label_orient);
				picker = new GenericListItemPicker(text_label, [factory], [defaultValue], null);
                WatchUi.pushView(picker, new GenericPickerDelegate(:north), WatchUi.SLIDE_IMMEDIATE);
                return true;
             case :center:
                defaultValue = Transform.centerMap;
                factory =  new GenericListItemFactory(
                    [true,false],
                    WatchUi.loadResource(Rez.Strings.onoff_opts),
                    {:font => Graphics.FONT_LARGE});
                text_label = WatchUi.loadResource(Rez.Strings.main_menu_label_center);
				picker = new GenericListItemPicker(text_label, [factory], [defaultValue], null);
                WatchUi.pushView(picker, new GenericPickerDelegate(:center), WatchUi.SLIDE_IMMEDIATE);
                return true;
            case :darkMode:
                defaultValue = $.isDarkMode;
                factory =  new GenericListItemFactory(
                    [true,false],
                     WatchUi.loadResource(Rez.Strings.onoff_opts),
                    {:font => Graphics.FONT_LARGE});
                picker = new GenericListItemPicker("Dark mode", [factory], [defaultValue], null);
                WatchUi.pushView(picker, new GenericPickerDelegate(:darkMode), WatchUi.SLIDE_IMMEDIATE);
                return true;    
            case :autolap:
                defaultValue = Trace.autolapDistance;
                factory = new GenericListItemFactory(
                    distanceValues,
                    distanceLabels,
                    null);
                text_label = WatchUi.loadResource(Rez.Strings.main_menu_label_lap);
				picker = new GenericListItemPicker(text_label, [factory], [defaultValue], null);
                WatchUi.pushView(picker, new GenericPickerDelegate(:autolap), WatchUi.SLIDE_IMMEDIATE);
                return true;
            case :breadCrumbs:
                defaultValue = Trace.breadCrumbDist;
                factory = new GenericListItemFactory(
                    distanceValues,
                    distanceLabels,
                    null);
                text_label = WatchUi.loadResource(Rez.Strings.main_menu_label_bc);
				picker = new GenericListItemPicker(text_label, [factory], [defaultValue], null);
                WatchUi.pushView(picker, new GenericPickerDelegate(:breadCrumbs), WatchUi.SLIDE_UP);
                return true;
            case :activityType:
                defaultValue = $.activityType;
                factory = new GenericListItemFactory(
                    [ActivityRecording.SPORT_GENERIC, ActivityRecording.SPORT_RUNNING, ActivityRecording.SPORT_WALKING, ActivityRecording.SPORT_CYCLING],
                    WatchUi.loadResource(Rez.Strings.Activities),
                    {:font => Graphics.FONT_SMALL});
                text_label = WatchUi.loadResource(Rez.Strings.main_menu_label_activity_type);
				picker = new GenericListItemPicker(text_label, [factory], [defaultValue], null);
				WatchUi.pushView(picker, new GenericPickerDelegate(:activityType), WatchUi.SLIDE_UP);
                return true;
            case :screens:
                WatchUi.pushView(new Rez.Menus.DataScreens(), new DataMenuDelegate(), WatchUi.SLIDE_UP);
                return true;
            case :refreshPeriods:
                WatchUi.pushView(new Rez.Menus.RefreshPeriods(), new PeriodsMenuDelegate(), WatchUi.SLIDE_UP);
                return true;
            case :track:
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
            case :north:
                Transform.northHeading = values[0];
                Application.getApp().setProperty("northHeading", Transform.northHeading);
                break;
            case :center:
                Transform.centerMap = values[0];
                Application.getApp().setProperty("centerMap", Transform.centerMap);
                break;
            case :darkMode:
                $.isDarkMode = values[0];
                Application.getApp().setProperty("darkMode", $.isDarkMode);
                if($.trackView != null) {
                    trackView.setDarkMode($.isDarkMode);        
                }
                if($.dataView != null) {
                    dataView.setDarkMode($.isDarkMode);
                }
                break;            
            case :autoLap:
                Trace.setAutolapDistance(values[0]);
                Application.getApp().setProperty("autolapDistance",values[0]);
            case :breadCrumbs:
                Trace.breadCrumbDist = values[0];
                Application.getApp().setProperty("breadCrumbDist", Trace.breadCrumbDist);
                break;
            case :activityType:
                $.activityType = values[0];
                Application.getApp().setProperty("activityType", $.activityType);
                Data.setMaxHeartRate();
                break;
            case :track:
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
        dataMenuContext = screen;
        var defaultValue = Data.dataScreens[screen].size();
        var factory =  new GenericListItemFactory([0,1,2,3,4],["Off", "1", "2", "3", "4"],{:font => Graphics.FONT_MEDIUM});
        var picker = new GenericListItemPicker(WatchUi.loadResource(Rez.Strings.num_data_fields), [factory], [defaultValue], dataMenuContext);
        WatchUi.pushView(picker, new NumberDataFieldsPickerDelegate(dataMenuContext), WatchUi.SLIDE_IMMEDIATE);
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
        var text_label;
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
            text_label = WatchUi.loadResource(Rez.Strings.main_menu_label_screens);
			var picker = new GenericListItemPicker(text_label, factories, defaults, mContext);
            WatchUi.pushView(picker, new DataFieldsPickerDelegate(mContext), WatchUi.SLIDE_IMMEDIATE);
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


class PeriodsMenuDelegate extends WatchUi.MenuInputDelegate {

    function initialize() {
        MenuInputDelegate.initialize();
    }

    function onMenuItem(item) {
        var factory;
        var defaultValue;
        var picker;
        var key;
        var text_label;
        
        var periodValues = [1,2,5,10,15,30,60];
        var periodLabels = ["1s", "2s", "5s", "10s", "15s", "30s", "60s"];
        
        switch ( item ) {
            case :upt:
            	defaultValue = $.trackViewPeriod;
            	key = "trackViewPeriod";
            	factory =  new GenericListItemFactory(periodValues, periodLabels,null);
                text_label = WatchUi.loadResource(Rez.Strings.Track_view);
			    picker = new GenericListItemPicker(text_label, [factory], [defaultValue], key);
			
                break;
            case :upd:
            	defaultValue = $.dataViewPeriod;
            	key = "dataViewPeriod";
            	factory =  new GenericListItemFactory(periodValues, periodLabels,null);
            	text_label = WatchUi.loadResource(Rez.Strings.Data_view);
			    picker = new GenericListItemPicker(text_label, [factory], [defaultValue], key);
            	
                break;
            case :upl:
            	defaultValue = $.lapViewPeriod;
            	key = "lapViewPeriod";
            	factory =  new GenericListItemFactory([5,10,15,20],["5s", "10s", "15s", "20s"],null);
            	text_label = WatchUi.loadResource(Rez.Strings.Auto_lap_view);
			    picker = new GenericListItemPicker(text_label, [factory], [defaultValue], key);
			    
                break;
            default:
                return false;
        }
        WatchUi.pushView(picker, new PeriodPickerDelegate(key), WatchUi.SLIDE_IMMEDIATE);
        return true;
    }
}

class PeriodPickerDelegate extends WatchUi.PickerDelegate {
    hidden var mKey;

    function initialize(key) {
        PickerDelegate.initialize();
        mKey = key;
    }

    function onCancel() {
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
    	switch ( mKey ) {
    		case "trackViewPeriod":
    			$.trackViewPeriod = values[0];
    			Application.getApp().setProperty("trackViewPeriod", $.trackViewPeriod);
    			break;
    		case "dataViewPeriod":
    			$.dataViewPeriod = values[0];
    			Application.getApp().setProperty("dataViewPeriod", $.dataViewPeriod);
    			break;
    		case "lapViewPeriod":
    			$.lapViewPeriod = values[0];
    			Application.getApp().setProperty("lapViewPeriod", $.lapViewPeriod);
    			break;
  		}
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }
}

class TrackMenuDelegate extends WatchUi.MenuInputDelegate {

    function initialize() {
        MenuInputDelegate.initialize();
    }

    function onMenuItem(item) {
        switch(item) {
            case :tdel:
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
            case :tinfo:
                if(track!=null) {
                    WatchUi.pushView(new TrackInfoView(),new TrackInfoDelegate(), WatchUi.SLIDE_IMMEDIATE);	
                }
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
            Trace.reset();
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
