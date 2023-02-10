using Toybox.WatchUi;
using Toybox.Activity;
using Data;
using Track;


class DataViewCommon extends GenericView {

    hidden var width;
    hidden var height;
    hidden var data = new [4];
    hidden var dataFields;
    hidden var numberDataFields = 0;
    hidden var font;
    hidden var fontNumber;

    hidden var offset;
    hidden var h;
    hidden var yl;
    hidden var yv;
    hidden var w2;


    // Set the label of the field here
    function initialize(dataFields) {
        GenericView.initialize();
        setDataFields(dataFields);
    }

    function setDataFields(df) {
        dataFields = new [df.size()];
        // deep copy of data fields items (integers)
        for(var i = 0; i < dataFields.size(); i += 1) {
            dataFields[i] = df[i];
        }
        numberDataFields = Data.min(4,dataFields.size());
    }

    // Handle the update event
    function onUpdate(dc) {
        dc.setColor(backgroundColor, backgroundColor);
        dc.clear();

        // draw lines for 4 data fields
        dc.setColor(Graphics.COLOR_RED, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(1);

        for(var i = 1; i < numberDataFields; i += 1) {
            // Optimize
            var j = Data.min(i, 2);
            dc.drawLine(0, offset + j * h, width, offset + j * h);
            if( i == 3) {
                dc.drawLine(w2, offset + h, w2, offset + 2 * h);
            }
        }

        // drwa fileds
        dc.setColor(foregroundColor, Graphics.COLOR_TRANSPARENT);
        for(var i = 0; i < numberDataFields; i += 1) {
             var dataLabelValue = Data.getDataFieldLabelValue(dataFields[i]);
             var xc = 0.0;
             var ylc = offset;
             var yvc = offset;
             if( numberDataFields < 4 || i == 0 || i == 3) {
                var j = Data.min(i, 2);
                xc = w2;
                ylc += yl + j * h;
                yvc += yv + j * h;
             } else {
                ylc += yl + h;
                yvc += yv + h;
                xc = (i - 0.5) * w2;
             }
             dc.drawText(xc, ylc, font, dataLabelValue[0], Graphics.TEXT_JUSTIFY_CENTER);
             dc.drawText(xc, yvc, fontNumber, dataLabelValue[1], Graphics.TEXT_JUSTIFY_CENTER);
        }

        drawStartStop(dc);
    }
}