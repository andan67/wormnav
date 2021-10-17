

class TrackModel {

    var lat_center;
    var lon_center;
    var diagonal;
    var name;
    var length;
    var nPoints;
    var xyArray;
    var xyLength;
    var xyLengthString;
    var eleMinIdx;
    var eleMaxIdx;
    var eleMinDist;
    var eleMaxDist;
    var eleMin;
    var eleMax;
    var eleUp;
    var eleDown;
    var eleArray;

    hidden var data;
    hidden var boundingBox;

    // lat lon values must be in radians!
    function initialize(msg) {

        data = msg;
        boundingBox = data[0];
        lat_center = boundingBox[4];
        lon_center = boundingBox[5];
        diagonal = boundingBox[6];
        name = data[1];
        length = data[2];
        nPoints = data[3];
        xyArray = data[4];
        xyLength = 0.0;
        var dx;
        var dy;
        eleMin = null;
        eleMax = null;


        if(data.size() > 5) {
            // message contains elevation data
            eleArray = data[5];
             // determine elevation stats
            eleMinIdx = 0;
            eleMaxIdx = 0;
            eleMin = 20000.0;
            eleMax = -20000.0;
            eleUp = 0.0;
            eleDown = 0.0;

            for(var i = 0; i < eleArray.size(); i++) {
                var ele = eleArray[i];

                if(ele < eleMin) {
                   eleMin = ele;
                   eleMinIdx = i;
                }
                if(ele > eleMax) {
                   eleMax = ele;
                   eleMaxIdx = i;
                }
                if(i > 0) {
                    var elePrev = eleArray[i - 1];
                    if(ele - elePrev > 0) {
                        eleUp += (ele - elePrev);
                    }
                    if(ele - elePrev < 0) {
                        eleDown -= (ele - elePrev);
                    }
                }

            }

            System.println("ele: " + eleArray.size());
            System.println("eleMin: " + eleMin);
            System.println("eleMax: " + eleMax);

        }

        for(var i = 0; i < xyArray.size() - 2 ; i += 2) {
            dx = xyArray[i + 2] - xyArray[i];
            dy = xyArray[i + 3] - xyArray[i + 1];
            if(eleArray != null) {
                if(i / 2 == eleMinIdx) {
                    eleMinDist = xyLength;

                }
                if(i / 2 == eleMaxIdx) {
                    eleMaxDist = xyLength;
                }
            }
            xyLength += Math.sqrt(dx * dx + dy * dy);
        }
        var ls = 6371.0 * xyLength;
        var w = 3;
        if(ls >= 10.0 && ls < 100) {
            w = 2;
        } else if(ls >= 100) {
            w = 1;
        }

        xyLengthString = ls.format("%." + w + "f") + " km";
        System.println("length: " + length);
        System.println("xyLength: " + xyLength);
        System.println("xyLength scale: " + 6371000.0 * xyLength);
        System.println("xyLength formatted: " + xyLengthString);

    }
}
