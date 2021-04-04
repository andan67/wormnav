using Toybox.WatchUi;
using Transform;
using Trace;

class TrackInfoView extends WatchUi.View {
    
    var dy;
    
    function initialize() {
        View.initialize();
    }
     
     function onLayout(dc) {
        dy = 1.2*dc.getFontAscent(Graphics.FONT_SMALL);
    }
     // Update the view
    function onUpdate(dc) {
        var text_label;
        // Call the parent onUpdate function to redraw the layout
        View.onUpdate(dc);
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_BLACK);
        dc.clear();
 
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_TRANSPARENT );
        
		text_label = WatchUi.loadResource(Rez.Strings.info_lenght);
		dc.drawText(dc.getWidth() / 2, dc.getHeight() / 2 - 4*dy, Graphics.FONT_TINY, text_label, Graphics.TEXT_JUSTIFY_CENTER);
		dc.drawText(dc.getWidth() / 2, dc.getHeight() / 2 - 3*dy, Graphics.FONT_TINY, (0.001*$.track.length).format("%.2f"), Graphics.TEXT_JUSTIFY_CENTER);

		text_label = WatchUi.loadResource(Rez.Strings.info_name);
		dc.drawText(dc.getWidth() / 2, dc.getHeight() / 2 - 1*dy, Graphics.FONT_TINY, text_label, Graphics.TEXT_JUSTIFY_CENTER);
		dc.drawText(dc.getWidth() / 2, dc.getHeight() / 2 - 0*dy, Graphics.FONT_XTINY, $.track.name, Graphics.TEXT_JUSTIFY_CENTER);
		
		text_label = WatchUi.loadResource(Rez.Strings.info_points);
		dc.drawText(dc.getWidth() / 2, dc.getHeight() / 2 +2*dy, Graphics.FONT_TINY, text_label, Graphics.TEXT_JUSTIFY_CENTER);
		dc.drawText(dc.getWidth() / 2, dc.getHeight() / 2 + 3*dy, Graphics.FONT_TINY, $.track.nPoints, Graphics.TEXT_JUSTIFY_CENTER);
        
    }    
}