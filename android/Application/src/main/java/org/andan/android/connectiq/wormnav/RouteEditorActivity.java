package org.andan.android.connectiq.wormnav;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import android.text.InputFilter;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.karambola.commons.collections.ListUtils;
import pt.karambola.gpx.beans.Point;
import pt.karambola.gpx.beans.RoutePoint;
import pt.karambola.gpx.util.GpxRouteUtils;
import pt.karambola.gpx.util.GpxUtils;

import static org.andan.android.connectiq.wormnav.R.id.osmmap;

/**
 * Route Creator activity created by piotr on 02.05.17.
 */
public class RouteEditorActivity extends Utils {

    private final String TAG = "Creator";

    private Map<Marker, RoutePoint> markerToRoutePoint;

    private Map<Marker, Point> markerToPoi;

    private final double MAX_ZOOM_LEVEL = 19;
    private final double MIN_ZOOM_LEVEL = 4;

    Button fitButton;
    Button zoomInButton;
    Button zoomOutButton;
    Button saveButton;

    TextView routePrompt;

    AlertDialog mEditDialog;
    AlertDialog mAddFromPoiDialog;

    private MapView mMapView;
    private IMapController mapController;

    private MapEventsReceiver mapEventsReceiver;

    private MyLocationNewOverlay mLocationOverlay;

    private RotationGestureOverlay mRotationGestureOverlay;

    private boolean mMapDragged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00ffffff")));
            actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#00ffffff")));
        }

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_route_editor);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationCallback();
        createLocationRequest();

        setUpMap();

        refreshMap();
    }

    private void setUpMap() {

        mMapView = (MapView) findViewById(osmmap);

        mMapView.setTilesScaledToDpi(true);

        mMapView.setTileSource(TileSourceFactory.MAPNIK);

        TilesOverlay tilesOverlay = mMapView.getOverlayManager().getTilesOverlay();
        //tilesOverlay.setOvershootTileCache(tilesOverlay.getOvershootTileCache() * 2);

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mMapView);
        mLocationOverlay.enableMyLocation();

        mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
        mRotationGestureOverlay.setEnabled(true);

        mMapView.setMaxZoomLevel(MAX_ZOOM_LEVEL);
        mMapView.setMinZoomLevel(MIN_ZOOM_LEVEL);

        mMapView.setMultiTouchControls(true);

        mapController = mMapView.getController();

        mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {

                RoutePoint routePoint = new RoutePoint();
                routePoint.setLatitude(p.getLatitude());
                routePoint.setLongitude(p.getLongitude());
                Data.sCopiedRoute.addRoutePoint(routePoint);

                refreshMap();
                return false;
            }
        };

        restoreMapPosition();

        mMapView.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {

                mMapDragged = true;
                //refreshMap();
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {

                mMapDragged = true;

                return false;
            }
        });

        setUpButtons();
        setButtonsState();
    }

    private void restoreMapPosition() {

        if (Data.sLastZoom == null) {
            mapController.setZoom(3);
        } else {
            mapController.setZoom(Data.sLastZoom);
        }

        if (Data.sLastCenter == null) {
            mapController.setCenter(new GeoPoint(0d, 0d));
        } else {
            mapController.setCenter(Data.sLastCenter);
        }
    }

    private void refreshMap() {

        mMapView.getOverlays().clear();

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mMapView.getOverlays().add(0, mapEventsOverlay);

        mMapView.getOverlays().add(mLocationOverlay);

        if (Data.sAllowRotation) {
            mMapView.getOverlays().add(this.mRotationGestureOverlay);
        }

        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(mMapView);
        mMapView.getOverlays().add(mScaleBarOverlay);

        mScaleBarOverlay.setScaleBarOffset(
                (int) (getResources().getDisplayMetrics().widthPixels / 2 - getResources()
                        .getDisplayMetrics().xdpi / 2), 10);

        if (showPoi) {
            drawPoi();
        }

        Polyline routeOverlay = new Polyline();
        routeOverlay.setColor(Color.parseColor("#0066ff"));

        Data.routeNodes = new ArrayList<>();
        markerToRoutePoint = new HashMap<>();

        List<RoutePoint> allRoutePointsList = Data.sCopiedRoute.getRoutePoints();

        /*
         * Let's limit the number of markers to draw to up to Data.POINTS_DISPLAY_LIMIT nearest to the center of the map
         */
        List<RoutePoint> nearestRoutePoints = Utils.getNearestRoutePoints(mMapView.getMapCenter(), Data.sCopiedRoute);

        for (RoutePoint routePoint : nearestRoutePoints) {

            GeoPoint markerPosition = new GeoPoint(routePoint.getLatitude(), routePoint.getLongitude());

            String displayName;
            if (routePoint.getName() != null && !routePoint.getName().isEmpty()) {
                displayName = routePoint.getName();
            } else {
                displayName = String.valueOf(allRoutePointsList.indexOf(routePoint));
            }

            Drawable icon = new BitmapDrawable(getResources(), makeMarkerBitmap(this, displayName));

            Marker marker = new Marker(mMapView);
            marker.setPosition(markerPosition);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setDraggable(true);
            marker.setIcon(icon);

            markerToRoutePoint.put(marker, routePoint);
            mMapView.getOverlays().add(marker);

            marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {

                    RoutePoint draggedPoint = markerToRoutePoint.get(marker);
                    draggedPoint.setLatitude(marker.getPosition().getLatitude());
                    draggedPoint.setLongitude(marker.getPosition().getLongitude());

                    refreshMap();
                }

                @Override
                public void onMarkerDragStart(Marker marker) {
                }
            });

            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {

                    /*
                     * @osmdroid allows to click multiple markers at a time.
                     * This is to avoid opening a dialog for each clicked one.
                     */
                    if (mEditDialog == null || !mEditDialog.isShowing()) {

                        RoutePoint clickedPoint = markerToRoutePoint.get(marker);
                        displayEditDialog(clickedPoint);
                    }

                    return false;
                }
            });
        }

        /*
         * And now we need all RoutePoints to draw the full path
         */
        for (int i = 0; i < allRoutePointsList.size(); i++) {

            RoutePoint routePoint = allRoutePointsList.get(i);
            GeoPoint node = new GeoPoint(routePoint.getLatitude(), routePoint.getLongitude());
            Data.routeNodes.add(node);
        }
        routeOverlay.setPoints(Data.routeNodes);
        mMapView.getOverlays().add(routeOverlay);

        routePrompt.setText(String.format(getResources().getString(R.string.map_prompt_route), Data.routeNodes.size()));

        mMapView.invalidate();
        setButtonsState();
    }

    private void drawPoi() {

        BoundingBox mMapViewBoundingBox = mMapView.getBoundingBox();

        Data.sFilteredPoi = ListUtils.filter(Data.sPoiGpx.getPoints(), Data.sViewPoiFilter);

        /*
         * Let's assign a color to each existing POI type
         */
        List<String> wptTypes = GpxUtils.getDistinctPointTypes(Data.sFilteredPoi);

        Map<String, Integer> wptTypeColourMap = new HashMap<>();
        int colourIdx = 0;
        for (String wptType : wptTypes) {
            wptTypeColourMap.put(wptType, typeColors[colourIdx++ % N_COLOURS]);
        }

        markerToPoi = new HashMap<>();

        for (Point poi : Data.sFilteredPoi) {

            GeoPoint markerPosition = new GeoPoint(poi.getLatitude(), poi.getLongitude());

            String displayName;
            if (poi.getName() != null && !poi.getName().isEmpty()) {
                displayName = poi.getName();
            } else {
                displayName = String.valueOf(Data.sFilteredPoi.indexOf(poi));
            }

            /*
             * Use the color from the map if the POI has a type defined.
             * If not - paint in grey.
             */
            int color;
            if (poi.getType() == null) {
                color = Color.parseColor("#999999");
            } else {
                color = wptTypeColourMap.get(poi.getType());
            }
            Drawable icon = new BitmapDrawable(getResources(), makeMarkerBitmap(this, displayName, color));

            Marker marker = new Marker(mMapView);
            marker.setPosition(markerPosition);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setDraggable(false);
            marker.setIcon(icon);

            markerToPoi.put(marker, poi);

            if (mMapViewBoundingBox.contains(markerPosition)) {
                mMapView.getOverlays().add(marker);
            }

            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {

                    /*
                     * @osmdroid allows to click multiple markers at a time.
                     * This is to avoid opening a dialog for each clicked one.
                     */
                    if (mAddFromPoiDialog == null || !mAddFromPoiDialog.isShowing()) {

                        addFromPoi(markerToPoi.get(marker));
                    }
                    return false;
                }
            });
        }
    }

    private void setUpButtons() {

        locationButton = (Button) findViewById(R.id.location_button);
        locationButton.setEnabled(false);
        locationButton.getBackground().setAlpha(0);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapController.setZoom(18);
                mapController.setCenter(Data.sCurrentPosition);

                refreshMap();
                setButtonsState();
            }
        });

        fitButton = (Button) findViewById(R.id.fit_button);
        fitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Data.routeNodes != null && Data.routeNodes.size() > 1) {
                    mMapView.zoomToBoundingBox(findBoundingBox(Data.routeNodes).increaseByScale(1.1f), false);
                }
                refreshMap();
                setButtonsState();
            }
        });
        zoomInButton = (Button) findViewById(R.id.zoom_in_button);
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mapController.setZoom(mMapView.getProjection().getZoomLevel() + 1);

                refreshMap();
                setButtonsState();
            }
        });
        zoomOutButton = (Button) findViewById(R.id.zoom_out_button);
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mapController.setZoom(mMapView.getProjection().getZoomLevel() - 1);

                refreshMap();
                setButtonsState();
            }
        });

        saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(RouteEditorActivity.this, RoutesBrowserActivity.class);

                if (Data.sSelectedRouteIdx != null) {

                    Data.sRoutesGpx.removeRoute(Data.sFilteredRoutes.get(Data.sSelectedRouteIdx));
                    Data.sRoutesGpx.addRoute(Data.sCopiedRoute);
                    Data.sSelectedRouteIdx = Data.sRoutesGpx.getRoutes().indexOf(Data.sCopiedRoute);

                } else {

                    Data.sRoutesGpx.addRoute(Data.sCopiedRoute);
                    Data.sSelectedRouteIdx = Data.sRoutesGpx.getRoutes().indexOf(Data.sCopiedRoute);
                    setResult(Data.NEW_ROUTE_ADDED, i);
                }
                finish();
            }
        });

        routePrompt = (TextView) findViewById(R.id.route_prompt);

        final TextView copyright = (TextView) findViewById(R.id.copyright);
        copyright.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setButtonsState() {

        if (mMapView.getProjection().getZoomLevel() < MAX_ZOOM_LEVEL) {
            zoomInButton.setEnabled(true);
            zoomInButton.getBackground().setAlpha(255);
        } else {
            zoomInButton.setEnabled(false);
            zoomInButton.getBackground().setAlpha(100);
        }

        if (mMapView.getProjection().getZoomLevel() > MIN_ZOOM_LEVEL) {
            zoomOutButton.setEnabled(true);
            zoomOutButton.getBackground().setAlpha(255);
        } else {
            zoomOutButton.setEnabled(false);
            zoomOutButton.getBackground().setAlpha(100);
        }

        if (Data.sCopiedRoute.isChanged()) {
            saveButton.setEnabled(true);
            saveButton.getBackground().setAlpha(255);
        } else {
            saveButton.setEnabled(false);
            saveButton.getBackground().setAlpha(100);
        }

    }

    /**
     * Route Point edition
     */
    private void displayEditDialog(final RoutePoint routePoint) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.waypoint_edit_dialog, mMapView, false);

        final EditText editName = (EditText) layout.findViewById(R.id.wp_edit_name);
        editName.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(19)
        });
        if (routePoint.getName() != null) {
            editName.setText(routePoint.getName());
        }

        String dialogTitle = getResources().getString(R.string.waypoint_edit_title);
        String okText = getResources().getString(R.string.dialog_apply);

        String deleteText = getResources().getString(R.string.dialog_delete);
        String insertText = getResources().getString(R.string.dialog_insert_b4);

        builder.setTitle(dialogTitle)
                .setView(layout)
                .setIcon(R.drawable.map_edit)
                .setCancelable(true)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        String name = editName.getText().toString().trim();
                        if (!name.isEmpty()) {
                            if (name.length() > 20) name = name.substring(0, 21);
                            routePoint.setName(name);

                        } else {
                            routePoint.setName(null);
                        }

                        refreshMap();
                    }
                })
                .setNegativeButton(insertText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        int idx = Data.sCopiedRoute.getRoutePoints().lastIndexOf(routePoint);
                        double this_point_lat = routePoint.getLatitude();
                        double this_point_lon = routePoint.getLongitude();

                        RoutePoint previousPoint = Data.sCopiedRoute.getRoutePoints().get(idx - 1);
                        double previous_point_lat = previousPoint.getLatitude();
                        double previous_point_lon = previousPoint.getLongitude();

                        RoutePoint halfway_point = new RoutePoint();
                        Data.sCopiedRoute.addRoutePoint(idx, halfway_point);
                        halfway_point.setLatitude((this_point_lat + previous_point_lat) * 0.5);
                        halfway_point.setLongitude((this_point_lon + previous_point_lon) * 0.5);

                        refreshMap();
                    }
                })
                .setNeutralButton(deleteText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Data.sCopiedRoute.removeRoutePoint(routePoint);

                        refreshMap();
                    }
                });


        mEditDialog = builder.create();

        mEditDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                if (Data.sCopiedRoute.getRoutePoints().lastIndexOf(routePoint) == 0) {
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                }
            }
        });

        final Button instertStartButton = (Button) layout.findViewById(R.id.start_end_button);
        instertStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditDialog.dismiss();
                insertStartEnd(routePoint);
            }
        });

        final Button endHereButton = (Button) layout.findViewById(R.id.end_here_button);
        endHereButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditDialog.dismiss();
                endHere(routePoint);
            }
        });

        if (Data.sCopiedRoute.getRoutePoints().lastIndexOf(routePoint) == 0 ||
                Data.sCopiedRoute.getRoutePoints().lastIndexOf(routePoint) == Data.sCopiedRoute.getRoutePoints().size() - 1) {
            instertStartButton.setEnabled(false);
            instertStartButton.setTextColor(Color.argb(80, 50, 50, 50));

            endHereButton.setEnabled(false);
            endHereButton.setTextColor(Color.argb(80, 50, 50, 50));
        }
        mEditDialog.show();
    }

    private void insertStartEnd(RoutePoint routePoint) {

        int idx = Data.sCopiedRoute.getRoutePoints().lastIndexOf(routePoint);

        List<RoutePoint> oldRoute = Data.sCopiedRoute.getRoutePoints();

        List<RoutePoint> newRoute = new ArrayList<>();

        for (int i = idx; i < oldRoute.size(); i++) {

            newRoute.add(oldRoute.get(i));

        }

        for (int i = 1; i < idx; i++) {

            newRoute.add(oldRoute.get(i));

        }

        RoutePoint lastPoint = new RoutePoint();
        lastPoint.setLatitude(newRoute.get(0).getLatitude() - 0.0002d);
        lastPoint.setLongitude(newRoute.get(0).getLongitude() - 0.0002d);

        newRoute.add(lastPoint);

        Data.sCopiedRoute.setRoutePoints(newRoute);

        refreshMap();
    }

    private void endHere(RoutePoint routePoint) {

        int idx = Data.sCopiedRoute.getRoutePoints().lastIndexOf(routePoint);

        List<RoutePoint> oldRoute = Data.sCopiedRoute.getRoutePoints();

        List<RoutePoint> newRoute = new ArrayList<>();

        for (int i = 0; i < idx + 1; i++) {

            newRoute.add(oldRoute.get(i));

        }

        Data.sCopiedRoute.setRoutePoints(newRoute);

        refreshMap();
    }

    public void addFromPoi(final Point poi) {

        /*
         * The issue to workaround: a marker drawn over another one (in the same place) does not
         * cover it for some mysterious reason, so OnMarkerClickListener will be executed for both
         * of them (Why?!). Let's check if the just clicked POI coordinates already exist as
         * a cardinal way point. If so - let's skip this dialog.
         * NOTE: this will not work for overlapping markers of slightly different coordinates.
         */
        boolean pointExists = false;
        for (RoutePoint routePoint : Data.sCopiedRoute.getRoutePoints()) {
            if (routePoint.getLatitude().equals(poi.getLatitude()) && routePoint.getLongitude().equals(poi.getLongitude())) {
                pointExists = true;
                break;
            }
        }
        if (pointExists) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String titleText = getResources().getString(R.string.dialog_poi2wpt);
        String insertText = getResources().getString(R.string.dialog_insert);
        String appendText = getResources().getString(R.string.dialog_append);
        String cancelText = getResources().getString(R.string.dialog_cancel);

        String messageText = "POI-> Way point";
        if (poi.getName() != null) {
            messageText = String.format(getString(R.string.dialog_insert_message), poi.getName());
        }

        builder.setCancelable(true)
                .setTitle(titleText)
                .setMessage(messageText)
                .setIcon(R.drawable.map_poi)
                .setPositiveButton(appendText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        RoutePoint routePoint = new RoutePoint();

                        routePoint.setLatitude(poi.getLatitude());
                        routePoint.setLongitude(poi.getLongitude());

                        Data.sCopiedRoute.addRoutePoint(routePoint);

                        refreshMap();
                    }
                })
                .setNeutralButton(insertText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        double[] dstToRoute = GpxUtils.distanceToRoute(poi, Data.sCopiedRoute);

                        RoutePoint routePoint = new RoutePoint();

                        routePoint.setName(poi.getName());
                        routePoint.setLatitude(poi.getLatitude());
                        routePoint.setLongitude(poi.getLongitude());


                        Data.sCopiedRoute.addRoutePoint((int) dstToRoute[1] + 1, routePoint);
                        refreshMap();

                    }
                })
                .setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        mAddFromPoiDialog = builder.create();

        mAddFromPoiDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                if (Data.sCopiedRoute.getRoutePoints().size() < 2) {
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
                }

            }
        });
        mAddFromPoiDialog.show();
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        startLocationUpdates();
        restoreMapPosition();

        loadSettings();
    }

    @Override
    protected void onPause() {

        super.onPause();
        if(mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        Data.sLastZoom = mMapView.getZoomLevelDouble();
        Data.sLastCenter = new GeoPoint(mMapView.getMapCenter().getLatitude(), mMapView.getMapCenter().getLongitude());

        saveSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_route_editor, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.show_poi).setChecked(showPoi);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.route_reverse:

                new GpxRouteUtils(Data.sCopiedRoute).reverse();
                refreshMap();
                return true;

            case R.id.show_poi:

                showPoi = !showPoi;
                saveSettings();
                refreshMap();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /*
         * Handle the back button
         */
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            /*
             * If data changed
             */
            if (Data.sCopiedRoute.isChanged() && Data.sCopiedRoute.getRoutePoints().size() > 1) {
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.map_warning)
                        .setTitle(R.string.dialog_save_changes_title)
                        .setMessage(R.string.dialog_route_changed_message)

                        .setPositiveButton(R.string.dialog_apply, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (!Data.sCopiedRoute.getRoutePoints().isEmpty()) {

                                    Data.sRoutesGpx.removeRoute(Data.sFilteredRoutes.get(Data.sSelectedRouteIdx));
                                    Data.sRoutesGpx.addRoute(Data.sCopiedRoute);

                                    Data.sSelectedRouteIdx = null; // index might have changed, clear selection
                                }
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.dialog_discard, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                finish();
                            }

                        })
                        .show();
                return true;

            } else {

                finish();
                return true;
            }

        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_UP:

                if (mMapDragged) {

                    mMapDragged = false;

                    refreshMap();
                }
                break;
        }
        return true;

    }
}