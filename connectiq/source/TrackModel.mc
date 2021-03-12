class TrackModel {

    var lat_center;
    var lon_center;
    var diagonal;
    var name;
    var length;
    var nPoints;
    var xyArray;

    hidden var data;
    hidden var boundingBox;
    
/*
//MENU: Simulation->Phone App Message: TEST 1:
[
  [ 0, 1, 2, 3, 40, 20, 20], 
  "name_test1", 
  1000, 
  20, 
  [ 0, 0, 
    1, 1, 
    2, 2, 
    3, 3, 
    4, 4, 
    5, 5, 
    4, 4, 
    3, 3, 
    2, 2, 
    1, 1]
]

//MENU: Simulation->Phone App Message: TEST 1:
[
  [ 0, 0, 0, 0, 40.368576, -3.612573, 40], 
  "name_test2", 
  1000, 
  12, 
  [ 40.368576, -3.612573, 
    40.368586, -3.612533,
    40.368596, -3.612523,
    40.368576, -3.612513,
    40.368586, -3.612503,
    40.368596, -3.612513,
    40.368506, -3.612523,
    40.368516, -3.612533,
    40.368526, -3.612543,
    40.368536, -3.612553,
    40.368546, -3.612563,
    40.368566, -3.612573]
]

//MENU: Simulation->Phone App Message: TEST 3:
[
  [ 0, 1, 2, 3, 20, 20, 40], 
  "name_test3", 
  1000, 
  20, 
  [ 0, 10, 1, 11, 2, 12, 3, 13, 4, 14, 5, 15, 6, 16, 7, 17, 8, 18, 9, 19, 0, 10]
]

*/

    // lat lon values must be in radians!
    function initialize(msg) {
        
        data = msg;
        /*
        System.println("data[0]:" + data[0]);
        System.println("data[1]:" + data[1]);
        System.println("data[2]:" + data[2]);
        System.println("data[3]:" + data[3]);
        System.println("data[4]:" + data[4]);
        */
        
        boundingBox = data[0];
        lat_center = boundingBox[4];
        lon_center = boundingBox[5];
        diagonal = boundingBox[6];
        name = data[1];
        length = data[2];
        nPoints = data[3];
        xyArray = data[4];
    }

    function clean() {
        data=null;
        boundingBox=null;
        lat_center =null;
        lon_center = null;
        diagonal=null;
        name=null;
        length=null;
        nPoints=null;
        xyArray=null;
        return null;
    }

}
