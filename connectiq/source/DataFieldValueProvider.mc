using Toybox.Activity;

class DataFieldValueProvider {
	
	enum {
		UNSET,
		DISTANCE,
		TIMER,
		AVGERAGE_PACE,
		CURRENT_HEART_RATE,
		PACE,
		SPEED,
		LAP_PACE,
		LAP_SPEED,
		LAST_LAP_PACE,
		LAST_LAP_SPEED,
		LAP_DISTANCE
	}
	
	const MEAN_CHAR = StringUtil.utf8ArrayToString([0xC3,0x98]);
	
	private var code2Func = {};
	
	function initialize() {
      code2Func.put(Data.TIMER,self.method(:timer));
    }
	
	function timer() {
		var data=Activity.getActivityInfo().timerTime;
		return ["Timer", data!=null? Data.msToTime(data) : null];
	}
	
	function distance() {
		var data=Activity.getActivityInfo().elapsedDistance;
		return ["Distance", data!=null? data.format("%.2f") : null];
	}
	
	function averagePace() {
		var data=Activity.getActivityInfo().averageSpeed;
		return [MEAN_CHAR + " Pace", data!=null?  Data.convertSpeedToPace(data) : null];
	}
	
	function currentHeartRate() {
		var data= Activity.getActivityInfo().currentHeartRate;
		return ["Heart Rate", data!=null?  Data.convertSpeedToPace(data) : null];
	}
}