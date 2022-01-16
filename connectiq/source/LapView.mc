using Toybox.WatchUi;
using Data;

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