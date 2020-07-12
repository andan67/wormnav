using Toybox.Application;
using Toybox.Graphics;
using Toybox.WatchUi;

class GenericListItemPicker extends WatchUi.Picker {
 	var mContext;
 	
    function initialize(title, pattern, defaultValues, context) {
    	var defaults = null;
    	mContext = context;
    	if(defaultValues!=null && pattern.size()==defaultValues.size()) {
    		defaults = new [pattern.size()];
    		for(var i=0; i<defaults.size(); i+=1) {
    			if(defaultValues[i]!=null && pattern[i] has :getIndex) {
    				defaults[i] = pattern[i].getIndex(defaultValues[i]);
    			} else {
    				defaults[i] = null;
    			}
    		}
    	}
        mTitle = new WatchUi.Text({:text=>title, :locX =>WatchUi.LAYOUT_HALIGN_CENTER, :locY=>WatchUi.LAYOUT_VALIGN_BOTTOM, :color=>Graphics.COLOR_WHITE});

        Picker.initialize({:title=>mTitle, :pattern=>pattern, :defaults=>defaults});
    }

    function onUpdate(dc) {
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_BLACK);
        dc.clear();
        Picker.onUpdate(dc);
    }
    
    function getContext() {
    	return mContext;
    }
    
    function setContext(context) {
    	mContext=context;
    }
    
    
}
