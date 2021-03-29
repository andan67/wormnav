using Toybox.Math;
using Toybox.Activity;
using Toybox.System as Sys;
using Toybox.Lang as Lang;
using Toybox.StringUtil;
using Toybox.Sensor;
using Toybox.UserProfile;

module Data {

    const AVG_CHAR = StringUtil.utf8ArrayToString([0xC3,0x98]);

    const dataFieldMenuLabels = [
        "Timer",                //  0
        "Dist.",                //  1
        "Pace",                 //  2
        "Speed",                //  3
        "Avg\nPace",            //  4
        "Avg\nSpeed",           //  5
        "Heart\nRate",          //  6
        "% max\nHeart\nRate",   //  7
        "Avg\nHeart\nRate",     //  8
        "Lap\nTimer",           //  9
        "Lap\nDist.",           // 10
        "Lap\nPace",            // 11
        "Lap\nSpeed",           // 12
        "Last\nLap\nPace",      // 13
        "Last\nLap\nSpeed",     // 14
        "Laps",                 // 15
        "Alt",                  // 16
        "Clock\nTime",          // 17
        "Bat."];                // 18

    const dataFieldLabels = [
        "Timer",                    //  0   TIMER
        "Distance",                 //  1   DisTANCE
        "Pace",                     //  2   PACE
        "Speed",                    //  3   SPEED
        AVG_CHAR + " Pace",         //  4   AVERAGE_PACE
        AVG_CHAR + " Speed",        //  5   AVERAGE_SPEED  
        "Heart Rate",               //  6   CURRENT_HEART_RATE
         "% max HR",                //  7   PERCENT_HEART_RATE
        AVG_CHAR + "Heart Rate",    //  8   AVERAGE_HEART_RATE
        "Lap Timer",                //  9   LAP_TIMER
        "Lap Dist.",                // 10   LAP_DISTANCE
        "Lap Pace",                 // 11   LAP_PACE
        "Lap Speed",                // 12   LAP_SPEED
        "LL Pace",                  // 13   LAST_LAP_PACE
        "LL Speed",                 // 14   LAST_LAP_SPEED
        "Laps",                     // 15   LAP
        "Altitude",                 // 16   ALTITUDE
        "Clock Time",               // 17   CLOCK_TIME
        "Battery"];                 // 18   BATTERY


    const dataScreensDefault = [[0,1,4,6],[9,10,11,15],[12,5,14,10]];

    var dataScreens = dataScreensDefault;
    var activeDataScreens = [];
    var maxHeartRate = UserProfile.getHeartRateZones( UserProfile.getCurrentSport())[5];

    function updateMaxHeartRate() {
        maxHeartRate = UserProfile.getHeartRateZones( UserProfile.getCurrentSport())[5];
    }

    function setDataScreens(pDataScreens) {
        dataScreens = pDataScreens;
        determineActiveDataScreens();
    }

    function getDataScreens() {
        return dataScreens;
    }

    function setDataScreen(i, dataScreen) {
        if(i < dataScreens.size()) {
            dataScreens[i] = dataScreen;
            determineActiveDataScreens();
        }
    }

    function determineActiveDataScreens() {
        activeDataScreens = [];
        for(var i=0; i < dataScreens.size(); i+=1) {
            if(dataScreens[i]!= null && dataScreens[i].size() > 0) {
                activeDataScreens.add(dataScreens[i]);
            }
        }
        Sys.println("determineActiveDataScreens: " + activeDataScreens);
    }

    function timer() {
        var data=Activity.getActivityInfo().timerTime;
        return data!=null? Data.msToTime(data) : "--";
    }

    function distance() {
        var data=Activity.getActivityInfo().elapsedDistance;
        return data!=null? (0.001*data+0.0001).format("%.2f") : "--";
    }

    function getDataFieldLabelValue(i) {
        var dataValue = null;
        var data = null;
        switch(i) {
            case 0: // TIMER
                dataValue = timer();
                break;
            case 1: // DISTANCE
                dataValue = distance();
                break;
            case 2: // PACE
                data = Activity.getActivityInfo().currentSpeed;
                dataValue = data!=null? Data.convertSpeedToPace(data) : null;
                break;
            case 3: // SPEED
                data = Activity.getActivityInfo().currentSpeed;
                dataValue = data!=null ? (3.6*data).format("%.2f") : null;
                break;
            case 4: // AVERAGE_PACE
                data = Activity.getActivityInfo().averageSpeed;
                dataValue = data!=null?  Data.convertSpeedToPace(data) : null;
                break;
            case 5: // AVERAGE_SPEED
                data = Activity.getActivityInfo().averageSpeed;
                dataValue = data!=null?  (3.6*data).format("%.2f") : null;
                break;
            case 6: // CURRENT_HEART_RATE
                dataValue = Activity.getActivityInfo().currentHeartRate;
                break;
            case 7: // PERCENT_HEART_RATE
                data = Activity.getActivityInfo().currentHeartRate;
                dataValue = data!=null ? (100.0*data/maxHeartRate).format("%.0f") + "%" : null;
                break;
            case 8: // AVERAGE_HEART_RATE
                dataValue = Activity.getActivityInfo().averageHeartRate;
                break;
            case 9: // LAP_TIMER
                dataValue = Trace.autolapDistance > 0 ? Data.msToTime(Trace.lapTime.toLong()) : null;
                break;
            case 10: // LAP_DISTANCE
                dataValue = Trace.autolapDistance > 0 ? (0.001*Data.Trace.lapDistance).format("%.2f") : null;
                break;
            case 11: // LAP_PACE
                 dataValue = Trace.autolapDistance > 0 ? Trace.lapCounter : null;
                break;
            case 12: // LAP_SPEED
                dataValue = (Trace.autolapDistance > 0  && Trace.lapTime > 0) ?
                    (3600*Trace.lapDistance/Trace.lapTime).format("%.2f") : null;
                break;
            case 13: // LAST_LAP_PACE
                dataValue = (Trace.autolapDistance > 0 && Trace.lapTimeP > 0) ?
                    Data.convertSpeedToPace(1000*Trace.lapDistanceP/Trace.lapTimeP) : null;
                break;
            case 14: // LAST_LAP_SPEED
                dataValue = (Trace.autolapDistance > 0  && Trace.lapTime > 0) ?
                    (3600*Trace.lapDistance/Trace.lapTime).format("%.2f") : null;
                break;
            case 15: // LAP
                dataValue = Trace.autolapDistance > 0 ? Trace.lapCounter : null;
                break;
            case 16: // ALTITUDE
                data = Activity.getActivityInfo().altitude;
                dataValue = data!=null ? data.format("%.0f") : null;
                break;
            case 17: // CLOCK_TIME
                data = Sys.getClockTime();
                dataValue =  data!=null ?
                    data.hour.format("%02d") + ":" +
                    data.min.format("%02d") + ":" +
                    data.sec.format("%02d"): null;
                break;
            case 18: // BATTERY
                data = Sys.getSystemStats().battery;
                dataValue = data!=null ? data.format("%.1f") + "%" : null;
                break;
            default:
                break;
        }
        return [dataFieldLabels[i], dataValue];
    }

    function msToTime(ms) {
        var seconds = (ms / 1000) % 60;
        var minutes = (ms / 60000) % 60;
        var hours = ms / 3600000;

        return Lang.format("$1$:$2$:$3$", [hours, minutes.format("%02d"), seconds.format("%02d")]);
    }

    function msToTimeWithDecimals(ms) {
        var decimals = (ms % 1000) / 10;
        var seconds = (ms / 1000) % 60;
        var minutes = (ms / 60000) % 60;
        var hours = ms / 3600000;
        var string = "";

        if (hours > 0){
            string = Lang.format("$1$:$2$:$3$", [hours, minutes.format("%02d"), seconds.format("%02d")]);
        }
        else{
            string = Lang.format("$1$:$2$.$3$", [minutes.format("%02d"), seconds.format("%02d"), decimals.format("%02d")]);
        }

        return string;
    }

    function convertSpeedToPace(speed) {
        var result_min;
        var result_sec;
        var result_per;
        var conversionvalue;
        var settings = Sys.getDeviceSettings();

        result_min = 0;
        result_sec = 0;
        if( settings.paceUnits == Sys.UNIT_METRIC ) {
            result_per = "/km";
            conversionvalue = 1000.0d;
        } else {
            result_per = "/mi";
            conversionvalue = 1609.34d;
        }

        if( speed != null && speed > 0 ) {
            var secpermetre = 1.0d / speed; // speed = m/s
            result_sec = secpermetre * conversionvalue;
            result_min = result_sec / 60;
            result_min = result_min.format("%d").toNumber();
            result_sec = result_sec - ( result_min * 60 );  // Remove the exact minutes, should leave remainder seconds
        }

        //return Lang.format("$1$:$2$$3$", [result_min, result_sec.format("%02d"), result_per]);
        return Lang.format("$1$:$2$", [result_min, result_sec.format("%02d")]);
    }

     function convertDistance(metres) {
        var result;

        if( metres == null ) {
            result = 0;
        } else {
            var settings = Sys.getDeviceSettings();
            if( settings.distanceUnits == Sys.UNIT_METRIC ) {
                result = metres / 1000.0;
            } else {
                result = metres / 1609.34;
            }

        }

        return Lang.format("$1$", [result.format("%.2f")]);
    }

    function convertToMeters(distance){
        var meters;

        if (distance == null){
            meters = 0;
        }
        else{
            var settings = Sys.getDeviceSettings();
            if( settings.distanceUnits == Sys.UNIT_METRIC ) {
                meters = distance * 1000.0;
            } else {
                meters = distance * 1609.34;
            }
        }
        return meters;
    }

    // Print pace as min:sec
    function printPace(pace) {
        var paceStr;

        if( pace != null && ( pace instanceof Toybox.Lang.Number || pace instanceof Toybox.Lang.Float ) ) {
            paceStr=pace.format("%.2f");
            return paceStr.substring(0,paceStr.find(".")) + ":" + paceStr.substring(paceStr.find(".")+1,paceStr.find(".")+3);
        } else {
            return "--";
        }
    }

     // Print pace as min:sec
    function printTime(timeInMillies) {
       var seconds = Math.floor((timeInMillies / 1000) % 60) ;
       var minutes = Math.floor(((timeInMillies / (1000*60)) % 60));
       var hours = Math.floor(((timeInMillies / (1000*60*60)) % 24));
       return Lang.format("$1$:$2$:$3$", [hours, minutes.format("%02d"), seconds.format("%02d")]);
    }

    // Convert from speed to pace (from m/s to min/km)
    function speedToPace(speed) {
        var seconds;

        if( speed==0.0 ) {
            return 0.0;
        }

        // Change from speed (m/s) to pace (min/km or min/mi)
        // Check device settings to get unit settings
        if( System.getDeviceSettings().paceUnits==System.UNIT_STATUTE ) {
            speed=1/speed*1609.344/60;
        } else {
            speed=1/speed*1000/60;
        }

        // Change decimals from base 100 to base 60 (a pace of 5.5 should be 5 minutes and 30 seconds)
        seconds=(speed-speed.toNumber())*60/100;
        if( seconds >= 0.595 ) {
            seconds=0;
            speed++;
        }

        return speed.toNumber()+seconds;
    }

     function min(x,y) {
        if(x < y) {
            return x;
        } else {
            return y;
        }
    }
}