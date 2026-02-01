using Toybox.WatchUi;
using Data;
using Layout;

class LapView extends WatchUi.View {

    var label = "";
    // Set the label of the field here
    function initialize() {
        View.initialize();
        label = WatchUi.loadResource(Rez.Strings.lap) + " ";
    }

    function onUpdate(dc) {
        dc.setColor(Graphics.COLOR_DK_GRAY, Graphics.COLOR_BLACK);
        dc.clear();
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_TRANSPARENT);
        dc.drawText(dc.getWidth()/2, dc.getFontHeight(Layout.LAP_FONT), Layout.LAP_FONT, label + Track.lapCounter, Graphics.TEXT_JUSTIFY_CENTER);
        dc.drawText(dc.getWidth()/2, dc.getHeight()/2 - dc.getFontHeight(Layout.LAP_FONT_NUMBER)/2, Layout.LAP_FONT_NUMBER, Data.msToTime(Track.lapTime.toLong(), true), Graphics.TEXT_JUSTIFY_CENTER);
    }
}