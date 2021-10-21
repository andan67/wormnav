using Toybox.WatchUi;
using Track;

class TrackView extends GenericView {

    var zoomLevel = null;
    var scaleFactor;
    var refScale = 2.0;
    const SCALE_PIXEL = 0.1;
    const SCALES = [12.0, 20.0, 30.0, 50.0, 80.0,
                    120.0, 200.0, 300.0, 500.0, 800.0,
                    1200.0, 2000.0, 3000.0, 5000.0, 8000.0,
                    12000.0, 20000.0, 30000.0, 50000.0, 80000.0,
                    120000.0, 200000.0, 300000.0, 500000.0, 800000.0];

    // used for drawing track

    var pixelHeight;
    var pixelWidth;
    var pixelWidth2;
    var pixelHeight2;
    var pixelHeight3;
    var pixelMin;

    var xs_center;
    var ys_center;

    var x1Scale;
    var y1Scale;
    var x2Scale;
    var y2Scale;
    var xCompass;
    var yCompass;
    var sizeCompass;

    var eleWidth;
    var eleHeight;
    var x1Ele;
    var y1Ele;
    var x2Ele;
    var y2Ele;
    var eleTrack;
    var scaleEleX;
    var scaleEleY;

    // position data
    var sinTransform = 0.0;
    var cosTransform = 1.0;
    var xCenter = 0.0;
    var yCenter = 0.0;

    var sizeCursor;
    var posCursor;
    var isNewTrack = false;
    var fontsize = Graphics.FONT_MEDIUM;
    var fontsizeNumber = Graphics.FONT_LARGE;
   
    var topPadding = 0.0;
    var bottomPadding = 0.0;

    var showElevationPlot = false;

    function drawScale(dc) {
        dc.setColor(foregroundColor, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(2);

        dc.drawLine(x1Scale, y1Scale - bottomPadding,
                    x1Scale, y2Scale - bottomPadding);
        dc.drawLine(x1Scale, y2Scale - bottomPadding,
                    x2Scale, y2Scale - bottomPadding);
        dc.drawLine(x2Scale, y2Scale - bottomPadding,
                    x2Scale, y1Scale - bottomPadding);
        dc.drawText(pixelWidth2, y2Scale - dc.getFontHeight(fontsize) - bottomPadding,
            fontsize , formattedScale(), Graphics.TEXT_JUSTIFY_CENTER);
    }

    function drawActivityInfo(dc) {
        if(!$.session.isRecording()) {
            dc.setColor(Graphics.COLOR_RED, Graphics.COLOR_TRANSPARENT);
        } else {
            dc.setColor(foregroundColor, Graphics.COLOR_TRANSPARENT);
        }
        var actualFontsize = $.trackViewLargeFont ? fontsizeNumber : fontsize;
        var y = 0.5 * dc.getFontAscent(actualFontsize);
        var i;
        for(i = 0; i < Data.getField(3, 0); i++) {
            // index of data field
            var j = Data.getField(3, i + 1);
            dc.drawText(pixelWidth2, topPadding + (1 + 2 * i) * y,
                actualFontsize,
                //Data.dataFieldSLabels[j] +": "+
                Data.getDataFieldLabelValue(j)[1], Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
        }
    }

    function drawTrack(dc) {

        xs_center = pixelWidth2;
        ys_center = pixelHeight2;

        if( !Track.northHeading && !Track.centerMap) {
            ys_center = pixelHeight3;
        }

        var x1 = 0.0;
        var y1 = 0.0;
        var x2 = 0.0;
        var y2 = 0.0;
        var xr = 0.0;
        var yr = 0.0;
        var xya = null;
        var ele = null;       

        var d2 = Track.EARTH_RADIUS * Track.EARTH_RADIUS;
        var dxy2 = 0.0;
        var xp = Track.xPos;
        var yp = Track.yPos;
        var nearestPointIndex = -1;
        var nearestPointLambda = 0.0;
        var findNearestPoint = Track.onPositionCalled && Track.findNearestPoint;
        var dx = 0.0;
        var dy = 0.0;
        var xt1 = 0.0;
        var yt1 = 0.0;
        var xt2 = 0.0;
        var yt2 = 0.0;
        var xs = 0.0;
        var ys = 0.0;
        var ds = 0.0;
        var ds2 = 0.0;
        var dss = 0.0;
        var dssn = 0.0;
        var s = 0.0;

        if($.track != null) {
            dc.setColor(trackColor, Graphics.COLOR_TRANSPARENT);
            dc.setPenWidth(2);

            xya = $.track.xyArray;
            ele = $.track.eleArray;

            for(var i = -2; i < xya.size() - 3; i += 2 ) {
                if(i >= 0) {
                    x1 = x2;
                    y1 = y2;
                    xt1 = xt2;
                    yt1 = yt2;
                }
                xt2 = xya[i + 2];
                yt2 = xya[i + 3];

                if(!showElevationPlot) {
                    xr = scaleFactor * (xt2 - xCenter);
                    yr = scaleFactor * (yt2 - yCenter);
                    x2 = xs_center + xr * cosTransform - yr * sinTransform;
                    y2 = ys_center - xr * sinTransform - yr * cosTransform;
                } else {                    
                    xr = scaleEleX * dss;
                    yr = scaleEleY * (ele[(i + 2) / 2] - Track.eleMinTrack);
                    x2 = x1Ele + xr;
                    y2 = y2Ele - yr;
                }

                if(i >= 0) {
                    dc.drawLine(x1, y1, x2, y2);

                    if(findNearestPoint || showElevationPlot) {
                        xs = xt2 - xt1;
                        ys = yt2 - yt1;
                        ds2 = xs * xs + ys * ys;
                        if(showElevationPlot) {
                            ds = Math.sqrt(ds2);
                            dss += ds;
                        }
                        s = (xs * (xp - xt1) + ys * (yp - yt1));
                        if(s <= 0.0) {
                            s = 0.0;
                        } else {
                            //ds2 = xs * xs + ys * ys;
                            if(ds2 < 1.0e-12) {
                                s = 0.0;
                            }
                            else if(s >= ds2) {
                                if(i == xya.size() - 4) {
                                    s = 1.0;
                                } else {
                                    continue;
                                }
                            } else {
                                s /= ds2;
                            }
                        }
                        dx = xp - (xt1 + s * xs);
                        dy = yp - (yt1 + s * ys);
                        dxy2 = dx * dx + dy * dy;
                        if(dxy2 < d2) {
                            nearestPointIndex = i;
                            nearestPointLambda = s;
                            dssn = dss + s * ds;
                            d2 = dxy2;
                        }
                    }
                }
            }
        }

        if(showElevationPlot) {
            dc.setColor(foregroundColor, Graphics.COLOR_TRANSPARENT);
            
            // draw axes
            dc.drawLine(x1Ele, y2Ele, x2Ele, y2Ele);
            dc.drawLine(x2Ele, y2Ele, x2Ele, y1Ele);
            dc.drawLine(x1Ele, y1Ele, x1Ele, y2Ele);
            
            // draw axes' tick marks
            for(var w = 0.25; w <= 0.75; w += 0.25) {
                dc.drawLine(x1Ele + w * eleWidth, y2Ele, x1Ele + w * eleWidth, y2Ele + 0.04 * eleWidth );
                dc.drawLine(x1Ele , y2Ele - w * eleHeight, x1Ele - 0.04 * eleWidth, y2Ele - w * eleHeight );
                dc.drawLine(x2Ele , y2Ele - w * eleHeight, x2Ele + 0.04 * eleWidth, y2Ele - w * eleHeight );   
            }
            
            // draw min/max elevation values 
            var ed = 0.1 * (pixelWidth2 - x1Ele);
            dc.drawText(x2Ele - ed,  y2Ele + 1.5 * ed, fontsize, Track.eleMinTrack.format("%d"), Graphics.TEXT_JUSTIFY_RIGHT | Graphics.TEXT_JUSTIFY_VCENTER);
            dc.drawText(x2Ele - ed,  y1Ele - 1.5 * ed, fontsize, Track.eleMaxTrack.format("%d"), Graphics.TEXT_JUSTIFY_RIGHT | Graphics.TEXT_JUSTIFY_VCENTER);
            
            // draw length
            dc.drawText(pixelWidth2, 1.12 * y2Ele, fontsize , $.track.xyLengthLabel, Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);

        }

        // draw nearest point on track
        if(showElevationPlot && findNearestPoint && nearestPointIndex >= 0) {           
            // if(!showElevationPlot) {
            //     dc.setColor(cursorColor, Graphics.COLOR_TRANSPARENT);
            //     x1 = xya[nearestPointIndex] + nearestPointLambda * (xya[nearestPointIndex + 2] -  xya[nearestPointIndex]);
            //     y1 = xya[nearestPointIndex + 1] + nearestPointLambda * (xya[nearestPointIndex + 3] -  xya[nearestPointIndex + 1]);
            //     var xy_pos = xy2Screen(x1, y1);
            //     dc.fillCircle(xy_pos[0], xy_pos[1], 4);
            // } else {
            xr = scaleEleX * dssn;
            yr = scaleEleY * (Track.ele - Track.eleMinTrack);
            x2 = x1Ele + xr;
            y2 = y2Ele - yr;

            var ed = 0.1 * (pixelWidth2 - x1Ele);
            
            // draw elevation from nearest point on track
            dc.setColor(trackColor, Graphics.COLOR_TRANSPARENT );
            eleTrack = ele[nearestPointIndex / 2] + nearestPointLambda * (ele[nearestPointIndex / 2 + 1 ] - ele[nearestPointIndex / 2]);
            Track.eleTrack = eleTrack;
            dc.drawText(x1Ele + ed,  y2Ele + 1.5 * ed, fontsize, eleTrack.format("%d"), Graphics.TEXT_JUSTIFY_LEFT | Graphics.TEXT_JUSTIFY_VCENTER);
            
            // draw actual elevation
            dc.setColor(cursorColor, backgroundColor);
            dc.drawText(x1Ele + ed,  y1Ele - 1.5 * ed, fontsize, Track.ele.format("%d"), Graphics.TEXT_JUSTIFY_LEFT | Graphics.TEXT_JUSTIFY_VCENTER);
            dc.drawLine(x2, y2Ele, x2, y1Ele);
            
            // draw small horizontal line at actual elevation
            dc.drawLine(x2 - 2, y2, x2 + 2, y2);

            // draw distance to end of course
            dc.drawText(pixelWidth2, 1.12 * y2Ele + 0.8 * dc.getFontHeight(fontsize), fontsize , Track.formatLength($.track.xyLength - dssn), 
                Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);         
            //}
        }

        // draw breadcrumbs
        if(!showElevationPlot && Track.pos_nelements > 0) {
            dc.setColor(Graphics.COLOR_DK_GREEN, Graphics.COLOR_TRANSPARENT);
            // (x,y) coordinates of recorded breadcrumb points
            xya = Track.bxy;
            for(var i = 0; i < Track.pos_nelements; i += 1) {
                var j = (Track.pos_start_index + i) % Track.breadCrumbNumber;
                xr = scaleFactor * (xya[2 * j] - xCenter);
                yr = scaleFactor * (xya[2 * j + 1] - yCenter);
                dc.fillCircle(xs_center + xr * cosTransform - yr * sinTransform,
                              ys_center - xr * sinTransform - yr * cosTransform, 4);
            }
        }
    }

    function drawPositionArrowAndCompass(dc) {

        dc.setColor(cursorColor, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(1);

        var dx1;
        var dy1;
        var dx2;
        var dy2;
        var dx3;
        var dy3;

        var _sin = 0.0;
        var _cos = 1.0;

        if(Track.northHeading || Track.centerMap) {
            _sin =  Track.sin_heading_smooth;
            _cos =  Track.cos_heading_smooth;
        } 
        dx1 =  sizeCursor * _sin;
        dy1 = -sizeCursor * _cos;
        dx2 = -dy1 - 1.577352 * dx1;
        dy2 =  dx1 - 1.577352 * dy1;            
        dx3 = -3.154704 * dx1 - dx2;
        dy3 = -3.154704 * dy1 - dy2;

        var xy_pos = xy2Screen(Track.xPos, Track.yPos);

        dc.setPenWidth(3);
        var x1 = xy_pos[0] + dx1;
        var y1 = xy_pos[1] + dy1;
        var x2 = xy_pos[0] + dx2;
        var y2 = xy_pos[1] + dy2;
        var x3 = xy_pos[0] - dx1;
        var y3 = xy_pos[1] - dy1;
        var x4 = xy_pos[0] + dx3;
        var y4 = xy_pos[1] + dy3;

        dc.drawLine(x1, y1, x2, y2);
        dc.drawLine(x2, y2, x3, y3);
        dc.drawLine(x3, y3, x4, y4);
        dc.drawLine(x4, y4, x1, y1);

        dc.setColor(backgroundColor, backgroundColor);
        dc.fillCircle(xCompass, yCompass, sizeCompass);

        dx1 = - 0.5 * sizeCompass * cosTransform;
        dy1 = + 0.5 * sizeCompass * sinTransform;
        dx2 = - sizeCompass * sinTransform;
        dy2 = - sizeCompass * cosTransform;
        dx3 = - dx1;
        dy3 = - dy1;
        

        dc.setColor(Graphics.COLOR_BLUE, Graphics.COLOR_TRANSPARENT);
        var points = [[xCompass + dx1, yCompass + dy1 - bottomPadding],
                      [xCompass - dx2, yCompass - dy2 - bottomPadding],
                      [xCompass + dx3, yCompass + dy3 - bottomPadding]];
        dc.fillPolygon(points);

        dc.setColor(Graphics.COLOR_RED, Graphics.COLOR_TRANSPARENT);
        points[1] = [xCompass + dx2, yCompass + dy2 - bottomPadding];
        dc.fillPolygon(points);

    }

    function initialize() {
        GenericView.initialize();
        if($.device.equals("vivoactive")) {
            fontsize=Graphics.FONT_XTINY;
        }
        Track.resetPosition();
        Track.resetBreadCrumbs(null);
    }

    function xy2Screen(x, y) {      
        var xr = scaleFactor * (x - xCenter);
        var yr = scaleFactor * (y - yCenter);
        return [xs_center + xr * cosTransform - yr * sinTransform,
                ys_center - xr * sinTransform - yr * cosTransform];    
    }

    // Load your resources here
    function onLayout(dc) {
        //System.println("onLayout(dc)");
        pixelWidth = dc.getWidth();
        pixelWidth2 = 0.5 * pixelWidth;
        pixelHeight = dc.getHeight();
        pixelHeight2 = 0.5 * pixelHeight;
        pixelHeight3 = 0.6666667 * pixelHeight;
        pixelMin = pixelWidth < pixelHeight ? pixelWidth : pixelHeight;
        
        // coordinates for drawing the scale
        x1Scale = pixelWidth * (0.5 - SCALE_PIXEL);
        y1Scale = (1.0 - 0.45 * SCALE_PIXEL) * pixelHeight;
        x2Scale = pixelWidth * (0.5 + SCALE_PIXEL);
        y2Scale = (1.0 - 0.2 * SCALE_PIXEL) * pixelHeight;
        
        // size and coordinates for the compass
        sizeCompass = 0.25 * (x2Scale - x1Scale);
        xCompass = x2Scale + 2 * sizeCompass;
        yCompass = y2Scale - sizeCompass;

        // size of position cursor
        sizeCursor = pixelWidth * SCALE_PIXEL * 0.5;

        // box for elevation plot
        eleWidth = 0.8 * pixelWidth;
        eleHeight = 0.4 * pixelWidth;
        x1Ele = pixelWidth2 -0.5 * eleWidth ;
        y1Ele = pixelHeight2 - 0.5 * eleHeight;
        x2Ele = x1Ele + eleWidth;
        y2Ele = pixelHeight2 + 0.5 * eleHeight;

        if($.device.equals("vivoactive")) {
            topPadding = 0.5 * dc.getFontAscent(fontsize);
            bottomPadding = 0.5 * dc.getFontAscent(fontsize);
        }
    }

    // Called when this View is brought to the foreground. Restore
    // the state of this View and prepare it to be shown. This includes
    // loading resources into memory.
    function onShow() {
        //System.println("onShow()");
        View.onShow();
        // inital zoom level when no track is loaded
        if(zoomLevel == null && $.track == null) {
            setZoomLevel(5);
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
            Track.resetPosition();
            Track.newTrack();
            setZoomLevel(null);
            if($.track != null && $.track.eleArray != null) {
                scaleEleX = eleWidth / $.track.xyLength;
                scaleEleY = ($.track.eleMax -  $.track.eleMin) > 1 ? 0.8 * eleHeight / ($.track.eleMax -  $.track.eleMin) : 0.0;
            }
        }

        if(Track.northHeading || Track.centerMap || !Track.onPositionCalled) {
            sinTransform = 0.0;
            cosTransform = 1.0;
        } else {
            sinTransform = Track.sin_heading_smooth;
            cosTransform = Track.cos_heading_smooth;          
        }

        if(Track.centerMap) {
            // the x,y cooridnate origin is the center of the map by construction
            xCenter = 0.0;
            yCenter = 0.0;    
        } else {
            xCenter = Track.xPos;
            yCenter = Track.yPos;
        }

        drawTrack(dc);

        if(Track.xPos != null && !showElevationPlot) {
            drawPositionArrowAndCompass(dc);
        }

        if($.session != null) {
            drawActivityInfo(dc);
        }

        if(!showElevationPlot) {
            drawScale(dc);
        }

        drawStartStop(dc);

    }

    function setZoomLevel(l) {
        if(l == null) {
            // fit to track size
            zoomLevel = 0;
            for(zoomLevel= 0; zoomLevel < SCALES.size(); zoomLevel += 1 ) {
                if(pixelMin / (0.2 * pixelWidth)* SCALES[zoomLevel] * 0.95 > $.track.diagonal) {
                    break;
                }
            }
        } else {
            if(l == -1 && zoomLevel > 0) {
                zoomLevel -=1;
            } else if(l == -2 && zoomLevel < SCALES.size() - 1) {
                zoomLevel += 1;
            } else if(l >=0 && l <= SCALES.size()) {
                zoomLevel = l;
            }
        }
        refScale = SCALES[zoomLevel];
        scaleFactor = 0.2 * pixelWidth / refScale * Track.EARTH_RADIUS;
        return zoomLevel;
    }


    function formattedScale() {
        return (refScale < 1000.0) ? refScale.format("%d") + "m" : (0.001 * refScale).format("%.1f") + "k";
    }

}
