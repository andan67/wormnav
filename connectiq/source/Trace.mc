using Toybox.Activity;
using Toybox.System;
using Toybox.Position;

module Trace {

    var xy;
    var breadCrumbNumber = 10;
    var pos_start_index;
    var pos_nelements;
    var cumDistance;
    var breadCrumbDist = 100.0;

    var lat_last_pos;
    var lon_last_pos;

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

    function getAutolapDistance() {
        return autolapDistance;
    }

    function getBreadCrumbNumber() {
        return breadCrumbNumber;
    }

    function getBreadCrumbDist() {
        return breadCrumbDist;
    }


    function reset() {
        xy = new [2 * breadCrumbNumber];
        pos_nelements = 0;
        pos_start_index = 0;
        cumDistance = breadCrumbDist;
        lat_last_pos=null;
        lon_last_pos=null;
        positionTime = 0;
        lastPositionTime = 0;
        positionDistance = 0.0;
    }

    function setBreadCrumbNumber(number) {
        breadCrumbNumber = number;
        reset();
    }

    function putBreadcrumbLastPosition() {
        if(lat_last_pos != null) {
            putBreadcrumbPosition(lat_last_pos, lon_last_pos);
            cumDistance = 0.0;
        }
    }


    function putBreadcrumbPosition(lat,lon) {
        var _xy = Transform.ll_2_xy(lat,lon);

        if(pos_nelements < breadCrumbNumber) {
            xy[2*pos_nelements] = _xy[0];
            xy[2*pos_nelements + 1] = _xy[1];
            pos_nelements += 1;
        }
        else {
            xy[2*pos_start_index] = _xy[0];
            xy[2*pos_start_index + 1] = _xy[1];
            pos_start_index = (pos_start_index +1) % breadCrumbNumber;
        }
    }

    function newLatLonPosition(lat_pos,lon_pos) {
        Transform.isTrackCentered = false;
        Transform.setPosition(lat_pos,lon_pos);
        if(lat_last_pos != null) {
            positionDistance = Transform.distance(lat_last_pos, lon_last_pos, lat_pos, lon_pos);
            cumDistance += positionDistance;
        }

        lat_last_pos=lat_pos;
        lon_last_pos=lon_pos;

        if((cumDistance >= breadCrumbDist) && (breadCrumbDist > 0)) {
            putBreadcrumbPosition(lat_last_pos,lon_last_pos);
            cumDistance -= breadCrumbDist;
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
//            System.println("==========================");
//            System.println("time=" + time);
//            System.println("distance=" +distance);
//            System.println("position=" + Position.getInfo().position.toGeoString(Position.GEO_DMS));
//            System.println("setNewLap=" + setNewLap);
//            System.println("isLap=" + isLap);
//            System.println("lapCounter=" + lapCounter);
//            System.println("lapTime=" +lapTime);
//            System.println("lapDistance=" + lapDistance);
//            System.println("lapInitTime=" +lapInitTime);
//            System.println("lapInitDistance=" + lapInitDistance);
//            System.println("lapTimeP=" +lapTimeP);
//            System.println("lapDistanceP=" + lapDistanceP);
        } else {
            isAutoLapActive = false;
        }
        return isLap;
    }
}
