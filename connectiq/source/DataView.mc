using Toybox.WatchUi;
using Toybox.Activity;
using Data;
using Track;
using Layout;


class DataView extends GenericView {

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

    function onLayout(dc) {
        if(numberDataFields == 0) {
            return;
        }

        width = dc.getWidth();
        height = dc.getHeight();

        if(numberDataFields == 1) {
            font = Layout.DATA_FONT_1;
            fontNumber = Layout.DATA_FONT_NUMBER_1;
            offset = -dc.getFontDescent(fontNumber);
            h = height;
        } else if(numberDataFields == 2) {
            font = Layout.DATA_FONT_2;
            fontNumber = Layout.DATA_FONT_NUMBER_2;
            offset = Layout.DATA_OFFSET_FACTOR_2 * height;
            h = Layout.DATA_HEIGHT_FACTOR_2 * height;
        } else {
            font = Layout.DATA_FONT_3;
            fontNumber = Layout.DATA_FONT_NUMBER_3;
            offset = Layout.DATA_OFFSET_FACTOR_3 * height;
            h = Layout.DATA_HEIGHT_FACTOR_3 * height;
        }

         // top position of data label text in pixels from top of data field
        yl = 0.5 * (h - dc.getFontHeight(font) -  dc.getFontHeight(fontNumber) + dc.getFontDescent(fontNumber));
        // top position of data value text in pixels from top of data field
        yv = yl + dc.getFontAscent(font);
        w2 = 0.5 * width;
    }

    // Handle the update event
    function onUpdate(dc) {
        dc.setColor(backgroundColor, backgroundColor);
        dc.clear();

        // draw lines for 4 data fields
        dc.setColor(Graphics.COLOR_RED, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(1);

        for(var i = 1; i < numberDataFields; i += 1) {
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