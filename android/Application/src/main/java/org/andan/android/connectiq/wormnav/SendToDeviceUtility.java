package org.andan.android.connectiq.wormnav;

import android.content.Context;
import android.content.Intent;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import pt.karambola.R3.R3;
import pt.karambola.gpx.beans.GenericPoint;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.RoutePoint;
import pt.karambola.gpx.beans.Track;
import pt.karambola.gpx.beans.TrackPoint;
import pt.karambola.gpx.util.GpxUtils;

public class SendToDeviceUtility {



    private SendToDeviceUtility() {
    }

    public static void startDeviceBrowserActivity(Context ctx, Track track) {
        List<GenericPoint> genericPoints = new ArrayList<>();
        for (TrackPoint trackPoint : track.getTrackPoints()) {
            genericPoints.add(trackPoint);
        }
        createIntentAndStartActivity(ctx, genericPoints,track.getName(), (float) GpxUtils.lengthOfTrack(track) );
    }

    public static void startDeviceBrowserActivity(Context ctx, Route route) {
        List<GenericPoint> genericPoints = new ArrayList<>();
        for (RoutePoint routePoint : route.getRoutePoints()) {
            genericPoints.add(routePoint);
        }
        createIntentAndStartActivity(ctx, genericPoints,route.getName(), (float) GpxUtils.lengthOfRoute(route) );
    }

    private static void createIntentAndStartActivity(Context ctx, List<GenericPoint> genericPoints, String name, float length) {
        List<GeoPoint> geoPoints = new ArrayList<>();

        float[] trackPointsToSend = new float[genericPoints.size() * 2];

        for (int j = 0; j < genericPoints.size(); j++) {

            GenericPoint genericPoint = genericPoints.get(j);
            GeoPoint geoPoint = new GeoPoint(genericPoint.getLatitude(), genericPoint.getLongitude());
            geoPoints.add(geoPoint);
            /*
            trackPointsToSend[2 * j] = (float) Math.toRadians(genericPoint.getLatitude());
            trackPointsToSend[2 * j + 1] = (float) Math.toRadians(genericPoint.getLongitude());
            */
        }

        BoundingBox boundingBox = ((Utils) ctx).findBoundingBox(geoPoints);
        System.out.println("Send to device -" +
                " Center lat:" + boundingBox.getCenter().getLatitude() +
                " Center lon: " + boundingBox.getCenter().getLongitude() +
                " Bbox lon west: " + boundingBox.getLonWest() +
                " Bbox lon east: " + boundingBox.getLonEast() +
                " Bbox lat south: " + boundingBox.getLatSouth() +
                " Bbox lat north: " + boundingBox.getLatNorth() +
                " Diameter: " + boundingBox.getDiagonalLengthInMeters() / 1000 +
                " Name :" + name +
                " #points:" + genericPoints.size() +
                " length: " + length);

        double latCenter = Math.toRadians(boundingBox.getCenter().getLatitude());
        double lonCenter = Math.toRadians(boundingBox.getCenter().getLongitude());

        for (int j = 0; j < genericPoints.size(); j++) {
            GenericPoint genericPoint = genericPoints.get(j);
            xyPoint xy = transform(Math.toRadians(genericPoint.getLatitude()),
                    Math.toRadians(genericPoint.getLongitude()),
                    latCenter, lonCenter);
            trackPointsToSend[2 * j] = (float) xy.x;
            trackPointsToSend[2 * j + 1] = (float) xy.y;
        }

        float[] track_boundingBox = new float[7];
        /*
        track_boundingBox[0] = (float) Math.toRadians(boundingBox.getLatNorth());
        track_boundingBox[1] = (float) Math.toRadians(boundingBox.getLatSouth());
        track_boundingBox[2] = (float) Math.toRadians(boundingBox.getLonWest());
        track_boundingBox[3] = (float) Math.toRadians(boundingBox.getLonEast());
        track_boundingBox[4] = (float) Math.toRadians(boundingBox.getCenter().getLatitude());
        track_boundingBox[5] = (float) Math.toRadians(boundingBox.getCenter().getLongitude());
        track_boundingBox[6] = (float) boundingBox.getDiagonalLengthInMeters();
        */
        xyPoint xy = transform(Math.toRadians(boundingBox.getLatNorth()),
                Math.toRadians(boundingBox.getLonWest()),
                latCenter, lonCenter);
        track_boundingBox[0] = (float) xy.x;
        track_boundingBox[1] = (float) xy.y;
        xy = transform(Math.toRadians(boundingBox.getLatSouth()),
                Math.toRadians(boundingBox.getLonEast()),
                latCenter, lonCenter);
        track_boundingBox[2] = (float) xy.x;
        track_boundingBox[3] = (float) xy.y;
        track_boundingBox[4] = (float) latCenter;
        track_boundingBox[5] = (float) lonCenter;
        track_boundingBox[6] = (float) boundingBox.getDiagonalLengthInMeters();

        Intent intent = new Intent(ctx, DeviceBrowserActivity.class);
        intent.putExtra(DeviceBrowserActivity.TRACK_BOUNDING_BOX, track_boundingBox);
        intent.putExtra(DeviceBrowserActivity.TRACK_NAME, name);
        intent.putExtra(DeviceBrowserActivity.TRACK_LENGTH, length);
        intent.putExtra(DeviceBrowserActivity.TRACK_NUMBER_OF_POINTS, genericPoints.size());
        intent.putExtra(DeviceBrowserActivity.TRACK_POINTS, trackPointsToSend);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        ctx.startActivity(intent);
    }

    private static xyPoint transform(double lat, double lon, double latCenter, double lonCenter) {
        return new xyPoint (
                Math.cos(lat)*Math.sin(lon-lonCenter),
                Math.cos(latCenter)*Math.sin(lat) - Math.sin(latCenter)*Math.cos(lat)*Math.cos(lon-lonCenter));
    }


}

class xyPoint {
    double x;
    double y;

    xyPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }
}