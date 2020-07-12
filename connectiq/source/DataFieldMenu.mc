using Toybox.WatchUi;
using Toybox.System;
using Trace;

class DataMenuDelegate extends WatchUi.MenuInputDelegate {

	var dataMenuContext = [];
    function initialize() {
        MenuInputDelegate.initialize();
    }

    function onMenuItem(item) {
        var screen = 0;
        switch ( item ) {
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
        dataMenuContext.add(screen);
        var defaultValue = Data.dataScreens[screen].size();
        var factory =  new GenericListItemFactory(
     			[0,1,2,3,4],["Off", "1", "2", "3", "4"],{:font => Graphics.FONT_MEDIUM});
     	var picker = new GenericListItemPicker("# Data Fields", [factory], [defaultValue], dataMenuContext);	
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
    	//Application.getApp().setProperty(mPicker.getPropertyKey(),values[0]);
    	WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    	var screen = mContext[0];
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
	    			Data.dataFieldValues, Data.dataFieldMenuLabels, {:font => Graphics.FONT_SMALL});
	    		defaults[i] = i < Data.dataScreens[screen].size()? Data.dataScreens[screen][i] : 0;	
	    	}
	    	var picker = new GenericListItemPicker("# Data Fields", factories, defaults, mContext);	
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
        //WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
    	System.println("on Accept()");
    	var screen = mContext[0];
    	System.println("on Accept() screen: " + screen);
    	Data.setDataScreen(screen, values);
    	System.println("on Accept() values: " + values);
    	Application.getApp().setProperty("dataScreens",Data.getDataScreens());
    	System.println("on Accept() setProperty ");
    	//WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    	WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }
}