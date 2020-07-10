using Toybox.WatchUi;
using Toybox.System;
using Trace;

class WormNavMainMenuDelegate extends WatchUi.MenuInputDelegate {

    function initialize() {
        MenuInputDelegate.initialize();
    }

    function onMenuItem(item) {
    	var picker;
    	switch(item) {
    		case :delete:
    			if(track!=null) {
                	track=track.clean();
                	Transform.setViewCenter(Trace.lat_last_pos,Trace.lon_last_pos);
                	track=null;
                	Trace.reset();
            	}
            	return true;
        	case :north:
        		Transform.northHeading = !Transform.northHeading;
            	Application.getApp().setProperty("northHeading", Transform.northHeading);
            	return true;
     		case :center:
     			Transform.centerMap = !Transform.centerMap;
            	Application.getApp().setProperty("centerMap", Transform.centerMap);
            	return true;
            case :autolap:
 				picker = new GenericListItemPicker(
 					"Auto lap",
 					[0,100,200,400,500,1000,2000,5000],
 					["off","100m","200m","400m","500m","1km","2km","5km"],
 					"autolapDistance");
 				WatchUi.pushView(picker, new AutoLapPickerDelegate(picker), WatchUi.SLIDE_IMMEDIATE);
            	return true;
            case :breadCrumbs:
            	picker = new GenericListItemPicker(
 					"Bread crumbs",
 					[0,100,200,400,500,1000,2000,5000],
 					["off","100m","200m","400m","500m","1km","2km","5km"],
 					"breadCrumbDist");
	            WatchUi.pushView(picker, new WormNavBreadCrumbsPickerDelegate(picker), WatchUi.SLIDE_UP);
	            return true;
	        case :screens:
	            WatchUi.pushView(new Rez.Menus.DataScreens(), new DataMenuDelegate(), WatchUi.SLIDE_UP);
	            return true;	
	    	default:
	    		return false;
        }
    }
}