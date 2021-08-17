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
    var onPositionCalled = false;

    var findNearestPoint = true;
    var nearestPointIndex = -1;
    var nearestPointDistance = EARTH_RADIUS;
    var nearestPointLambda = 0.0;

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

        nearestPointIndex = -1;
        nearestPointDistance = EARTH_RADIUS;
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
            i = 2 * pos_start_index;
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
        onPositionCalled = true;
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

        // store last xy coordinates for heading and breadcrumb distance for gps positions
        if(xPos != null && onPositionCalled) {
            xLastPos = xPos;
            yLastPos = yPos;
        }

        xPos = _xy[0];
        yPos = _xy[1];

        // find nearest point on track for given gps position
        if(onPositionCalled && findNearestPoint && $.track != null) {
            var d2 = EARTH_RADIUS * EARTH_RADIUS;
            var dxy2 = 0.0;
            var xya = $.track.xyArray;
            var dx = 0.0;
            var dy = 0.0;

            var xs = 0.0;
            var ys = 0.0;
            var ds = 0.0;
            var s = 0.0;
            /*
            for(var i = 0; i < xya.size(); i += 2) {
                dx = xPos - xya[i];
                dy = yPos - xya[i+1];
                dxy2 = dx*dx + dy*dy;
                if(dxy2 < d2) {
                    nearestPointIndex = i;
                    d2 = dxy2;
                }
            }
            if(nearestPointIndex >= 0) {
                nearestPointDistance = EARTH_RADIUS * Math.sqrt(d2);
            }
            */
            for(var i = 0; i < xya.size() - 3; i += 2) {
                xs = xya[i + 2] - xya[i];
                ys = xya[i + 3] - xya[i + 1];
                ds = xs * xs + ys * ys;
                if(ds > 1.0e-12) {
                    s = (xs * (xPos - xya[i]) + ys * (yPos - xya[i + 1])) / ds;
                    if(s < 0.0) {
                        s = 0.0;
                    } else if(s > 1.0) {
                        s = 1.0;
                    }
                } else {
                    s = 0.0;
                }

                dx = xPos - (xya[i] + s * xs);
                dy = yPos - (xya[i+1] + s * ys);
                dxy2 = dx*dx + dy*dy;
                if(dxy2 < d2) {
                    nearestPointIndex = i;
                    nearestPointLambda = s;
                    d2 = dxy2;
                }
            }
            if(nearestPointIndex >= 0) {
                nearestPointDistance = EARTH_RADIUS * Math.sqrt(d2);
            }
            //System.println("nearestPointIndex: " + nearestPointIndex);
            //System.println("nearestPointDistance: " + nearestPointDistance);
            //System.println("nearestPointLambda: " + nearestPointLambda);

        }

        // determine distance and smoothed heading
        if(xLastPos != null) {
            var dx = xPos - xLastPos;
            var dy = yPos - yLastPos;
            var d2 = dx*dx + dy*dy;

            // Dimensional distance between current and last point
            var d = Math.sqrt(d2);
            // distance in m
            positionDistance = EARTH_RADIUS * d;
            //var positionDistance2 = latLongDist(lat,lon,latLast,lonLast);
            // cummulative distance used for breadcrumbs
            cumDistance += positionDistance;

            // mixing factor to smooth heading
            if (positionDistance > 0.01) {
                var sf = positionDistance / (2.0 + positionDistance);
                var dxs = (1.0 - sf) * sin_heading_smooth +  sf/d * dx;
                var dys = (1.0 - sf) * cos_heading_smooth +  sf/d * dy;
                // normalized vector
                sf = 1.0 / Math.sqrt(dxs*dxs + dys*dys);
                sin_heading_smooth = dxs * sf;
                cos_heading_smooth = dys * sf;
             }
             else {
                sin_heading_smooth = 0.0;
                cos_heading_smooth = 1.0;
            }
        }

        // add (x,y) coordinate to breadcrumb array if cumaltive distance is reached
        if( cumDistance >= breadCrumbDist && breadCrumbDist > 0 && onPositionCalled ) {
            putBreadcrumbPosition(xPos, yPos);
            // reset cumulative distance by larger value of either breadcrumd distance or distance from previous position
            cumDistance -= breadCrumbDist > positionDistance ? breadCrumbDist : positionDistance;
            //System.println("putBreadcrumbPosition: ");
            //System.println("cumDistance: " + cumDistance);
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
        onPositionCalled = false;
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
        var x = cos_lat * Math.sin(ll);
        var y = cos_lat_view_center * Math.sin(lat) - sin_lat_view_center * cos_lat * Math.cos(ll);

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
        return EARTH_RADIUS * 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        //return EARTH_RADIUS * 2.0 * Math.asin(Math.sqrt(a));
    }

}
