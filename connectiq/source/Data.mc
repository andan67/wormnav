using Toybox.Math;
using Toybox.Activity;
using Toybox.System as Sys;
using Toybox.Lang as Lang;
using Toybox.StringUtil;

module Data {

	enum {
		TIMER,
		DISTANCE,
		PACE,
		SPEED,
		AVERAGE_PACE,
		AVERAGE_SPEED,
		CURRENT_HEART_RATE,
		LAP_TIMER,
		LAP_DISTANCE,
		LAP_PACE,
		LAP_SPEED,
		LAST_LAP_PACE,
		LAST_LAP_SPEED,
		LAP
	}
	
	const AVG_CHAR = StringUtil.utf8ArrayToString([0xC3,0x98]);
	
	var dataScreensDefault = [	
						[TIMER,DISTANCE,AVERAGE_PACE,CURRENT_HEART_RATE],
						[DISTANCE,TIMER,AVERAGE_PACE],
						[]
					  ];
					  
	const dataFieldValues = [
		TIMER,
		DISTANCE,
		PACE,
		SPEED,
		AVERAGE_PACE,
		AVERAGE_SPEED,
		CURRENT_HEART_RATE,
		LAP_TIMER,
		LAP_DISTANCE,
		LAP_PACE,
		LAP_SPEED,
		LAST_LAP_PACE,
		LAST_LAP_SPEED,
		LAP];
		
	const dataFieldMenuLabels = [
		"Timer",
		"Dist.",
		"Pace",
		"Speed",
		"Avg\nPace",
		"Avg\nSpeed",
		"Heart\nRate",
		"Lap\nTimer",
		"Lap\nDist.",
		"Lap\nPace",
		"Lap\nSpeed",
		"Last\nLap\nPace",
		"Last\nLap\nSpeed",
		"Laps"];
			
	var dataFieldLabels = [
		"Timer",
		"Distance",
		"Pace",
		"Speed",
		AVG_CHAR + " Pace",
		AVG_CHAR + " Speed",
		"Heart Rate",
		"Lap Timer",
		"Lap Distance",
		"Lap Pace",
		"Lap Speed",
		"LL Pace",
		"LL Speed",
		"Laps"];
	
 	var dataScreens = dataScreensDefault;
	var activeDataScreens = [];
	
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
		return data!=null? Data.msToTime(data) : null;
	}
	
	function distance() {
		var data=Activity.getActivityInfo().elapsedDistance;
		return data!=null? data.format("%.2f") : null;
	}
	
	function averagePace() {
		var data=Activity.getActivityInfo().averageSpeed;
		return data!=null?  Data.convertSpeedToPace(data) : null;
	}
	
	function currentHeartRate() {
		var data= Activity.getActivityInfo().currentHeartRate;
		return data!=null?  Data.convertSpeedToPace(data) : null;
	}
	
	function lastLapPace() {
		if(Trace.isAutoLapActive && Trace.lapTime > 0) {
			return Data.convertSpeedToPace(1000*Trace.lapDistance/Trace.lapTime); 
		}
		return null;
	}
	
	function lapPace() {
		if(Trace.isAutoLapActive && Trace.elapsedlapTime > 0) {
			return Data.convertSpeedToPace(1000*Trace.elapsedLapDistance/Trace.elapsedlapTime); 
		}
		return null;
	}

	function lap() {
		if(Trace.isAutoLapActive){
			return Trace.lapCounter;
		}
		return null;
	}

	function getDataFieldLabelValue(i) {
		var dataValue = null;
		switch(i) {
			case TIMER:
				dataValue = timer();
				break;
			case DISTANCE:
				dataValue = distance();
				break;
			case PACE:
				//dataValue = pace();
				break;
			case SPEED:
				//dataValue = speed();
				break;		
			case AVERAGE_PACE:
				dataValue = averagePace();
				break;
			case AVERAGE_SPEED:
				//dataValue = averageSpeed();
				break;	
			case CURRENT_HEART_RATE:
				dataValue = currentHeartRate();
				break;
			case LAP_TIMER:
				//dataValue = lapTimer();
				break;
			case LAP_DISTANCE:
				//dataValue = lapDistance();
				break;
			case LAP_PACE:
				dataValue = lapPace();
				break;
			case LAP_SPEED:
				//dataValue = lapSpeed();
				break;
			case LAST_LAP_PACE:
				dataValue = lastLapPace();
				break;
			case LAST_LAP_SPEED:
				//dataValue = lastLapSpeed();
				break;
			case LAP:
				dataValue = lap();
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
    
    function max(x,y) {
    	if(x>=y) {
    		return x;
    	} else {
    		return y;
    	} 
    }
    
     function min(x,y) {
    	if(x<y) {
    		return x;
    	} else {
    		return y;
    	} 
    }
}