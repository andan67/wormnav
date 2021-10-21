using Toybox.Math;
using Toybox.Activity;
using Toybox.System as Sys;
using Toybox.Lang as Lang;
using Toybox.StringUtil;
using Toybox.Sensor;
using Toybox.UserProfile;
using Toybox.WatchUi;
using Toybox.Application;
using Track;

module Data {
    const dataFieldMenuLabels =  Application.getApp().split(WatchUi.loadResource(Rez.Strings.dm_labels),'|');
    const dataFieldLabels = Application.getApp().split(WatchUi.loadResource(Rez.Strings.df_labels),'|');

    const dataScreensDefault = [4,0,1,4,6,4,9,10,11,15,4,12,5,14,10,2,0,1,4,6];

    var dataScreens = [];
    var activeDataScreens = [];

    var maxHeartRate = null;

    function setMaxHeartRate() {
        maxHeartRate = UserProfile.getHeartRateZones( UserProfile.getCurrentSport())[5];
    }

    function getField(_screen, _idx) {
        return dataScreens[5*_screen + _idx];
    }

    function setField(_screen, _idx, _field) {
        dataScreens[5*_screen + _idx] = _field;
        setActiveDataScreens();
    }

    function setDataScreens(_dataScreens) {
        dataScreens = [];
        // check for old format
        if(_dataScreens[0] instanceof Lang.Array) {
            // old format
            for(var i = 0; i < _dataScreens.size(); i++) {
                dataScreens.add(_dataScreens[i].size());
                dataScreens.addAll(_dataScreens[i]);
                for (var j = 0; j < 4 - _dataScreens[i].size(); j++) {
                    // default
                    dataScreens.add(0);
                }
            }
            // for track screen
            for(var i = 15; i < 20; i++) {
                dataScreens.add(dataScreensDefault[i]);
            }
        } else {
            for(var i = 0; i < _dataScreens.size(); i++) {
                dataScreens.add(_dataScreens[i]);
            }
        }
        setActiveDataScreens();
    }

    function setActiveDataScreens() {
       activeDataScreens = [];
        for(var i = 0; i < 15; i += 5) {
            if(dataScreens[i] > 0) {
                activeDataScreens.add(dataScreens.slice(i + 1, i + 1 + dataScreens[i]));
            }
        }
    }

    function getDataFieldLabelValue(i) {
        var dataValue = null;
        var data = null;
        switch(i) {
            case 0: // TIMER
                data = Activity.getActivityInfo().timerTime;
                dataValue = data != null? Data.msToTime(data, false) : "--";
                break;
            case 1: // DISTANCE
                data = Activity.getActivityInfo().elapsedDistance;
                dataValue = data != null? (0.001*data+0.0001).format("%.2f") : "--";
                break;
            case 2: // PACE
                data = Activity.getActivityInfo().currentSpeed;
                dataValue = data != null? Data.convertSpeedToPace(data) : null;
                break;
            case 3: // SPEED
                data = Activity.getActivityInfo().currentSpeed;
                dataValue = data != null ? (3.6*data).format("%.2f") : null;
                break;
            case 4: // AVERAGE_PACE
                data = Activity.getActivityInfo().averageSpeed;
                dataValue = data != null?  Data.convertSpeedToPace(data) : null;
                break;
            case 5: // AVERAGE_SPEED
                data = Activity.getActivityInfo().averageSpeed;
                dataValue = data != null?  (3.6*data).format("%.2f") : null;
                break;
            case 6: // CURRENT_HEART_RATE
                dataValue = Activity.getActivityInfo().currentHeartRate;
                break;
            case 7: // PERCENT_HEART_RATE
                data = Activity.getActivityInfo().currentHeartRate;
                dataValue = data != null ? (100.0*data/maxHeartRate).format("%.0f") + "%" : null;
                break;
            case 8: // AVERAGE_HEART_RATE
                dataValue = Activity.getActivityInfo().averageHeartRate;
                break;
            case 9: // LAP_TIMER
                dataValue = Track.autolapDistance > 0 ? Data.msToTime(Track.lapTime.toLong(), false) : null;
                break;
            case 10: // LAP_DISTANCE
                dataValue = Track.autolapDistance > 0 ? (0.001*Data.Track.lapDistance).format("%.2f") : null;
                break;
            case 11: // LAP_PACE
                dataValue = (Track.autolapDistance > 0 && Track.lapTime > 0) ?
                    Data.convertSpeedToPace(1000*Track.lapDistance/Track.lapTime) : null;
                break;
            case 12: // LAP_SPEED
                dataValue = (Track.autolapDistance > 0  && Track.lapTime > 0) ?
                    (3600*Track.lapDistance/Track.lapTime).format("%.2f") : null;
                break;
            case 13: // LAST_LAP_PACE
                dataValue = (Track.autolapDistance > 0 && Track.lapTimeP > 0) ?
                    Data.convertSpeedToPace(1000*Track.lapDistanceP/Track.lapTimeP) : null;
                break;
            case 14: // LAST_LAP_SPEED
                dataValue = (Track.autolapDistance > 0  && Track.lapTime > 0) ?
                    (3600*Track.lapDistance/Track.lapTime).format("%.2f") : null;
                break;
            case 15: // LAP
                dataValue = Track.autolapDistance > 0 ? Track.lapCounter : null;
                break;
            case 16: // ALTITUDE
                data = Activity.getActivityInfo().altitude;
                dataValue = data != null ? data.format("%.0f") : null;
                break;
            case 17: // TOTAL_ASCENT
                data = Activity.getActivityInfo().totalAscent;
                dataValue = data != null ? data.format("%.0f") : null;
                break;
            case 18: // TOTAL_DESCENT
                data = Activity.getActivityInfo().totalDescent;
                dataValue = data != null ? data.format("%.0f") : null;
                break;    
            case 19: // CLOCK_TIME
                data = Sys.getClockTime();
                dataValue =  data != null ?
                    data.hour.format("%02d") + ":" +
                    data.min.format("%02d") + ":" +
                    data.sec.format("%02d"): null;
                break;
            case 20: // BATTERY
                data = Sys.getSystemStats().battery;
                dataValue = data != null ? data.format("%.1f") + "%" : null;
                break;
            default:
                break;
        }
        return [dataFieldLabels[i], dataValue];
    }

    function msToTime(ms, withDecimals) {
        var decimals = (ms % 1000) / 100;
        var seconds = (ms / 1000) % 60;
        var minutes = (ms / 60000) % 60;
        var hours = ms / 3600000;

        if (!withDecimals || hours > 0){
            return Lang.format("$1$:$2$:$3$", [hours, minutes.format("%02d"), seconds.format("%02d")]);
        }
        else{
            return Lang.format("$1$:$2$.$3$", [minutes.format("%02d"), seconds.format("%02d"), decimals.format("%1d")]);
        }

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

    function min(x,y) {
        if(x < y) {
            return x;
        } else {
            return y;
        }
    }
}