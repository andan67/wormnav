package org.andan.android.connectiq.wormnav;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import pt.karambola.gpx.beans.GenericPoint;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.RoutePoint;
import pt.karambola.gpx.beans.Track;
import pt.karambola.gpx.util.GpxUtils;

public class SendToDeviceUtility {

    private SendToDeviceUtility() {
    }

    public static void startDeviceBrowserActivity(Context ctx, Track track) {
        createIntentAndStartActivity(ctx, track.getTrackPoints() ,track.getName(), (float) GpxUtils.lengthOfTrack(track) );
    }

    public static void startDeviceBrowserActivity(Context ctx, Route route) {
        createIntentAndStartActivity(ctx, route.getRoutePoints(),route.getName(), (float) GpxUtils.lengthOfRoute(route) );
    }

    private static void createIntentAndStartActivity(Context ctx, List<? extends GenericPoint> genericPoints, String name, float length) {
        Data.geoPointsForDevice.clear();
        for(GenericPoint genericPoint : genericPoints) {
            //Data.geoPointsForDevice.add(new GeoPoint(genericPoint.getLatitude(), genericPoint.getLongitude(), genericPoint.getElevation()));
            Data.geoPointsForDevice.add(new GeoPoint(genericPoint.getLatitude(), genericPoint.getLongitude(), genericPoint.getElevation()==null ? 0. : genericPoint.getElevation()));
        }
        Intent intent = new Intent(ctx, DeviceBrowserActivity.class);
        intent.putExtra(DeviceBrowserActivity.TRACK_NAME, name);
        intent.putExtra(DeviceBrowserActivity.TRACK_LENGTH, length);

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        ctx.startActivity(intent);
    }

    /**
     *
     * @param geoPoints
     * @param trackName
     * @param maxPathWpt
     * @param maxPathError
     * @param invertCourse
     * @param sendElevationData
     * @return container message as list of objects that is send to the Garmin device via BLE
     * The default container elements are:
     *      0 : List<Float> with 7 elements:
     *          x coordinate of north west point of course bounding box (normalized)
     *          y coordinate of north west point of course bounding box (normalized)
     *          x coordinate of south east point of course bounding box (normalized)
     *          y coordinate of south east point of course bounding box (normalized)
     *          latitude of course center
     *          longitude of course center
     *          diagonal of bounding box on meters
     *      1 : String: track (course) name
     *      2 : Float: track length (after potential optimization)
     *      3 : Integer: number of track points (after potential optimization)
     *      4 : List<Float> with 2*(number of track points) elements containing x,y coordinates of track points
     * If elevations are sent:
     *      5 : List<Float> with (number of track points) elements containing elevation of coordinates
     *      6 : Float: Normalized route length calculated using x,y coordinates
     *      7 : Float: Minimum elevation
     *      8 : Float: Normalized distance from start to minimum elevation point
     *      9 : Integer: Index of minimum elevation point
     *      10 : Float: Maximum elevation
     *      11: Float: Normalized distance from start to maximum elevation point
     *      12: Integer: Index of maximum elevation point
     *      13: Float: total ascent
     *      14: Float: total descent
     */
    public static List <Object> generateMessageForDevice(List<GeoPoint> geoPoints, float originalTrackLength, String trackName, int maxPathWpt, double maxPathError,
                                                         boolean invertCourse, boolean sendElevationData) {
        List<Object> message = new ArrayList<>();
        if(geoPoints == null) return message;

        float trackLength = originalTrackLength;

        // optimize course
        if(invertCourse || (maxPathWpt > 0 && maxPathWpt <= geoPoints.size())) {
            Route route = new Route();
            for (GeoPoint geoPoint : geoPoints) {
                RoutePoint routePoint = new RoutePoint();
                routePoint.setLatitude(geoPoint.getLatitude());
                routePoint.setLongitude(geoPoint.getLongitude());
                routePoint.setElevation(geoPoint.getAltitude());
                route.addRoutePoint(routePoint);
            }
            if(invertCourse) {
                GpxUtils.reverseRoute(route);
            }
            if(maxPathWpt > 0 && maxPathWpt <= geoPoints.size()) {
                GpxUtils.simplifyRoute(route, maxPathWpt, maxPathError);
            }
            trackLength = (float) GpxUtils.lengthOfRoute(route);
            geoPoints = new ArrayList<>();
            for (RoutePoint routePoint : route.getRoutePoints()) {
                geoPoints.add(new GeoPoint(routePoint.getLatitude(), routePoint.getLongitude(), routePoint.getElevation()));
            }
        }

        BoundingBox boundingBox = findBoundingBox(geoPoints);

        double latCenter = Math.toRadians(boundingBox.getCenter().getLatitude());
        double lonCenter = Math.toRadians(boundingBox.getCenter().getLongitude());
        xyPoint xy = transform(Math.toRadians(boundingBox.getLatNorth()),
                Math.toRadians(boundingBox.getLonWest()),
                latCenter, lonCenter);

        List<Float> boundingBoxValueList = new ArrayList<>();


        // add bounding box related data to message container
        boundingBoxValueList.add((float) xy.x);
        boundingBoxValueList.add((float) xy.y);

        xy = transform(Math.toRadians(boundingBox.getLatSouth()),
                Math.toRadians(boundingBox.getLonEast()),
                latCenter, lonCenter);

        boundingBoxValueList.add((float) xy.x);
        boundingBoxValueList.add((float) xy.y);

        boundingBoxValueList.add((float) latCenter);
        boundingBoxValueList.add((float) lonCenter);
        boundingBoxValueList.add((float) boundingBox.getDiagonalLengthInMeters());

        message.add(boundingBoxValueList);

        message.add(trackName);
        message.add(trackLength);
        message.add(geoPoints.size());

        List<Float> trackPointsValueList = new ArrayList<>();
        List<Float> elevationValueList = new ArrayList<>();

        boolean hasElevation = true;

        for (int j = 0; j < geoPoints.size(); j++) {
            GeoPoint geoPoint = geoPoints.get(j);
            xyPoint xyP = transform(Math.toRadians(geoPoint.getLatitude()),
                    Math.toRadians(geoPoint.getLongitude()),
                    latCenter, lonCenter);
            trackPointsValueList.add((float) xyP.x);
            trackPointsValueList.add((float) xyP.y);
            if(hasElevation) {
                try {
                    elevationValueList.add((float) geoPoint.getAltitude());
                } catch (Exception e) {
                    hasElevation = false;
                }
            }
        }

        message.add(trackPointsValueList);

        if(hasElevation && sendElevationData ) {
            float xyLength = 0;
            int eleMinIdx = -1;
            int eleMaxIdx = -1;
            float eleMin = 20000;
            float eleMax = -20000;
            float eleMinDist = -1;
            float eleMaxDist = -1;
            float eleAscent = 0;
            float eleDescent = 0;

            for(int i = 0; i < elevationValueList.size(); i++) {
                float ele = elevationValueList.get(i);

                if(ele < eleMin) {
                    eleMin = ele;
                    eleMinIdx = i;
                }
                if(ele > eleMax) {
                    eleMax = ele;
                    eleMaxIdx = i;
                }
                if(i > 0) {
                    float elePrev = elevationValueList.get(i - 1);
                    if(ele - elePrev > 0) {
                        eleAscent += (ele - elePrev);
                    }
                    if(ele - elePrev < 0) {
                        eleDescent -= (ele - elePrev);
                    }
                }
            }

            for(int i = 0; i < trackPointsValueList.size() - 2; i += 2) {
                float dx = trackPointsValueList.get(i + 2) - trackPointsValueList.get(i);
                float dy = trackPointsValueList.get(i + 3) - trackPointsValueList.get(i + 1);

                if(i / 2 == eleMinIdx) {
                    eleMinDist = xyLength;

                }
                if(i / 2 == eleMaxIdx) {
                    eleMaxDist = xyLength;
                }
                xyLength += (float)Math.sqrt(dx * dx + dy * dy);
            }

            message.add(elevationValueList);
            message.add(xyLength);
            message.add(eleMin);
            message.add(eleMinDist);
            message.add(eleMinIdx);
            message.add(eleMax);
            message.add(eleMaxDist);
            message.add(eleMaxIdx);
            message.add(eleAscent);
            message.add(eleDescent);
        }
        return message;
    }

    /**
     * Calculate bounding box for given List of GeoPoints
     * Based on the osmdroid code by Nicolas Gramlich, released under the Apache License 2.0
     * https://github.com/osmdroid/osmdroid/blob/master/osmdroid-android/src/main/java/org
     * /osmdroid/util/BoundingBox.java
     */
    private static BoundingBox findBoundingBox(List<GeoPoint> geoPoints) {

        double minLat = Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        for (GeoPoint geoPoint : geoPoints) {

            final double latitude = geoPoint.getLatitude();
            final double longitude = geoPoint.getLongitude();

            minLat = Math.min(minLat, latitude);
            minLon = Math.min(minLon, longitude);
            maxLat = Math.max(maxLat, latitude);
            maxLon = Math.max(maxLon, longitude);

        }
        return new BoundingBox(maxLat, maxLon, minLat, minLon);
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

class TransmissionLogEntry {
    String trackName;

    int noTrackPointsOriginal;
    double trackLengthOriginal;

    boolean isOptimized;
    int noTrackPointsSent;
    double trackLengthSent;

    long sendTime;

    String deviceName;
    String statusMessage;
    int statusCode;

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public int getNoTrackPointsOriginal() {
        return noTrackPointsOriginal;
    }

    public void setNoTrackPointsOriginal(int noTrackPointsOriginal) {
        this.noTrackPointsOriginal = noTrackPointsOriginal;
    }

    public double getTrackLengthOriginal() {
        return trackLengthOriginal;
    }

    public void setTrackLengthOriginal(double trackLengthOriginal) {
        this.trackLengthOriginal = trackLengthOriginal;
    }

    public boolean isOptimized() {
        return isOptimized;
    }

    public void setOptimized(boolean optimized) {
        isOptimized = optimized;
    }

    public int getNoTrackPointsSent() {
        return noTrackPointsSent;
    }

    public void setNoTrackPointsSent(int noTrackPointsSent) {
        this.noTrackPointsSent = noTrackPointsSent;
    }

    public double getTrackLengthSent() {
        return trackLengthSent;
    }

    public void setTrackLengthSent(double trackLengthSent) {
        this.trackLengthSent = trackLengthSent;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        SimpleDateFormat sf = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "TransmissionLogEntry{" +
                "trackName='" + trackName + '\'' +
                ", noTrackPointsOriginal=" + noTrackPointsOriginal +
                ", trackLengthOriginal=" + trackLengthOriginal +
                ", isOptimized=" + isOptimized +
                ", noTrackPointsSent=" + noTrackPointsSent +
                ", trackLengthSent=" + trackLengthSent +
                ", sendTime=" + sf.format(sendTime) +
                ", deviceName='" + deviceName + '\'' +
                ", statusMessage='" + statusMessage + '\'' +
                ", statusCode=" + statusCode +
                '}';
    }

    @SuppressLint("DefaultLocale")
    public String toLogString() {
        if(!isOptimized)
            if(statusCode==IQSendMessageIntentService.MESSAGE_SEND_OK)
                return String.format("%tF %tT: Track '%s' (%.2fkm, #%d) successfully sent to device '%s'",
                    new Date(sendTime), new Time(sendTime), trackName,
                            0.001 * trackLengthOriginal, noTrackPointsOriginal,
                            deviceName);
            else
                return String.format("%tF %tT: Track '%s' (%.2fkm, #%d) unsuccessfully sent to device '%s' for reason '%s'",
                        new Date(sendTime), new Time(sendTime), trackName,
                        0.001 * trackLengthOriginal, noTrackPointsOriginal,
                        deviceName, statusMessage);
        else {
            if(statusCode==IQSendMessageIntentService.MESSAGE_SEND_OK)
                return String.format("%tF %tT: Optimized track '%s' (%.2fkm->%.2fkm, #%d->#%d) successfully sent to device '%s'",
                        new Date(sendTime), new Time(sendTime), trackName,
                        0.001 * trackLengthOriginal, 0.001 * trackLengthSent,
                        noTrackPointsOriginal, noTrackPointsSent,
                        deviceName);
            else
                return String.format("%tF %tT: Optimized track '%s' (%.2fkm->%.2fkm, #%d->#%d) unsuccessfully sent to device '%s' for reason '%s'",
                        new Date(sendTime), new Time(sendTime), trackName,
                        0.001 * trackLengthOriginal, 0.001 * trackLengthSent,
                        noTrackPointsOriginal, noTrackPointsSent,
                        deviceName, statusMessage);
        }
    }

    static public ArrayList<String> toStringArray(List<TransmissionLogEntry> entries) {
        ArrayList<String> stringArray = new ArrayList();
        if(entries!= null && !entries.isEmpty()) {
            // add log string in reverse order
            for (int i=entries.size()-1; i>=0; i-=1) {
                stringArray.add(entries.get(i).toLogString());
            }
        }
        return stringArray;
    }

    public void addToConstraintList(List<TransmissionLogEntry> entries, int maxSize) {
        if (entries != null) {
            if(entries.size() == maxSize) {
                entries.remove(0);
            }
            entries.add(this);
        }
    }
}

