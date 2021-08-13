using Toybox.Activity;
using Toybox.System;
using Toybox.Position;
using Toybox.Math;

module Track {

    // General constants
    const EARTH_RADIUS = 6371000.0;
    const PI = 3.1415927;
    const PI2_3 = 0.6666667*PI;

    // used for breadcrumbs
    var bxy;
    var breadCrumbNumber = 10;
    var pos_start_index;
    var pos_nelements;
    var cumDistance;
    var cumDistance2;

    var breadCrumbDist = 100.0;

    // center of perspective projection (defined by track if exists)
    var lat_view_center = null;
    var lon_view_center = null;
    var cos_lat_view_center;
    var sin_lat_view_center;

    // used to store current and last position in (x,y) coordinates
    var xPos;
    var yPos;
    var xLastPos;
    var yLastPos;
    var latLast;
    var lonLast;

    var isTrackCentered = true;

    // used for calculation of heading
    var northHeading = true;
    var centerMap = false;
    var cos_heading_smooth = 1.0;
    var sin_heading_smooth = 0.0;

    // used for autolap
    var lapTime = 0.0;
    var lapDistance = 0.0;
    var lapInitDistance = 0.0;
    var lapInitTime = 0.0;
    var lapTimeP = 0.0;
    var lapDistanceP = 0.0;
    var lapCounter = 0;
    var autolapDistance = 1000.0;
    var lapPace = "";
    var isAutoLapActive = false;

    var positionTime = 0;
    var lastPositionTime = 0;
    var positionDistance = 0.0;


    function reset() {
        bxy = new [2 * breadCrumbNumber];
        pos_nelements = 0;
        pos_start_index = 0;
        cumDistance = breadCrumbDist;
        xLastPos=null;
        yLastPos=null;
        positionTime = 0;
        lastPositionTime = 0;
        positionDistance = 0.0;
    }

    function setBreadCrumbNumber(number) {
        breadCrumbNumber = number;
        reset();
    }

    function putBreadcrumbLastPosition() {
        if(xLastPos != null) {
            putBreadcrumbPosition(xLastPos, yLastPos);
            cumDistance = 0.0;
        }
    }


    function putBreadcrumbPosition(x ,y) {
        var i = 2*pos_nelements;
        if(pos_nelements < breadCrumbNumber) {
            pos_nelements += 1;
        }
        else {
            i = 2*pos_start_index;
            pos_start_index = (pos_start_index +1) % breadCrumbNumber;
        }
        bxy[i] = x;
        bxy[i + 1] = y;
    }

    function getOrientation() {
        if(northHeading) {
            return centerMap ? 2 : 1;
        } else {
            return 0;
        }
    }

    function onPosition(info) {
        // (lat,lon) coordinates from position are transformed to projected (x,y) coordinates.
        // Hereafter, only (x,y) coordinates should be used
        //var lat = info.position.toRadians()[0].toFloat();
        //var lon = info.position.toRadians()[1].toFloat();
        var lat = info.position.toRadians()[0];
        var lon = info.position.toRadians()[1];
        isTrackCentered = false;
        setPosition(lat, lon);
    }

    function setPosition(lat, lon) {
         // Use first position as view center if not defined by track
        if(lat_view_center == null) {
            // no view center defined, so use current position coordinates
            lat_view_center = lat.toFloat();
            lon_view_center = lon.toFloat();
            cos_lat_view_center = Math.cos(lat_view_center);
            sin_lat_view_center = Math.sin(lat_view_center);
        }

        // (lat,lon) coordinates from position are transformed to projected (x,y) coordinates.
        // Hereafter, only (x,y) coordinates should be used
        var _xy = latLon2xy(lat, lon);

        // store last xy coordinates for heading
        if(xPos != null) {
            xLastPos = xPos;
            yLastPos = yPos;
            // last position exists, so calculated distance
            // positionDistance = Math.sqrt( xyDist2(xLastPos, yLastPos, _xy[0], _xy[1]) );
            // cumDistance += positionDistance;
        }

        xPos = _xy[0];
        yPos = _xy[1];

        // determine distance and smoothed heading
        if(xLastPos != null) {
            var dx = xPos - xLastPos;
            var dy = yPos - yLastPos;
            var d2 = dx*dx + dy*dy;

            // Dimensional distance between current and last point
            var d = Math.sqrt(d2);
            positionDistance = EARTH_RADIUS * d;
            var positionDistance2 = latLongDist(lat,lon,latLast,lonLast);
            // cummulative distance used for breadcrumbs
            cumDistance += positionDistance;
            System.println("xLastPos : " + xLastPos.format("%.10f"));
            System.println("xPos : " + xPos.format("%.10f"));
            System.println("yLastPos : " + yLastPos.format("%.10f"));
            System.println("yPos : " + yPos.format("%.10f"));
            System.println("latLast : " + latLast.format("%.10f"));
            System.println("lonLast : " + lonLast.format("%.10f"));
            System.println("lat : " + lat.format("%.10f"));
            System.println("lon : " + lon.format("%.10f"));

            System.println("positionDistance : " + positionDistance);
            System.println("positionDistance2: " + positionDistance2);
            System.println("positionDistanced: " + (positionDistance2 - positionDistance));
            System.println("cumDistance: " + cumDistance);

            // mixing factor to smooth heading
            if (d > 1.0e-10) {
                var sf = positionDistance / (2.0 + positionDistance);
                var dxs = (1.0 - sf) * sin_heading_smooth +  sf/d * dx;
                var dys = (1.0 - sf) * cos_heading_smooth +  sf/d * dy;
                d2 = Math.sqrt(dxs*dxs + dys*dys);
                sin_heading_smooth = dxs/d2;
                cos_heading_smooth = dys/d2;
             }
             else {
                sin_heading_smooth = 0.0;
                cos_heading_smooth = 1.0;
            }
        }

        // add (x,y) coordinate to breadcrumb array if cumaltive distance is reached
        if( (cumDistance >= breadCrumbDist) && (breadCrumbDist > 0) ) {
            putBreadcrumbPosition(xPos, yPos);
            cumDistance -= breadCrumbDist;
        }

        latLast = lat;
        lonLast = lon;

    }

    function resetHeading() {
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
        reset();
        resetHeading();
        setPosition(lat_view_center, lon_view_center);
    }

    function setAutolapDistance(distance) {
        autolapDistance = distance;
        if(autolapDistance == 0) {
            // reset autolap
            lapTime = 0.0;
            lapDistance = 0.0;
            lapInitTime = 0.0;
            lapInitDistance = 0.0;
            lapTimeP = 0.0;
            lapDistanceP = 0.0;
            lapCounter = 0;
        }
        isAutolap(true);
    }

    function isAutolap(setNewLap) {
        var isLap = false;
        if(autolapDistance > 0 && $.session != null && $.session.isRecording() && Activity.getActivityInfo() != null) {
            isAutoLapActive = true;
            var distance = Activity.getActivityInfo().elapsedDistance;
            var time = Activity.getActivityInfo().timerTime;
            if ( time != null && time > 0 && distance != null  && distance > 0) {
                lapTime = time - lapInitTime;
                lapDistance = distance - lapInitDistance;
                if(lapDistance > autolapDistance || setNewLap ) {
                    // adjusted lap time for lap
                    if(!setNewLap) {
                        lapTime *= autolapDistance/lapDistance;
                        lapDistance = autolapDistance;
                    }
                    lapTimeP = lapTime;
                    lapDistanceP = lapDistance;
                    lapInitTime += lapTime;
                    lapInitDistance += lapDistance;
                    lapCounter++;
                    isLap = true;
                    session.addLap();
                }
            }
        } else {
            isAutoLapActive = false;
        }
        return isLap;
    }

    // Returns normalized (x,y) coordinates from perspective projection of point on sphere given by (lat,lon) coordinates
    // onto the plane that touches the sphere at the point (lat_center, lon_center).
    // This center (touch point) is also the origin for the transformed (x,y) coordinates.
    // Dimensional coordinates can be derived by multiplication with standard earth radius 6371km.
    function latLon2xy(lat, lon) {
        var ll = lon - lon_view_center;
        var cos_lat = Math.cos(lat);
        System.println("lon_view_center: " + lon_view_center.format("%.10f"));
        System.println("lat_view_center: " + lat_view_center.format("%.10f"));
        System.println("cos_lat_view_center: " + cos_lat_view_center.format("%.10f"));
        System.println("sin_lat_view_center: " + sin_lat_view_center.format("%.10f"));
        System.println("lat: " + lat.format("%.10f"));
        System.println("lon: " + lon.format("%.10f"));
        System.println("cos_lat: " + cos_lat.format("%.10f"));
        System.println("ll: " + ll.format("%.10f"));
        System.println("Math.cos(ll): " + Math.cos(ll).format("%.10f"));
        System.println("Math.sin(ll): " + Math.sin(ll).format("%.10f"));
        var x = cos_lat * Math.sin(ll);
        var y = cos_lat_view_center * Math.sin(lat) - sin_lat_view_center * cos_lat * Math.cos(ll);
        System.println("x: " + x.format("%.10f"));
        System.println("y: " + y.format("%.10f"));

        //return [cos_lat * Math.sin(ll), cos_lat_view_center * Math.sin(lat) - sin_lat_view_center * cos_lat * Math.cos(ll)];
        return [x.toFloat(), y.toFloat()];
    }

    function xyDist2(x1, y1, x2, y2) {
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
    }

    function latLongDist(lat1, lon1, lat2, lon2) {
        var dphi = (lat2-lat1);
        var dlambda = (lon2-lon1);
        var a = Math.sin(0.5*dphi)*Math.sin(0.5*dphi) +
            Math.cos(lat1)*Math.cos(lat2) *
            Math.sin(0.5*dlambda)*Math.sin(0.5*dlambda);
        //return EARTH_RADIUS * 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return EARTH_RADIUS * 2.0 * Math.asin(Math.sqrt(a));
    }

}
