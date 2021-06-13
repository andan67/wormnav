using Toybox.WatchUi;
using Transform;
using Trace;

class TrackView extends GenericView {

    var cursorSizePixel;
    var posCursor;
    var isNewTrack=false;
    var activity_values;
    var fontsize = Graphics.FONT_MEDIUM;
    var topPadding = 0.0;
    var bottomPadding = 0.0;

    function drawScale(dc) {
        dc.setColor(foregroundColor, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(2);

        dc.drawLine(Transform.scale_x1,Transform.scale_y1 - bottomPadding,
                    Transform.scale_x1,Transform.scale_y2 - bottomPadding);
        dc.drawLine(Transform.scale_x1,Transform.scale_y2 - bottomPadding,
                    Transform.scale_x2,Transform.scale_y2 - bottomPadding);
        dc.drawLine(Transform.scale_x2,Transform.scale_y2 - bottomPadding,
                    Transform.scale_x2,Transform.scale_y1 - bottomPadding);
        dc.drawText(Transform.pixelWidth2, Transform.scale_y2-dc.getFontHeight(fontsize) - bottomPadding,
            fontsize , Transform.formatScale(Transform.refScale), Graphics.TEXT_JUSTIFY_CENTER);

    }

    function drawActivityInfo(dc) {
        dc.setColor(foregroundColor, Graphics.COLOR_TRANSPARENT);
        var y = 0.5*dc.getFontAscent(fontsize);
        for(var i = 0; i < Data.getField(3, 0); i++) {
            // index of data field
            var j = Data.getField(3, i+1);
            dc.drawText(Transform.pixelWidth2, topPadding + (1+2*i)*y, fontsize,
                //Data.dataFieldSLabels[j] +": "+
                Data.getDataFieldLabelValue(j)[1], Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
        }

    }

    function drawTrack(dc) {

        var scaleFactor = Transform.scaleFactor;
        var x_d = Transform.x_d;
        var y_d = Transform.y_d;
        var xs_center = Transform.xs_center;
        var ys_center = Transform.ys_center;
        var cos_heading_smooth = 1.0;
        var sin_heading_smooth = 0.0;
        if( !Transform.northHeading && !Transform.centerMap && !Transform.isTrackCentered) {
            cos_heading_smooth = Transform.cos_heading_smooth;
            sin_heading_smooth = Transform.sin_heading_smooth;
        }

        var xy_pos1;
        var xy_pos2;
        var x1 = 0.0;
        var y1 = 0.0;
        var x2 = 0.0;
        var y2 = 0.0;
        var xr = 0.0;
        var yr = 0.0;
        var xya = null;

        if($.track != null) {
            dc.setColor(trackColor, Graphics.COLOR_TRANSPARENT);
            dc.setPenWidth(2);

            xya = $.track.xyArray;

            for(var i = -2; i < xya.size() - 3; i += 2 ) {
                if(i >= 0) {
                    x1 = x2;
                    y1 = y2;
                }
                xr = scaleFactor*(xya[i+2] - x_d);
                yr = scaleFactor*(xya[i+3] - y_d);
                x2 = xs_center + xr*cos_heading_smooth - yr*sin_heading_smooth;
                y2 = ys_center - xr*sin_heading_smooth - yr*cos_heading_smooth;
                if(i >= 0) {
                    dc.drawLine(x1,y1,x2,y2);
                }
            }
        }

        if(Trace.pos_nelements > 0) {
            dc.setColor(Graphics.COLOR_DK_GREEN, Graphics.COLOR_TRANSPARENT);
            xya = Trace.xy;
            for(var i=0; i < Trace.pos_nelements; i += 1) {
                var j = (Trace.pos_start_index + i) % Trace.breadCrumbNumber;
                xr = scaleFactor*(xya[2*j] - x_d);
                yr = scaleFactor*(xya[2*j + 1] - y_d);
                dc.fillCircle(xs_center + xr*cos_heading_smooth - yr*sin_heading_smooth, ys_center - xr*sin_heading_smooth - yr*cos_heading_smooth, 4);
            }
        }
    }

    function drawPositionArrowAndCompass(dc) {

        dc.setColor(Graphics.COLOR_BLUE, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(1);

        var sinHead =Transform.sin_heading_smooth;
        var cosHead =Transform.cos_heading_smooth;

        //var heading;
        var dx1;
        var dy1;
        var dx2;
        var dy2;
        var dx3;
        var dy3;

        if(Transform.northHeading || Transform.centerMap) {
            dx1 =  cursorSizePixel * sinHead;
            dy1 = -cursorSizePixel * cosHead;
            dx2 =  cursorSizePixel * (cosHead - 1.577352 * sinHead);
            dy2 =  cursorSizePixel * (sinHead + 1.577352 * cosHead);
        } else {
            dx1 = 0.0;
            dy1 = -cursorSizePixel;
            dx2 = cursorSizePixel;
            dy2 = 1.577352 * cursorSizePixel;
        }
        dx3 = -3.154704 * dx1 - dx2;
        dy3 = -3.154704 * dy1 - dy2;

        var xy_pos = Transform.xy_2_screen(Transform.x_pos, Transform.y_pos);

        dc.setPenWidth(3);
        var x1 = xy_pos[0] + dx1;
        var y1 = xy_pos[1] + dy1;
        var x2 = xy_pos[0] + dx2;
        var y2 = xy_pos[1] + dy2;
        var x3 = xy_pos[0] - dx1;
        var y3 = xy_pos[1] - dy1;
        var x4 = xy_pos[0] + dx3;
        var y4 = xy_pos[1] + dy3;

        dc.drawLine(x1,y1,x2,y2);
        dc.drawLine(x2,y2,x3,y3);
        dc.drawLine(x3,y3,x4,y4);
        dc.drawLine(x4,y4,x1,y1);

        dc.setColor(backgroundColor, backgroundColor);
        dc.fillCircle(Transform.compass_x,Transform.compass_y,Transform.compass_size);

        if(Transform.northHeading || Transform.centerMap) {
            dx1 = - 0.5*Transform.compass_size;
            dx2 = 0.0;
            dx3 = -dx1;
            dy1 = 0.0;
            dy2 = - Transform.compass_size;
            dy3 = 0.0;
        } else {
            dx1 = -0.5 * Transform.compass_size * cosHead;
            dy1 = +0.5 * Transform.compass_size*sinHead;
            dx2 = -Transform.compass_size * sinHead;
            dy2 = -Transform.compass_size * cosHead;
            dx3 =  -dx1;
            dy3 =  -dy1;
        }

        dc.setColor(Graphics.COLOR_BLUE, Graphics.COLOR_TRANSPARENT);
        var points = [[Transform.compass_x + dx1, Transform.compass_y + dy1 - bottomPadding],
                      [Transform.compass_x - dx2, Transform.compass_y - dy2 - bottomPadding],
                      [Transform.compass_x + dx3, Transform.compass_y + dy3 - bottomPadding]];
        dc.fillPolygon(points);

        dc.setColor(Graphics.COLOR_RED, Graphics.COLOR_TRANSPARENT);
        points[1] = [Transform.compass_x + dx2, Transform.compass_y + dy2 - bottomPadding];
        dc.fillPolygon(points);
    }

    function initialize() {
        GenericView.initialize();
        if($.device.equals("vivoactive")) {
            fontsize=Graphics.FONT_XTINY;
        }
        Trace.reset();
        activity_values = new[2];
        setDarkMode($.isDarkMode);
    }


    // Load your resources here
    function onLayout(dc) {
        //System.println("onLayout(dc)");
        Transform.setPixelDimensions(dc.getWidth(), dc.getHeight());
        cursorSizePixel=Transform.pixelWidth*Transform.SCALE_PIXEL*0.5;
        if($.device.equals("vivoactive")) {
            topPadding = 0.5*dc.getFontAscent(fontsize);
            bottomPadding = 0.5*dc.getFontAscent(fontsize);
        }
    }

    // Called when this View is brought to the foreground. Restore
    // the state of this View and prepare it to be shown. This includes
    // loading resources into memory.
    function onShow() {
        //System.println("onShow()");
        View.onShow();
        // inital zoom level when no track is loaded
        if($.track == null && Transform.zoomLevel == null) {
            Transform.setZoomLevel(5);
        }
    }

    // Update the view
    function onUpdate(dc) {
        // Call the parent onUpdate function to redraw the layout
        //View.onUpdate(dc);
        dc.setColor(backgroundColor, backgroundColor);
        dc.clear();

        if(isNewTrack && $.track != null) {
            isNewTrack = false;
            Trace.reset();
            Transform.newTrack();
        }

        drawTrack(dc);

        if(Transform.x_pos != null) {
            drawPositionArrowAndCompass(dc);
        }

        if(session != null) {
            drawActivityInfo(dc);
        }

        drawScale(dc);
        drawStartStop(dc);

    }

}
