using Toybox.Application;
using Toybox.Graphics;
using Toybox.WatchUi;

class GenericListItemPicker extends WatchUi.Picker {
    hidden var mItem;
    hidden var mFactory;
    hidden var mPropKey;

    function initialize(title, itemList, itemTextList, propKey) {
        mFactory = new GenericListItemFactory(itemList, itemTextList , null);
        mPropKey = propKey;
        var titleText = title;

        var itemValue = Application.getApp().getProperty(propKey);
        var defaults = null;

        if(itemValue != null) {
            defaults = [mFactory.getIndex(itemValue)];
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

class GenericListItemPickerDelegate extends WatchUi.PickerDelegate {
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
