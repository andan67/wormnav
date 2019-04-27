using Toybox.Activity;

module Trace {

    const BUFFER_SIZE = 10;

    var x_array = new [BUFFER_SIZE];
    var y_array = new [BUFFER_SIZE];
    var pos_start_index;
    var pos_nelements;
    var cumDistance;
    var breadCrumbDist = 100;

    var lat_last_pos;
    var lon_last_pos;

    var lapTime = 0;
    var elapsedlapTimeP = 0;
    var elapsedLapDistanceP = 0.0;
    var elapsedlapTime = 0;
    var elapsedLapDistance = 0.0;
    var lapInitDistance = 0.0;
    var lapInitTime = 0;
    var lapCounter = 0;
    var autolapDistance = 1000;
    var lapPace = "";

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

    function new_pos(lat_pos,lon_pos) {
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

    function isAutolap() {
        var isLap = false;
        if(autolapDistance > 0 && $.session!=null && session.isRecording() && Activity.getActivityInfo()!=null) {

            var elapsedDistance = Activity.getActivityInfo().elapsedDistance;
            var elapsedTime = Activity.getActivityInfo().elapsedTime;
            if ( elapsedTime != null && elapsedTime > 0 && elapsedDistance != null  && elapsedDistance > 0) {
                elapsedlapTime = elapsedTime - lapInitTime;
                elapsedLapDistance = elapsedDistance - lapInitDistance;
                //System.println("AutoLap on Timer():" + elapsedDistance + "|" + elapsedLapDistance + "|" + lapInitDistance + "|" + autolapDistance);

                var lapVel = 0;
                if ( elapsedlapTime > 0 && elapsedLapDistance > 0 ) {
                    lapVel = elapsedLapDistance.toDouble()/(elapsedlapTime.toDouble()/1000);
                }

                if(elapsedLapDistance > autolapDistance) {
                    lapTime = elapsedlapTimeP + (autolapDistance - elapsedLapDistanceP)/(elapsedDistance - elapsedLapDistanceP)*(elapsedTime - elapsedlapTimeP);
                    lapInitTime = lapInitTime + lapTime;
                    lapInitDistance = lapInitDistance + autolapDistance;
                    lapCounter++;
                    isLap = true;
                }
                elapsedlapTimeP = elapsedlapTime;
                elapsedLapDistanceP = elapsedLapDistance;
            }
        }
        return isLap;
    }
}
