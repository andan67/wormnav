using Toybox.Activity;
using Toybox.System;
using Toybox.Position;

module Trace {

    const BUFFER_SIZE = 10;

    var x_array = new [BUFFER_SIZE];
    var y_array = new [BUFFER_SIZE];
    var pos_start_index;
    var pos_nelements;
    var cumDistance;
    var breadCrumbDist = 100.0;

    var lat_last_pos;
    var lon_last_pos;

    var time = 0;
    var distance = 0.0;
    var lapTime = 0.0;
    var lapDistance = 0.0;
    var lapInitDistance = 0.0;
    var lapInitTime = 0.0;
    var lapTimeP = 0.0;
    var lapDistanceP = 0.0;
    var lapCounter = 0;
    //var autolapDistance = 1000;
    var autolapDistance = 200.0;
    var lapPace = "";
    var isAutoLapActive = false;
    var lastPositionTime = 0;

    function reset() {
        pos_nelements = 0;
        pos_start_index = 0;
        cumDistance=breadCrumbDist;
        lat_last_pos=null;
        lon_last_pos=null;
    }

    function put_pos(lat,lon) {
        var xy = Transform.ll_2_xy(lat,lon);

        if(pos_nelements<BUFFER_SIZE) {
            x_array[pos_nelements] = xy[0];
            y_array[pos_nelements] = xy[1];
            pos_nelements += 1;
        }
        else {
            x_array[pos_start_index] = xy[0];
            y_array[pos_start_index] = xy[1];
            pos_start_index = (pos_start_index +1) % BUFFER_SIZE;
        }

    }

    function newLatLonPosition(lat_pos,lon_pos) {
        Transform.isTrackCentered = false;
        Transform.setPosition(lat_pos,lon_pos);
        if(lat_last_pos!=null) {
            cumDistance += Transform.distance(lat_last_pos, lon_last_pos, lat_pos, lon_pos);
        }

        lat_last_pos=lat_pos;
        lon_last_pos=lon_pos;

        if((cumDistance >= breadCrumbDist) && (breadCrumbDist > 0)) {
            put_pos(lat_last_pos,lon_last_pos);
            cumDistance -=breadCrumbDist;
        }
    }

    function setAutolapDistance(distance) {
        autolapDistance    = distance;
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
        if(autolapDistance > 0 && $.session!=null && $.session.isRecording() && Activity.getActivityInfo()!=null) {
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
