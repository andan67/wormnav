using Toybox.Graphics;
using Toybox.WatchUi;

class DistanceFactory extends WatchUi.PickerFactory {
    hidden var mDistances;
    hidden var mFormatString;
    hidden var mFont;

    function initialize(distances, options) {
        PickerFactory.initialize();
        
        mDistances = distances;

        if(options != null) {
            mFormatString = options.get(:format);
            mFont = options.get(:font);
        }

        if(mFont == null) {
            mFont = Graphics.FONT_LARGE;
        }

        if(mFormatString == null) {
            mFormatString = "%d";
        }
    }

    function getDrawable(index, selected) {
        var valueText;
        if(getValue(index)==0) {
        	valueText = "off";
        } else if (getValue(index) < 1000) {
        	valueText = getValue(index).format(mFormatString)+"m";
        } else {
        	valueText = (getValue(index)/1000).format(mFormatString)+"km";
        }
        if(selected) {
        	//valueText = "<" + valueText + ">";
        }
        System.println("DistancePicker: " + index + "|" +  getValue(index) + "|" + valueText);
        return new WatchUi.Text( { :text=>valueText, :color=>Graphics.COLOR_WHITE, :font=> mFont, :locX =>WatchUi.LAYOUT_HALIGN_CENTER, :locY=>WatchUi.LAYOUT_VALIGN_CENTER } );
    }

    function getValue(index) {
        return mDistances[index];
    }
    
    function getIndex(value) {
        for(var i=0; i < mDistances.size(); i +=1) {
        	if(mDistances[i]==value) {
        		return i;
        	}
        }
        return 0;
    }
    

    function getSize() {
        return mDistances.size();
    }

}
