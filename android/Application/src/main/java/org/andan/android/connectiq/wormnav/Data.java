package org.andan.android.connectiq.wormnav;

import android.net.Uri;

import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import pt.karambola.geo.Units;
import pt.karambola.gpx.beans.Gpx;
import pt.karambola.gpx.beans.Point;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.Track;
import pt.karambola.gpx.comparator.RouteComparator;
import pt.karambola.gpx.comparator.TrackComparator;
import pt.karambola.gpx.predicate.PointFilter;
import pt.karambola.gpx.predicate.RouteFilter;

/**
 * Created by piotr on 16.05.17.
 * Common static data defined here
 */

final class Data {

    static ArrayList<GeoPoint> sCardinalGeoPoints;
    static GeoPoint sCurrentPosition;
    static Route osrmRoute;

    static final int ROUTING_SRC_OSRM = 0;
    static final int ROUTING_SRC_MAPQUEST = 1;
    static Integer sRoutingSource = null;

    /**
     * Encode your MapQuest API key (e.g. at https://www.base64encode.org/) and paste below
     * if you want the app to use it. Leave the line UNCHANGED (as below):
     *
     * String sEncodedKey = "YOUR_BASE64_ENCODED_KEY_HERE";
     *
     * if you don't want to provide a key.
     */
    static String sEncodedKey = "YOUR_BASE64_ENCODED_KEY_HERE";

    static String sMapQuestKey = "";

    /**
     * Routing profile to be used in the MapQuest API request
     */
    static final String MODE_CAR = "routeType=fastest";
    static final String MODE_BIKE = "routeType=bicycle";
    static final String MODE_FOOT = "routeType=pedestrian";

    static String sRoutingProfile = MODE_CAR;

    static Gpx mGpx = null;

    static Gpx sPoiGpx = new Gpx();
    static Gpx sRoutesGpx = new Gpx();
    static Gpx sTracksGpx = new Gpx();

    static Uri lastImportedExportedUri;
    static Uri lastLoadedSavedUri;

    static boolean saveIntoRepositoryOnExit = true;
    static boolean loadFromRepositoryOnStart = true;

    static File applicationFilesDir;
    static final String applicationRepositoryFilename = "wormnav.gpx";

    static boolean useDefaultSendElevationData = false;

    static boolean useDefaultOptimization = false;
    static int defaultMaxPathWpt = 0;
    static double defaultMaxPathError = 10d;

    static Double sLastZoom;
    static GeoPoint sLastCenter;
    static Float sLastRotation;

    /**
     * Index of currently selected route on the Data.sFilteredRoutes list
     * (Routes Browser - filtered view); null if nothing selected
     */
    static Integer sSelectedRouteIdx = null;

    /**
     * We'll edit a copy of selected route in case user given up
     */
    static Route sCopiedRoute;
    static List<GeoPoint> routeNodes;

    /**
     * Routes view filtering
     */
    static List<String> sSelectedRouteTypes;
    static Double sDstStartMinValue = null;
    static Double sDstStartMaxValue = null;
    static Double sLengthMinValue = null;
    static Double sLengthMaxValue = null;
    static List<Route> sFilteredRoutes = new ArrayList<>();
    static RouteFilter sViewRouteFilter = new RouteFilter();

    /**
     * Comparator used in browsers
     */
    //static Comparator<Route> rteComparator = RouteComparator.NAME;
    static Comparator<Route> rteComparator = RouteComparator.NONE;
    //static Comparator<Track> trkComparator = TrackComparator.NAME;
    static Comparator<Track> trkComparator = TrackComparator.NONE;

    /**
     * Common setting in the main activity
     */
    static Units sUnitsInUse = Units.METRIC;
    static boolean sAllowRotation = false;

    static final int POINTS_DISPLAY_LIMIT = 20;

    /*
     * Activity results
     */
    static final int NEW_ROUTE_ADDED = 70;

    /**
     * Route Optimizer
     */
    static int sSourceRoutePointsNumber;
    static int sCurrentMaxPointsNumber;
    static double currentMaxErrorMtr = 0.00;

    static final int OPTIMIZER_POINTS_LIMIT = 1000;

    /**
     * POI view filtering
     */
    static List<Point> sFilteredPoi = new ArrayList<>();
    static PointFilter sViewPoiFilter = new PointFilter();
    static Gpx sCopiedPoiGpx;

    /**
     * Not tracks vew filtering, at least for now
     */
    static List<Track> sAllTracks = new ArrayList<>();

    /**
     * Index of currently selected track
     */
    static Integer sSelectedTrackIdx = null;

    /**
     * List of GeoPoints in sent to Device
     */
    static List<GeoPoint> geoPointsForDevice = new ArrayList<>();
}
