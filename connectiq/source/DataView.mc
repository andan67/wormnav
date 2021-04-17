using Toybox.WatchUi;
using Toybox.Activity;
using Data;
using Trace;


class DataView extends GenericView {

    hidden var width;
    hidden var height;
    hidden var data = new [4];
    hidden var mDataFields;
    hidden var numberDataFields = 0;
    hidden var dfLines;
    hidden var dfCenters;
    hidden var font;
    hidden var fontNumber;
    
    // Set the label of the field here
    function initialize(dataFields) {
        GenericView.initialize();
        setDataFields(dataFields);
    }

    function setDataFields(dataFields) {
        mDataFields = new [dataFields.size()];
        // deep copy of data fields items (integers)
        for(var i=0; i< mDataFields.size(); i+=1) {
            mDataFields[i] = dataFields[i];
        }
        numberDataFields = Data.min(4,mDataFields.size());
        font = Graphics.FONT_SMALL;
        switch(numberDataFields) {
            case 1:
                font = Graphics.FONT_LARGE;
                fontNumber = Graphics.FONT_NUMBER_HOT;
                break;
            case 2:
                font = Graphics.FONT_LARGE;
                fontNumber = Graphics.FONT_NUMBER_HOT;
                break;
            case 3:
            case 4:
            default:
                if($.device.equals("vivoactive")) {
                    font = Graphics.FONT_XTINY;
                    fontNumber = Graphics.FONT_NUMBER_MEDIUM;
                } else
                {
                    font = Graphics.FONT_SMALL;
                    fontNumber = Graphics.FONT_NUMBER_MEDIUM;
                }
                break;
        }
    }

    function onLayout(dc) {
        width=dc.getWidth();
        height=dc.getHeight();

        if(numberDataFields==0) {
            return;
        }

        var h1 = dc.getFontHeight(font);
        var a1 = dc.getFontAscent(font);
        var d1 = dc.getFontDescent(font);
        var h2 = dc.getFontHeight(fontNumber);
        var a2 = dc.getFontAscent(fontNumber);
        var d2 = dc.getFontDescent(fontNumber);

        var h = height/Data.min(numberDataFields,3);
        var b = 0.5*(h-a1-a2-d1);
        var y1 = b + 0.5*h1;
        var y2 = b + h1 + 0.5*(a2-d2);
        var w2= 0.5*width;

        switch(numberDataFields) {
            case 1:
                //
                dfLines = [];
                dfCenters = [[w2, y1-d2, y2-d2]];
                break;
            case 2:
                dfLines = [[[0,h],[width,h]]];
                dfCenters = [[w2,y1,y2],[w2,h+y1,h+y2]];
                break;
            case 3:
                dfLines = [[[0,h],[width,h]],[[0,2*h],[width,2*h]]];
                dfCenters = [[w2,y1,y2],[w2,h+y1,h+y2],[w2,2*h+y1,2*h+y2]];
                break;
            case 4:
            default:
                dfLines = [[[0,h],[width,h]],[[0,2*h],[width,2*h]],[[w2,h],[w2,2*h]]];
                dfCenters = [[w2,y1,y2],[0.5*w2,h+y1,h+y2],[1.5*w2,h+y1,h+y2],[w2,2*h+y1,2*h+y2]];
                break;
        }
    }

    // Handle the update event
    function onUpdate(dc) {
        dc.setColor(backgroundColor, backgroundColor);
        dc.clear();

        // draw lines for 4 data fields
        dc.setColor(Graphics.COLOR_RED, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(1);

        for(var i=0; i < dfLines.size(); i +=1) {
            dc.drawLine(dfLines[i][0][0],dfLines[i][0][1],dfLines[i][1][0],dfLines[i][1][1]);
        }

        var dataLabelValue = null;
        for(var i=0; i< numberDataFields; i+= 1) {
            dataLabelValue = Data.getDataFieldLabelValue(mDataFields[i]);
            drawField(dc, dataLabelValue[0], dataLabelValue[1] , dfCenters[i][0], dfCenters[i][1], dfCenters[i][2]);
        }

        drawStartStop(dc);
    }

    function drawField(dc, label, value, x, y1, y2) {
        if(value == null) {
            value="--";
        }
        if( label == null ) {
            label = "";

        }
        dc.setColor(foregroundColor, Graphics.COLOR_TRANSPARENT);
        dc.drawText(x, y1, font, label, Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
        dc.drawText(x, y2, fontNumber, value, Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
        return;
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
        dc.drawText(dc.getWidth()/2, dc.getFontHeight(Graphics.FONT_LARGE), Graphics.FONT_LARGE, "Lap " + Trace.lapCounter, Graphics.TEXT_JUSTIFY_CENTER);
        dc.drawText(dc.getWidth()/2, dc.getHeight()/2 - dc.getFontHeight(Graphics.FONT_NUMBER_MEDIUM)/2, Graphics.FONT_NUMBER_HOT, Data.msToTimeWithDecimals(Trace.lapTime.toLong()), Graphics.TEXT_JUSTIFY_CENTER);
    }
}