using Toybox.Math;
using Toybox.System;
using Trace;

module Transform {

    const EARTH_RADIUS = 6371000.0;
    const PI = 3.1415927;
    const PI2_3 = 0.6666667*PI;
    const ANGLE_R =0.5*PI+Math.atan2(3.0, 2.0);
    const ANGLE_L =1.5*PI-Math.atan2(3.0, 2.0);

    var refScale = 2.0;
    const SCALE_PIXEL = 0.1;
  
    var zoomLevel;
    var scaleFactor;

    var x_pos;
    var y_pos;
    var last_x_pos;
    var last_y_pos;

    var lat_view_center;
    var lon_view_center;
    var cos_lat_view_center;
    var sin_lat_view_center;
    var xs_center;
    var ys_center;
    var isTrackCentered;
    var lat_first_position=null;
    var lon_first_position=null;

    var x_d;
    var y_d;

    var northHeading=true;
    var centerMap=false;
    var heading_smooth=-1.0;
    var cos_heading_smooth;
    var sin_heading_smooth;
   
    var pixelHeight;
    var pixelWidth;
    var pixelWidth2;
    var pixelHeight2;
    var pixelHeight3;
    var pixelMin;

    var scale_x1;
    var scale_y1;
    var scale_x2;
    var scale_y2;
    var compass_x;
    var compass_y;
    var compass_size;

    function setPixelDimensions(width, height) {
        pixelWidth = width;
        pixelWidth2 = 0.5*pixelWidth;
        pixelHeight = height;
        pixelHeight2 = 0.5 * pixelHeight;
        pixelHeight3 = 0.6666667*pixelHeight;
        pixelMin =pixelWidth < pixelHeight ? pixelWidth : pixelHeight;
        scale_x1 = pixelWidth*(0.5-SCALE_PIXEL);
        scale_y1 = (1.0-0.45*SCALE_PIXEL)*pixelHeight;
        scale_y2 = (1.0-0.2*SCALE_PIXEL)*pixelHeight;
        scale_x2 = pixelWidth*(0.5+SCALE_PIXEL);
        compass_size = 0.25*(scale_x2-scale_x1);
        compass_x = scale_x2 + 2*compass_size;
        compass_y = scale_y2 - compass_size;
    }

    function setViewCenter(lat, lon) {
        if(!centerMap) {
            var ll = lon-lon_view_center;
            var cos_lat = Math.cos(lat);
            x_d = cos_lat*Math.sin(ll);
            y_d = cos_lat_view_center*Math.sin(lat)-sin_lat_view_center*cos_lat*Math.cos(ll);
            xs_center = pixelWidth2;
            if(northHeading || isTrackCentered) {
                ys_center = pixelHeight2;
            }
            else {
                ys_center = pixelHeight3;
            }
        }
        else {
           x_d=0.0;
           y_d=0.0;
           xs_center = pixelWidth2;
           ys_center = pixelHeight2;
        }
    }

    function setHeading() {
        if(last_x_pos != null) {
            //heading_smooth = (1-SMOOTH_FACTOR)*Math.atan2(x_pos-last_x_pos, y_pos-last_y_pos) + SMOOTH_FACTOR*heading_smooth ;
            //System.println("Trace.positionDistance: " +Trace.positionDistance);
            var sf = (1 - Math.pow(Math.E, -0.1*Trace.positionDistance));
            heading_smooth = heading_smooth + sf*(Math.atan2(x_pos-last_x_pos, y_pos-last_y_pos) - heading_smooth) ;
            //heading_smooth = Math.atan2(x_pos-last_x_pos, y_pos-last_y_pos);
        }
        else {
            heading_smooth = 0.0;
        }
        /*
        if(heading_smooth < 0.0) {
            heading_smooth = 2.0*PI+heading_smooth;
        }
        */
        cos_heading_smooth = Math.cos(heading_smooth);
        sin_heading_smooth = Math.sin(heading_smooth);
    }

    function resetHeading() {
        heading_smooth = 0.0;
        cos_heading_smooth = 1.0;
        sin_heading_smooth = 0.0;
    }

    function newTrack() {
        System.println("newTrack()");
        lat_view_center=$.track.lat_center;
        lon_view_center=$.track.lon_center;
        cos_lat_view_center = Math.cos(lat_view_center);
        sin_lat_view_center = Math.sin(lat_view_center);
        isTrackCentered = true;
        calcZoomToFitLevel();
        setViewCenter(lat_view_center, lon_view_center);
        Transform.setHeading();
    }

    function setPosition(lat_pos,lon_pos) {
        if(x_pos != null) {
            last_x_pos = x_pos;
            last_y_pos = y_pos;
        }
        if(lat_first_position==null) {
            lat_first_position = lat_pos;
            lon_first_position = lon_pos;
        }
        if($.track==null) {
            lat_view_center = lat_first_position;
            lon_view_center = lon_first_position;
            cos_lat_view_center = Math.cos(lat_view_center);
            sin_lat_view_center = Math.sin(lat_view_center);
        }

        var xy = ll_2_xy(lat_pos, lon_pos);
       
        x_pos = xy[0];
        y_pos = xy[1];
        setViewCenter(lat_pos,lon_pos);
        setHeading();
    }

    function xy_2_screen(x, y) {
        var xr = scaleFactor*(x-x_d);
        var yr = scaleFactor*(y-y_d);
        if(Transform.northHeading || Transform.centerMap || Transform.isTrackCentered) {
            return [xs_center+scaleFactor*(x-x_d), ys_center-scaleFactor*(y-y_d)];
        } else {
            return [xs_center+xr*cos_heading_smooth - yr*sin_heading_smooth,
                    ys_center-xr*sin_heading_smooth - yr*cos_heading_smooth];
        }
    }

    function ll_2_xy(lat, lon) {
        var ll = lon-lon_view_center;
        var cos_lat = Math.cos(lat);
        return [cos_lat*Math.sin(ll), cos_lat_view_center*Math.sin(lat)-sin_lat_view_center*cos_lat*Math.cos(ll)];
    }

    function refScaleFromLevel(level) {
        var levelrange = Math.floor(level/5);
        var sublevel = level % 5;
        var offset = 0;
        switch ( sublevel ) {
           case 1:
               offset= 8;
               break;
           case 2:
               offset= 18;
               break;
           case 3:
               offset= 38;
               break;
           case 4:
               offset= 68;
               break;
        }
        return (Math.pow(10, levelrange)*(12+offset));
    }

    function calcZoomToFitLevel() {
        zoomLevel = 0;
        for(zoomLevel= 0; zoomLevel < 25; zoomLevel+=1 ) {
            refScale = refScaleFromLevel(zoomLevel);
            if(pixelMin/(0.2*pixelWidth)*refScaleFromLevel(zoomLevel)*0.95>$.track.diagonal) {
                break;
            }
        }
        scaleFactor = 0.2*pixelWidth/refScale*EARTH_RADIUS;
        return;
    }

    function setZoomLevel(l) {
        if(l == -1 && zoomLevel > 0) {
            zoomLevel -=1; 
        } else if(l ==-2 && zoomLevel < 24) {
            zoomLevel +=1;
        } else if(l >=0 && l <= 25) {
            zoomLevel = l;
        } else {
            zoomLevel = 5;
        }
                
        refScale = refScaleFromLevel(zoomLevel);
        scaleFactor = 0.2*pixelWidth/refScale*EARTH_RADIUS;
        return zoomLevel;
    }

    function formatScale(scale) {
        if(scale < 1000) {
            return scale.format("%d") + "m";
        }
        var scalekm = scale/1000;
        return scalekm.format("%.1f") + "k";
    }

    function distance(lat1, lon1, lat2, lon2) {
        var dphi = (lat2-lat1);
        var dlambda = (lon2-lon1);
        var a = Math.sin(0.5*dphi)*Math.sin(0.5*dphi) +
            Math.cos(lat1)*Math.cos(lat2) *
            Math.sin(0.5*dlambda)*Math.sin(0.5*dlambda);
        return EARTH_RADIUS*2.0*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
}