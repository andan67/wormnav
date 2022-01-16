using Toybox.WatchUi;
using Track;

class TrackViewCommon extends GenericView {

    var zoomLevel = null;
    var scaleFactor;
    var refScale = 2.0;
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

    var xSCenter;
    var ySCenter;

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
    var fontHeight = 0;
    var fontsizeNumber = Graphics.FONT_LARGE;
    var padding = 0.0;

    var showElevationPlot = false;

    // var drawTrackTime = 0;

    function drawScale(dc) {
        dc.setColor(foregroundColor, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(2);

        dc.drawLine(x1Scale, y1Scale - padding,
                    x1Scale, y2Scale - padding);
        dc.drawLine(x1Scale, y2Scale - padding,
                    x2Scale, y2Scale - padding);
        dc.drawLine(x2Scale, y2Scale - padding,
                    x2Scale, y1Scale - padding);
        dc.drawText(pixelWidth2, y2Scale - fontHeight - padding,
            fontsize , formattedScale(), Graphics.TEXT_JUSTIFY_CENTER);
    }

    function drawTrack(dc) {

        //var _clock = System.System.getTimer();
        var x1 = 0.0;
        var y1 = 0.0;
        var x2 = 0.0;
        var y2 = 0.0;
        var xr = 0.0;
        var yr = 0.0;

        // copy class variables into lcoally defined variables to speed up processing 
        var _xSCenter = xSCenter;
        var _ySCenter = ySCenter;
        var _xCenter = xCenter;
        var _yCenter = yCenter;
        var _scaleFactor = scaleFactor;
        var _cosTransform = cosTransform;
        var _sinTransform = sinTransform;

        var _xArray = null;
        var _yArray = null;
        var _nPoints = null;

        if(Track.hasTrackData) {
            dc.setColor(trackColor, Graphics.COLOR_TRANSPARENT);
            dc.setPenWidth(2);

            _xArray = Track.xArray;
            _yArray = Track.yArray;
            _nPoints = Track.nPoints;

            for(var i = 0; i < _nPoints; i++ ) {

                if(i > 0) {
                    x1 = x2;
                    y1 = y2;
                }
                xr = _scaleFactor * (_xArray[i] - _xCenter);
                yr = _scaleFactor * (_yArray[i] - _yCenter);
                x2 = _xSCenter + xr * _cosTransform - yr * _sinTransform;
                y2 = _ySCenter - xr * _sinTransform - yr * _cosTransform;

                if(i > 0) {
                    dc.drawLine(x1, y1, x2, y2);
                }

            }
        }

        // draw breadcrumbs
        if(Track.pos_nelements > 0) {
            dc.setColor(Graphics.COLOR_DK_GREEN, Graphics.COLOR_TRANSPARENT);
            // (x,y) coordinates of recorded breadcrumb points
            var _xya = Track.bxy;
            _nPoints = Track.pos_nelements;
            for(var i = 0; i < _nPoints; i += 1) {
                var j = (Track.pos_start_index + i) % Track.breadCrumbNumber;
                xr = _scaleFactor * (_xya[2 * j] - _xCenter);
                yr = _scaleFactor * (_xya[2 * j + 1] - _yCenter);
                dc.fillCircle(_xSCenter + xr * _cosTransform - yr * _sinTransform,
                              _ySCenter - xr * _sinTransform - yr * _cosTransform, 4);
            }
        }
        _xArray = null;
        _yArray = null;
        // drawTrackTime = System.getTimer() - _clock;
    }

    function drawProfile(dc) {

        // var _clock = System.System.getTimer();
        var x1 = 0.0;
        var y1 = 0.0;
        var x2 = 0.0;
        var y2 = 0.0;

        var d2 = Track.EARTH_RADIUS * Track.EARTH_RADIUS;
        var nearestPointIndex = -1;
        var nearestPointLambda = 0.0;
        var findNearestPoint = Track.onPositionCalled;
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

        // copy class variables into lcoally defined variables to speed up processing 
        var _eleWidth = eleWidth;
        var _eleHeight = eleHeight;
        var _scaleEleX = scaleEleX;
        var _scaleEleY = (Track.eleMaxTrack -  Track.eleMinTrack) > 1.0 ? 1.0 * eleHeight / (Track.eleMaxTrack -  Track.eleMinTrack) : 0.0;
        var _eleMinTrack = Track.eleMinTrack;
        var _x1Ele = x1Ele;
        var _y1Ele = y1Ele;
        var _x2Ele = x2Ele;
        var _y2Ele = y2Ele + _scaleEleY * _eleMinTrack;

        var _xp = Track.xPos;
        var _yp = Track.yPos;

        var _xArray = null;
        var _yArray = null;
        var _eleArray = null;
        var _nPoints = 0;

        if(Track.hasElevationData) {
            dc.setColor(trackColor, Graphics.COLOR_TRANSPARENT);
            dc.setPenWidth(2);
            _xArray = Track.xArray;
            _yArray = Track.yArray;
            _eleArray = Track.eleArray;
            _nPoints = Track.nPoints;

            for(var i = 0; i < _nPoints; i++ ) {
                if(i > 0) {
                    x1 = x2;
                    y1 = y2;
                    xt1 = xt2;
                    yt1 = yt2;
                }
                // track coordinates
                xt2 = _xArray[i];
                yt2 = _yArray[i];

                // x1,x2: screen coordinate of distance on track from start to previous/current point
                // y1,y2: screnn coordiante of elevation of previous /current point
                x2 = _x1Ele + _scaleEleX * dss;
                y2 = _y2Ele - _scaleEleY * _eleArray[i];

                if(i > 0) {
                    dc.drawLine(x1, y1, x2, y2);
                    xs = xt2 - xt1;
                    ys = yt2 - yt1;
                    ds2 = xs * xs + ys * ys;
                    // distance between current and last point
                    ds = Math.sqrt(ds2);
                    // dss is cummulated distance along track until point i
                    dss += ds;
                    if(findNearestPoint) {
                        s = (xs * (_xp - xt1) + ys * (_yp - yt1));
                        if(s <= 0.0 || ds2 < 1.0e-12) {
                            s = 0.0;
                        } else if(s >= ds2) {
                            if(i == _nPoints - 1) {
                                s = 1.0;
                            } else {
                                continue;
                            }
                        } else {
                            s /= ds2;
                        }
                        // calculate distance between current position and projection onto track segement defined by current and previous point
                        dx = _xp - (xt1 + s * xs);
                        dy = _yp - (yt1 + s * ys);
                        ds2 = dx * dx + dy * dy;
                        // if minumum is found -> candidate for nearest point
                        if(ds2 < d2) {
                            nearestPointIndex = i - 1;
                            nearestPointLambda = s;
                            // distance to nearest point on track from start
                            dssn = dss + s * ds;
                            d2 = ds2;
                        }
                    }
                }
            }
        }

        _y2Ele = y2Ele;
        dc.setColor(foregroundColor, Graphics.COLOR_TRANSPARENT);

        // draw axes
        dc.drawLine(_x1Ele, _y2Ele, _x2Ele, _y2Ele);
        dc.drawLine(_x2Ele, _y2Ele, _x2Ele, _y1Ele);
        dc.drawLine(_x1Ele, _y1Ele, _x1Ele, _y2Ele);

        // draw axes' tick marks
        for(var w = 0.25; w <= 0.75; w += 0.25) {
            dc.drawLine(_x1Ele + w * _eleWidth, _y2Ele, _x1Ele + w * _eleWidth, y2Ele + 0.04 * _eleWidth );
            dc.drawLine(_x1Ele , _y2Ele - w * _eleHeight, x1Ele - 0.04 * _eleWidth, _y2Ele - w * _eleHeight );
            dc.drawLine(_x2Ele , _y2Ele - w * _eleHeight, x2Ele + 0.04 * _eleWidth, _y2Ele - w * _eleHeight );
        }

        // draw min/max elevation values
        var ed = 0.08 * (pixelWidth2 - _x1Ele);
        dc.drawText(_x2Ele - ed,  _y2Ele + 1.5 * ed, fontsize, Track.eleMinTrack.format("%d"), Graphics.TEXT_JUSTIFY_RIGHT | Graphics.TEXT_JUSTIFY_VCENTER);
        dc.drawText(_x2Ele - ed,  _y1Ele - 1.5 * ed - dc.getFontAscent(fontsize), fontsize, "^" + Track.eleTotAscent.format("%d"), Graphics.TEXT_JUSTIFY_RIGHT | Graphics.TEXT_JUSTIFY_VCENTER);
        dc.drawText(_x2Ele - ed,  _y1Ele - 1.5 * ed, fontsize, Track.eleMaxTrack.format("%d"), Graphics.TEXT_JUSTIFY_RIGHT | Graphics.TEXT_JUSTIFY_VCENTER);

        // draw length
        dc.drawText(pixelWidth2, 1.12 * _y2Ele, fontsize , Track.xyLengthLabel, Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);

        // draw nearest point on track
        if(nearestPointIndex >= 0) {
            //System.println("nearestPointIndex:" + nearestPointIndex);
            x2 = _x1Ele + _scaleEleX * dssn;
            y2 = _y2Ele - _scaleEleY * (Track.eleAct - Track.eleMinTrack);

            // draw elevation from nearest point on track
            dc.setColor(trackColor, Graphics.COLOR_TRANSPARENT );
            // interpolate elevation
            eleTrack = _eleArray[nearestPointIndex] + nearestPointLambda * (_eleArray[nearestPointIndex + 1] - _eleArray[nearestPointIndex]);
            Track.eleTrack = eleTrack;
            dc.drawText(_x1Ele + ed,  _y2Ele + 1.5 * ed, fontsize, eleTrack.format("%d"), Graphics.TEXT_JUSTIFY_LEFT | Graphics.TEXT_JUSTIFY_VCENTER);

            // draw actual elevation
            dc.setColor(cursorColor, backgroundColor);
            dc.drawText(_x1Ele + ed,  _y1Ele - 1.5 * ed, fontsize, Track.eleAct.format("%d"), Graphics.TEXT_JUSTIFY_LEFT | Graphics.TEXT_JUSTIFY_VCENTER);
            dc.drawText(_x1Ele + ed,  _y1Ele - 1.5 * ed - dc.getFontAscent(fontsize), fontsize, "^" + Track.eleTotAscentAct.format("%d"), Graphics.TEXT_JUSTIFY_LEFT | Graphics.TEXT_JUSTIFY_VCENTER);
            dc.drawLine(x2, _y2Ele, x2, _y1Ele);

            // draw small horizontal line at actual elevation
            dc.drawLine(x2 - 2, y2, x2 + 2, y2);

            // draw distance to end of course
            dc.drawText(pixelWidth2, 1.12 * _y2Ele + 0.8 * dc.getFontHeight(fontsize), fontsize , Track.formatLength(Track.xyLength - dssn), 
                Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
            //}
        }

        _xArray = null;
        _yArray = null;
        _eleArray = null;
        // drawTrackTime = System.getTimer() - _clock;
    }

    function drawPositionArrowAndCompass(dc) {

        var _sin = 0.0;
        var _cos = 1.0;
        if(Track.northHeading || Track.centerMap) {
            _sin = Track.sin_heading_smooth;
            _cos = Track.cos_heading_smooth;
        }

        var _dx1 =  sizeCursor * _sin;
        var _dy1 = -sizeCursor * _cos;
        var _dx2 = -_dy1 - 1.577352 * _dx1;
        var _dy2 =  _dx1 - 1.577352 * _dy1;
        var _dx3 = -3.154704 * _dx1 - _dx2;
        var _dy3 = -3.154704 * _dy1 - _dy2;

        var _xy = xy2Screen(Track.xPos, Track.yPos);

        var _x1 = _xy[0] + _dx1;
        var _y1 = _xy[1] + _dy1;
        var _x2 = _xy[0] + _dx2;
        var _y2 = _xy[1] + _dy2;
        var _x3 = _xy[0] - _dx1;
        var _y3 = _xy[1] - _dy1;
        var _x4 = _xy[0] + _dx3;
        var _y4 = _xy[1] + _dy3;

        // draw position cursor
        dc.setColor(cursorColor, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(3);
        dc.drawLine(_x1, _y1, _x2, _y2);
        dc.drawLine(_x2, _y2, _x3, _y3);
        dc.drawLine(_x3, _y3, _x4, _y4);
        dc.drawLine(_x4, _y4, _x1, _y1);

        // clear compass area
        dc.setColor(backgroundColor, backgroundColor);
        dc.fillCircle(xCompass, yCompass, sizeCompass);

        _dx1 = - 0.5 * sizeCompass * cosTransform;
        _dy1 = + 0.5 * sizeCompass * sinTransform;
        _dx2 = - sizeCompass * sinTransform;
        _dy2 = - sizeCompass * cosTransform;

        // north part of compass
        var points = [[xCompass + _dx1, yCompass + _dy1 - padding],
                      [xCompass - _dx2, yCompass - _dy2 - padding],
                      [xCompass - _dx1, yCompass - _dy1 - padding]];
        dc.setColor(Graphics.COLOR_BLUE, Graphics.COLOR_TRANSPARENT);
        dc.fillPolygon(points);

        // south part of compass
        points[1] = [xCompass + _dx2, yCompass + _dy2 - padding];
        dc.setColor(Graphics.COLOR_RED, Graphics.COLOR_TRANSPARENT);
        dc.fillPolygon(points);

    }

    function initialize() {
        GenericView.initialize();
        Track.resetPosition();
        Track.resetBreadCrumbs(null);
    }

    function xy2Screen(x, y) {
        var xr = scaleFactor * (x - xCenter);
        var yr = scaleFactor * (y - yCenter);
        return [xSCenter + xr * cosTransform - yr * sinTransform,
                ySCenter - xr * sinTransform - yr * cosTransform];
    }

    // Load your resources here
    function onLayout(dc) {
        pixelWidth = dc.getWidth();
        pixelWidth2 = 0.5 * pixelWidth;
        pixelHeight = dc.getHeight();
        pixelHeight2 = 0.5 * pixelHeight;
        pixelHeight3 = 0.6666667 * pixelHeight;
        pixelMin = pixelWidth < pixelHeight ? pixelWidth : pixelHeight;

        // coordinates for drawing the scale
        x1Scale = 0.4 * pixelWidth;
        y1Scale = 0.96 * pixelHeight;
        x2Scale = 0.6 * pixelWidth;
        y2Scale = 0.98 * pixelHeight;

        // size and coordinates for the compass
        sizeCompass = 0.25 * (x2Scale - x1Scale);
        xCompass = x2Scale + 2 * sizeCompass;
        yCompass = y2Scale - sizeCompass;

        // size of position cursor
        sizeCursor = 0.05 * pixelWidth;

        // box for elevation plot
        eleWidth = 0.8 * pixelWidth;
        //eleHeight = 0.4 * pixelWidth;
        eleHeight = 0.32 * pixelWidth;
        x1Ele = pixelWidth2 -0.5 * eleWidth ;
        y1Ele = pixelHeight2 - 0.4 * eleHeight;
        x2Ele = x1Ele + eleWidth;
        y2Ele = pixelHeight2 + 0.6 * eleHeight;

        fontHeight = dc.getFontHeight(fontsize);
    }

    // Called when this View is brought to the foreground. Restore
    // the state of this View and prepare it to be shown. This includes
    // loading resources into memory.
    function onShow() {
        View.onShow();
        // inital zoom level when no track is loaded
        if(zoomLevel == null && !Track.hasTrackData) {
            setZoomLevel(5);
        }
    }

    // Update the view
    function onUpdate(dc) {
        // Call the parent onUpdate function to redraw the layout
        //View.onUpdate(dc);
        dc.setColor(backgroundColor, backgroundColor);
        dc.clear();

        if(isNewTrack) {
            isNewTrack = false;
            setZoomLevel(null);
            if(Track.hasElevationData) {
                scaleEleX = eleWidth / Track.xyLength;
            }

        }

        xSCenter = pixelWidth2;
        ySCenter = pixelHeight2;

        if(Track.northHeading || Track.centerMap || !Track.onPositionCalled) {
            sinTransform = 0.0;
            cosTransform = 1.0;
        } else {
            sinTransform = Track.sin_heading_smooth;
            cosTransform = Track.cos_heading_smooth;
            ySCenter = pixelHeight3;
        }

        if(Track.centerMap) {
            // the x,y cooridnate origin is the center of the map by construction
            xCenter = 0.0;
            yCenter = 0.0;
        } else {
            xCenter = Track.xPos;
            yCenter = Track.yPos;
        }

        if(!showElevationPlot) {
            drawTrack(dc);
        } else {
            drawProfile(dc);
        }

        if(Track.onPositionCalled && !showElevationPlot) {
            drawPositionArrowAndCompass(dc);
        }

        // draw activity info
        if($.session != null) {
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
                dc.drawText(pixelWidth2, padding + (1 + 2 * i) * y,
                    actualFontsize,
                    //Data.dataFieldSLabels[j] +": "+
                    Data.getDataFieldLabelValue(j)[1], Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
            }
            // show computation time for draw track or draw profile for testing performance on real devices
            /*
            dc.drawText(pixelWidth2, padding + (1 + 2 * i) * y,
                    actualFontsize,
                    //Data.dataFieldSLabels[j] +": "+
                    drawTrackTime, Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
            */
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
                if(pixelMin / (0.2 * pixelWidth)* SCALES[zoomLevel] * 0.95 > Track.diagonal) {
                    break;
                }
            }
        } else {
            if(l == -1 && zoomLevel > 0) {
                zoomLevel -= 1;
            } else if(l == -2 && zoomLevel < SCALES.size() - 1) {
                zoomLevel += 1;
            } else if(l >= 0 && l <= SCALES.size()) {
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
