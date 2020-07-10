using Toybox.Application;
using Toybox.Graphics;
using Toybox.WatchUi;

class DistancePicker extends WatchUi.Picker {
    hidden var mFactory;
    hidden var mPropKey;

    function initialize(title, distances, propKey) {
        mFactory = new DistanceFactory(distances,null);
        mPropKey = propKey;
        var titleText = title;

        var distance = Application.getApp().getProperty(propKey);
        var defaults = null;

        if(distance != null) {
            defaults = [mFactory.getIndex(distance)];
        }

        mTitle = new WatchUi.Text({:text=>titleText, :locX =>WatchUi.LAYOUT_HALIGN_CENTER, :locY=>WatchUi.LAYOUT_VALIGN_BOTTOM, :color=>Graphics.COLOR_WHITE});

        Picker.initialize({:title=>mTitle, :pattern=>[mFactory], :defaults=>defaults});
    }

    function onUpdate(dc) {
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_BLACK);
        dc.clear();
        Picker.onUpdate(dc);
    }

    function isDone(value) {
        return mFactory.isDone(value);
    }
    
    function getPropertyKey() {
    	return mPropKey;
    }
}

class DistancePickerDelegate extends WatchUi.PickerDelegate {
    hidden var mPicker;

    function initialize(picker) {
        PickerDelegate.initialize();
        mPicker = picker;
    }

    function onCancel() {
        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

    function onAccept(values) {
    	Application.getApp().setProperty(mPicker.getPropertyKey(),values[0]);
    	WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
    }

}
