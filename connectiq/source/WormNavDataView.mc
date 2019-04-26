using Toybox.WatchUi;
using Toybox.Activity;
using Utils;
using Trace;

class WormNavDataView extends  WatchUi.View {

    hidden var width;
    hidden var height;
    hidden var data = new [4];
    const avgChar = StringUtil.utf8ArrayToString([0xC3,0x98]);

    // Set the label of the field here
    function initialize() {
        View.initialize();
        //data = new [4];
        // Set up a 1Hz update timer because we aren't registering
        // for any data callbacks that can kick our display update.
    }

    function onShow() {
    }

    function onHide() {
    }

    function onLayout(dc) {
        width=dc.getWidth();
        height=dc.getHeight();
    }

    // Handle the update event
    function onUpdate(dc) {
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_WHITE);
        dc.clear();

        // draw lines for 4 data fields
        dc.setColor(Graphics.COLOR_RED, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(1);

        var y = height/3;
        dc.drawLine(0,y,width,y);
        dc.drawLine(0,2*y,width,2*y);
        dc.drawLine(width/2,y,width/2,2*y);

        if(session.isRecording() && Activity.getActivityInfo()!=null) {
            if( Activity.getActivityInfo().elapsedDistance!= null) {
               data[0] = Activity.getActivityInfo().elapsedDistance/1000;
            }
            if(Activity.getActivityInfo().elapsedTime!=null) {
               data[1] = Activity.getActivityInfo().timerTime;
            }
            if(Activity.getActivityInfo().averageSpeed!=null) {
               data[2] = Activity.getActivityInfo().averageSpeed;
               //data[2]=Utils.speedToPace(data[2]);
               //data[2] = Utils.convertSpeedToPace(data[2]);
            }
            if(Activity.getActivityInfo().currentHeartRate!=null) {
               data[3] = Activity.getActivityInfo().currentHeartRate;
            }
        }

        y=0;
        var x= width/2;
        drawField(dc, "Timer", data[1]!=null? Utils.msToTime(data[1]) : null,x,y);
        y= height/3;
        x = width/4;
        drawField(dc, "Distance", data[0]!=null? data[0].format("%.2f") : null, x, y);
        x= 3*width/4;
        StringUtil.utf8ArrayToString([0xC2,0xB0]);
        //drawField(dc, avgChar + " Pace",Utils.printPace(data[2]) , x, y);
        drawField(dc, avgChar + " Pace", data[2]!=null? Utils.convertSpeedToPace(data[2]) : null , x, y);
        y=2*height/3;
        x=width/2;
        drawField(dc, "Heart Rate", data[3]!=null? data[3] : null, x, y);
    }

    function drawField(dc, label, value, x, y) {
        //var offset = dc.getFontHeight( Graphics.FONT_MEDIUM );
        var offset = 0.5*dc.getFontAscent( Graphics.FONT_MEDIUM );

        if(value==null) {
            value="--";
        }
        if( label == null ) {
            dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_TRANSPARENT);
            dc.drawText(x, y+offset, Graphics.FONT_NUMBER_MEDIUM, value, Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
        } else {
            dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_TRANSPARENT);
            dc.drawText(x, y+offset , Graphics.FONT_MEDIUM, label, Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
            dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_TRANSPARENT);
            dc.drawText(x, y + offset + dc.getFontDescent( Graphics.FONT_MEDIUM )+ 0.5*dc.getFontHeight( Graphics.FONT_NUMBER_MEDIUM ), Graphics.FONT_NUMBER_MEDIUM, value, Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
        }
        return;
    }
}


class WormNavLapView extends WatchUi.View {

    // Set the label of the field here
    function initialize() {
        View.initialize();
    }

    function onUpdate(dc) {
        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        dc.clear();
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_TRANSPARENT);
        //System.println("WormNavLapView");
        dc.drawText(dc.getWidth()/2, dc.getFontHeight(Graphics.FONT_LARGE), Graphics.FONT_LARGE, "Lap " + Trace.lapCounter, Graphics.TEXT_JUSTIFY_CENTER);
        dc.drawText(dc.getWidth()/2, dc.getHeight()/2 - dc.getFontHeight(Graphics.FONT_NUMBER_MEDIUM)/2, Graphics.FONT_NUMBER_HOT, Utils.msToTimeWithDecimals(Trace.lapTime.toLong()), Graphics.TEXT_JUSTIFY_CENTER);
    }
}