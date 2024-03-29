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
    var breadCrumbDist = 100.0;
    var pos_start_index;
    var pos_nelements;
    var cumDistance;

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
    var isAutoLapActive = false;

    // track data
    var trackStats;
    var xArray;
    var yArray;
    var eleArray;

    var latCenter;
    var lonCenter;
    var diagonal;
    var name;
    var length;
    var nPoints;
    var xyLength;
    var xyLengthLabel;
    var eleMin;
    var eleMinDist;
    var eleMinIdx;
    var eleMax;
    var eleMaxDist;
    var eleMaxIdx;
    var eleTotAscent;
    var eleTotDescent;
    var hasTrackData = false;
    var hasElevationData = false;

    // used for elevation plot
    var eleAct = null;
    var eleMinAct = null;
    var eleMaxAct = null;
    var eleMinTrack = null;
    var eleMaxTrack = null;
    var eleTrack = null;
    var eleTotAscentAct = null;

    var positionDistance = 0.0;
    var onPositionCalled = false;

    function resetPosition() {
        xLastPos = null;
        yLastPos = null;
        positionDistance = 0.0;
        onPositionCalled = false;

        cos_heading_smooth = 1.0;
        sin_heading_smooth = 0.0;
    }

    function resetBreadCrumbs(number) {
        if(number != null) {
            breadCrumbNumber = number;
        }
        bxy = new [2 * breadCrumbNumber];
        pos_nelements = 0;
        pos_start_index = 0;
        cumDistance = breadCrumbDist;
    }

    function putBreadcrumbLastPosition() {
        if(xLastPos != null) {
            putBreadcrumbPosition(xLastPos, yLastPos);
            cumDistance = 0.0;
        }
    }

    function putBreadcrumbPosition(x ,y) {
        var i = 2 * pos_nelements;
        if(pos_nelements < breadCrumbNumber) {
            pos_nelements += 1;
        }
        else {
            i = 2 * pos_start_index;
            pos_start_index = (pos_start_index + 1) % breadCrumbNumber;
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
        onPositionCalled = true;
        setPosition(info.position.toRadians()[0], info.position.toRadians()[1]);
        if($.trackElevationPlot && hasElevationData) {
            //ToDo: It seems there is a problem with getting correct altitude values in the simulator
            // Thus simulate good enough values from the track elevation data
            // get elevevation from activity info as this should be the better value from either gps or barometer
            // enable for real device
            eleTotAscentAct = Activity.getActivityInfo() != null ? Activity.getActivityInfo().totalAscent : null;
            var eleAct = Activity.getActivityInfo() != null ? Activity.getActivityInfo().altitude : null;

            // enable for simulation
            /*
            eleTotAscentAct = 47.0;
            var eleAct = eleTrack == null ? 0.5 * (eleMin + eleMax) : eleTrack + (Math.rand() % 20 -10);
            */

            setElevation(eleAct);
        }
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
        var xy = latLon2xy(lat, lon);

        // store last xy coordinates for heading and breadcrumb distance for gps positions
        if(xPos != null && onPositionCalled) {
            xLastPos = xPos;
            yLastPos = yPos;
        }

        xPos = xy[0];
        yPos = xy[1];

        // determine distance and smoothed heading
        if(xLastPos != null) {
            var dx = xPos - xLastPos;
            var dy = yPos - yLastPos;

            // Dimensional distance between current and last point
            var d = Math.sqrt(dx * dx + dy * dy);
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
                sf = 1.0 / Math.sqrt(dxs * dxs + dys * dys);
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
        }

        latLast = lat;
        lonLast = lon;

    }

    function deleteTrack() {
        trackStats = null;
        xArray = null;
        yArray = null;
        eleArray = null;
        latCenter = null;
        lonCenter = null;
        diagonal = null;
        name = null;
        length = null;
        nPoints = null;
        xyLength = null;
        eleMin = null;
        eleMinDist = null;
        eleMinIdx = null;
        eleMax = null;
        eleMaxDist = null;
        eleMaxIdx = null;
        eleTotAscent = null;
        eleTotDescent = null;

        hasTrackData = false;
        hasElevationData = false;

        onPositionCalled = false;
        resetPosition();
        resetBreadCrumbs(null);
    }

    function newTrack(_stats, _xArray, _yArray, _eleArray) {
        trackStats = _stats;

        name = trackStats[0];
        length = trackStats[1];
        nPoints = trackStats[2];

        latCenter = trackStats[7];
        lonCenter = trackStats[8];
        diagonal = trackStats[9];

        xArray = _xArray;
        yArray = _yArray;
        hasTrackData = true;

        if(_eleArray != null) {
            hasElevationData = true;
            eleArray = _eleArray;

            // message contains elevation data
            xyLength = trackStats[10];
            xyLengthLabel = Track.formatLength(xyLength);
            eleMin = trackStats[11];
            eleMinDist = trackStats[12];
            eleMinIdx = trackStats[13];
            eleMax = trackStats[14];
            eleMaxDist = trackStats[15];
            eleMaxIdx = trackStats[16];
            eleTotAscent = trackStats[17];
            eleTotDescent = trackStats[18];
        }

        lat_view_center = latCenter;
        lon_view_center = lonCenter;

        cos_lat_view_center = Math.cos(lat_view_center);
        sin_lat_view_center = Math.sin(lat_view_center);

        if(eleMinTrack == null) {
            eleMinTrack = eleMin;
            eleMaxTrack = eleMax;
        }
        onPositionCalled = false;
        resetPosition();
        resetBreadCrumbs(null);
        setPosition(lat_view_center, lon_view_center);
    }

    function setElevation(e) {
        if (e != null) {
            eleAct = e;
            if(eleMaxAct == null || eleAct > eleMaxAct) {
                eleMaxAct = eleAct;
                if(eleAct > eleMax) {
                    eleMaxTrack = eleAct;
                } else {
                    eleMaxTrack = eleMax;
                }
            }
            if(eleMinAct == null || eleAct < eleMinAct) {
                eleMinAct = eleAct;
                if(eleAct < eleMin) {
                    eleMinTrack = eleAct;
                } else {
                    eleMinTrack = eleMin;
                }
            }
        }
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
    // onto the plane that touches the sphere at the point (latCenter, lonCenter).
    // This center (touch point) is also the origin for the transformed (x,y) coordinates.
    // Dimensional coordinates can be derived by multiplication with standard earth radius 6371km.
    function latLon2xy(lat, lon) {
        var ll = lon - lon_view_center;
        var cosLat = Math.cos(lat);
        var x = cosLat * Math.sin(ll);
        var y = cos_lat_view_center * Math.sin(lat) - sin_lat_view_center * cosLat * Math.cos(ll);

        //return [cos_lat * Math.sin(ll), cos_lat_view_center * Math.sin(lat) - sin_lat_view_center * cos_lat * Math.cos(ll)];
        return [x.toFloat(), y.toFloat()];
    }

    function formatLength(ln) {
        var l = 0.001 * EARTH_RADIUS * ln;
        return l.format("%.2f") + " km";
    }

}
