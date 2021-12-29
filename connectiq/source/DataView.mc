using Toybox.WatchUi;
using Toybox.Activity;
using Data;
using Track;


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
        font = Graphics.FONT_SMALL;
        switch(numberDataFields) {
            case 1:
                font = Graphics.FONT_LARGE;
                fontNumber = Graphics.FONT_NUMBER_HOT;
                break;
            case 2:
                if($.device.equals("vivoactive")) {
                    font = Graphics.FONT_MEDIUM;
                    fontNumber = Graphics.FONT_NUMBER_MEDIUM;
                } else {
                    font = Graphics.FONT_LARGE;
                    fontNumber = Graphics.FONT_NUMBER_HOT;
                }
                break;
            case 3:
            case 4:
            default:
                if($.device.equals("vivoactive")) {
                    font = Graphics.FONT_SMALL;
                    fontNumber = Graphics.FONT_NUMBER_MEDIUM;
                } else
                {
                    font = Graphics.FONT_MEDIUM;
                    fontNumber = Graphics.FONT_NUMBER_MEDIUM;
                }
                break;
        }
    }

    function onLayout(dc) {
        width = dc.getWidth();
        height = dc.getHeight();

        if(numberDataFields == 0) {
            return;
        }

        offset = numberDataFields == 1 ? -dc.getFontDescent(fontNumber) : 0.0;
        h = height / Data.min(numberDataFields, 3);
        if($.device.equals("vivoactive") && numberDataFields > 1) {
            // narrow vertical size of data fields for vivoactive devices with round screen
            if(numberDataFields > 2) {
                offset = 0.2 * h;
                h = (height - 2 * offset) / 3;
            } else {
                offset = 0.3 * h;
                h = (height - 2 * offset) / 2;
            }
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


class LapView extends WatchUi.View {

    // Set the label of the field here
    function initialize() {
        View.initialize();
    }

    function onUpdate(dc) {
        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        dc.clear();
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_TRANSPARENT);
        dc.drawText(dc.getWidth()/2, dc.getFontHeight(Graphics.FONT_LARGE), Graphics.FONT_LARGE, "Lap " + Track.lapCounter, Graphics.TEXT_JUSTIFY_CENTER);
        dc.drawText(dc.getWidth()/2, dc.getHeight()/2 - dc.getFontHeight(Graphics.FONT_NUMBER_MEDIUM)/2, Graphics.FONT_NUMBER_HOT, Data.msToTime(Track.lapTime.toLong(), true), Graphics.TEXT_JUSTIFY_CENTER);
    }
}