class TrackModel {

    var latCenter;
    var lonCenter;
    var diagonal;
    var name;
    var length;
    var nPoints;
    var xyArray;
    var eleArray;
    var xyLength;
    var xyLengthLabel;
    var eleMinIdx;
    var eleMaxIdx;
    var eleMinDist;
    var eleMaxDist;
    var eleMin;
    var eleMax;
    var eleTotAscent;
    var eleTotDescent;

    hidden var data;

    // lat lon values must be in radians!
    function initialize(msg) {

        data = msg;
        latCenter = data[0][4];
        lonCenter = data[0][5];
        diagonal = data[0][6];
        name = data[1];
        length = data[2];
        nPoints = data[3];
        xyArray = data[4];
        
        if(data.size() > 5) {
            // message contains elevation data
            eleArray = data[5];
            xyLength = data[6];
            xyLengthLabel = Track.formatLength(xyLength);  
            eleMin = data[7];
            eleMinDist = data[8];
            eleMinIdx = data[9];
            eleMax = data[10];
            eleMaxDist = data[11];
            eleMaxIdx = data[12];
            eleTotAscent = data[13];
            eleTotDescent = data[14];
        }
    }

    function clear() {
        data = null;
        xyArray = null;
        eleArray = null;
    }
}
