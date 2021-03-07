package org.andan.android.connectiq.wormnav;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
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
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.RoutePoint;
import pt.karambola.gpx.util.GpxUtils;

import static org.andan.android.connectiq.wormnav.R.id.osmmap;

/**
 * Route Creator activity created by piotr on 02.05.17.
 */
public class RouteCreatorActivity extends Utils {

    private final String TAG = "Creator";

    private Map<Marker, GeoPoint> markerToCardinalWaypoint;

    private final double MAX_ZOOM_LEVEL = 19.0;
    private final double MIN_ZOOM_LEVEL = 4.0;

    Button pencilButton;
    Button fitButton;
    Button zoomInButton;
    Button zoomOutButton;
    Button modeButton;
    Button saveButton;

    TextView routePrompt;

    AlertDialog mDeleteWayPointDialog;
    AlertDialog mAddFromPoiDialog;

    private MapView mMapView;
    private IMapController mapController;

    private MapEventsReceiver mapEventsReceiver;

    private MyLocationNewOverlay mLocationOverlay;

    private RotationGestureOverlay mRotationGestureOverlay;

    private Map<Marker, Point> markerToPoi;

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
        setContentView(R.layout.activity_route_creator);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationCallback();
        createLocationRequest();

        if (Data.sCardinalGeoPoints == null) {
            Data.sCardinalGeoPoints = new ArrayList<>();
        }

        if (Data.sRoutingProfile == null) {
            Data.sRoutingProfile = Data.MODE_CAR;
        }

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

                Data.sCardinalGeoPoints.add(new GeoPoint(p));
                //clearResults();
                refreshMap();
                return false;
            }
        };

        restoreMapPosition();

        mMapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {

                mMapDragged = true;

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
            mapController.setZoom(3.0);
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

        if (Data.osrmRoute == null || Data.osrmRoute.getRoutePoints().size() == 0) {

            routeOverlay.getOutlinePaint().setColor(Color.parseColor("#006666"));
            routeOverlay.setPoints(Data.sCardinalGeoPoints);

            routePrompt.setText(String.format(getResources().getString(R.string.map_prompt_route), Data.sCardinalGeoPoints.size()));

        } else {

            List<GeoPoint> geoPoints = new ArrayList<>();

            for (int i = 0; i < Data.osrmRoute.getRoutePoints().size(); i++) {
                RoutePoint routePoint = Data.osrmRoute.getRoutePoints().get(i);
                geoPoints.add(new GeoPoint(routePoint.getLatitude(), routePoint.getLongitude()));
            }
            routeOverlay.setColor(Color.parseColor("#0066ff"));
            routeOverlay.setPoints(geoPoints);
            routePrompt.setText(GpxUtils.getRouteNameAnnotated(Data.osrmRoute, Data.sUnitsInUse));
        }

        mMapView.getOverlays().add(routeOverlay);

        markerToCardinalWaypoint = new HashMap<>();

        for (int i = 0; i < Data.sCardinalGeoPoints.size(); i++) {

            GeoPoint geoPoint = Data.sCardinalGeoPoints.get(i);

            Drawable icon = new BitmapDrawable(getResources(), makeMarkerBitmap(this, String.valueOf(i)));

            Marker marker = new Marker(mMapView);
            marker.setPosition(geoPoint);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setDraggable(true);
            marker.setIcon(icon);

            markerToCardinalWaypoint.put(marker, geoPoint);
            mMapView.getOverlays().add(marker);

            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {

                    /*
                     * @osmdroid allows to click multiple markers at a time.
                     * This is to avoid opening a dialog for each clicked one.
                     */
                    if (mDeleteWayPointDialog == null || !mDeleteWayPointDialog.isShowing()) {

                        displayDeleteWaypointDialog(markerToCardinalWaypoint.get(marker));

                    }
                    return false;
                }
            });
            marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    GeoPoint dragged = markerToCardinalWaypoint.get(marker);
                    dragged.setCoords(marker.getPosition().getLatitude(), marker.getPosition().getLongitude());

                    Data.osrmRoute = null;
                    refreshMap();
                }

                @Override
                public void onMarkerDragStart(Marker marker) {

                }
            });

        }
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

        pencilButton = (Button) findViewById(R.id.pencil_button);
        pencilButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //askOsrm(Data.sCardinalGeoPoints);
                getOsrmRoute(Data.sCardinalGeoPoints);
            }
        });

        fitButton = (Button) findViewById(R.id.fit_button);
        fitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Data.sCardinalGeoPoints != null && Data.sCardinalGeoPoints.size() > 1) {
                    mMapView.zoomToBoundingBox(findBoundingBox(Data.sCardinalGeoPoints).increaseByScale(1.1f), false);
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
        modeButton = (Button) findViewById(R.id.mode_button);
        modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (Data.sRoutingProfile) {
                    case Data.MODE_CAR:
                        Data.sRoutingProfile = Data.MODE_BIKE;
                        modeButton.setBackgroundResource(R.drawable.button_cycling);
                        break;
                    case Data.MODE_BIKE:
                        Data.sRoutingProfile = Data.MODE_FOOT;
                        modeButton.setBackgroundResource(R.drawable.button_walking);
                        break;
                    case Data.MODE_FOOT:
                        Data.sRoutingProfile = Data.MODE_CAR;
                        modeButton.setBackgroundResource(R.drawable.button_driving);
                        break;
                }
            }
        });

        saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Data.osrmRoute != null) {
                    Data.sRoutesGpx.addRoute(Data.osrmRoute);

                    Intent i = new Intent(RouteCreatorActivity.this, RoutesBrowserActivity.class);
                    Data.sSelectedRouteIdx = Data.sRoutesGpx.getRoutes().indexOf(Data.osrmRoute);
                    setResult(Data.NEW_ROUTE_ADDED, i);

                    clearResults();

                    finish();
                }
            }
        });

        routePrompt = (TextView) findViewById(R.id.route_prompt);

        final TextView copyright = (TextView) findViewById(R.id.copyright);
        copyright.setMovementMethod(LinkMovementMethod.getInstance());

        final TextView routingBy = (TextView) findViewById(R.id.routing_by);
        routingBy.setPaintFlags(routingBy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        if (Data.sRoutingSource == Data.ROUTING_SRC_MAPQUEST) {

            routingBy.setText(getResources().getString(R.string
                    .credits_routing_mapquest));

            routingBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("http://www.mapquest.com");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });

        } else {

            routingBy.setText(getResources().getString(R.string
                    .credits_routing_osrm));

            routingBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("http://project-osrm.org");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        }
    }

    private void setButtonsState() {

        if (Data.sCardinalGeoPoints != null && Data.sCardinalGeoPoints.size() > 1) {
            pencilButton.setEnabled(true);
            pencilButton.getBackground().setAlpha(255);
        } else {
            pencilButton.setEnabled(false);
            pencilButton.getBackground().setAlpha(100);
        }

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

        /*
         * When the Route Manager main activity (picker) is ready, this button will be adding selected route
         * to Data.sRoutesGpx, and close the Creator.
         */
        if (Data.osrmRoute != null && Data.osrmRoute.getRoutePoints().size() > 0) {
            saveButton.setEnabled(true);
            saveButton.getBackground().setAlpha(255);
        } else {
            saveButton.setEnabled(false);
            saveButton.getBackground().setAlpha(100);
        }

        /*
         * "routeType=..." is unavailable for the OSRM Demo Server.
         * Let's disable the switch if OSRM selected in common settings.
         */
        if (Data.sRoutingSource == Data.ROUTING_SRC_MAPQUEST) {
            modeButton.setEnabled(true);
            modeButton.getBackground().setAlpha(255);
        } else {
            modeButton.setEnabled(false);
            modeButton.getBackground().setAlpha(100);
        }

    }

    private void displayDeleteWaypointDialog(final GeoPoint geoPoint) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getResources().getString(R.string.waypoint_delete))
                .setIcon(R.drawable.map_question)
                .setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.dialog_delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Data.sCardinalGeoPoints.remove(geoPoint);

                        Data.osrmRoute = null;
                        refreshMap();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        mDeleteWayPointDialog = builder.create();
        mDeleteWayPointDialog.show();

    }

    private void displaySettingsDialog() {

        loadSettings();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_settings_creator, null);

        final Spinner routing_spinner = (Spinner) layout.findViewById(R.id.routing_spinner);
        routing_spinner.setEnabled(!Data.sEncodedKey.equals("YOUR_BASE64_ENCODED_KEY_HERE"));

        ArrayAdapter<String> dataAdapterRouting = new ArrayAdapter<>(this, android.R.layout
                .simple_spinner_item, getResources().getStringArray(R.array.settings_routing_array));
        dataAdapterRouting.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routing_spinner.setAdapter(dataAdapterRouting);
        if (Data.sRoutingSource == null) {
            Data.sRoutingSource = Data.ROUTING_SRC_OSRM;
        }
        routing_spinner.setSelection(Data.sRoutingSource);
        routing_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {

                switch (pos) {
                    case 0:
                        Data.sRoutingSource = Data.ROUTING_SRC_OSRM;
                        break;
                    case 1:
                        Data.sRoutingSource = Data.ROUTING_SRC_MAPQUEST;
                        break;
                    default:
                        Data.sRoutingSource = Data.ROUTING_SRC_OSRM;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        builder.setTitle(getResources().getString(R.string.settings_routing_label))
                .setIcon(R.drawable.ico_settings)
                .setCancelable(true)
                .setView(layout)
                .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        saveSettings();
                        setUpButtons();
                        refreshMap();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
        for (GeoPoint geoPoint : Data.sCardinalGeoPoints) {
            if (geoPoint.getLatitude() == poi.getLatitude() && geoPoint.getLongitude() == poi.getLongitude()) {
                pointExists = true;
                break;
            }
        }
        if (pointExists) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String titleText = getResources().getString(R.string.dialog_poi2wpt);
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

                        GeoPoint geoPoint = new GeoPoint(poi.getLatitude(), poi.getLongitude());

                        Data.sCardinalGeoPoints.add(geoPoint);
                        refreshMap();

                    }
                })
                .setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        mAddFromPoiDialog = builder.create();
        mAddFromPoiDialog.show();
    }

    public void clearResults() {
        Data.osrmRoute = null;
        Data.sCardinalGeoPoints = new ArrayList<>();
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
        refreshMap();
    }

    @Override
    protected void onPause() {

        super.onPause();

        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        Data.sLastZoom = mMapView.getZoomLevelDouble();
        Data.sLastCenter = new GeoPoint(mMapView.getMapCenter().getLatitude(), mMapView.getMapCenter().getLongitude());

        saveSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_route_creator, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (Data.sCardinalGeoPoints == null || Data.sCardinalGeoPoints.isEmpty()) {
            menu.findItem(R.id.clear_waypoints).setEnabled(false);
        } else {
            menu.findItem(R.id.clear_waypoints).setEnabled(true);
        }
        menu.findItem(R.id.routing_server).setEnabled(!Data.sMapQuestKey.isEmpty());

        menu.findItem(R.id.tou_mapquest).setEnabled(!Data.sMapQuestKey.isEmpty());

        menu.findItem(R.id.show_poi).setChecked(showPoi);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.show_poi:

                showPoi = !showPoi;
                saveSettings();
                refreshMap();
                return true;

            case R.id.routing_server:

                displaySettingsDialog();
                return true;

            case R.id.clear_waypoints:
                Data.sCardinalGeoPoints = new ArrayList<>();
                Data.osrmRoute = null;
                refreshMap();
                return true;

            case R.id.tou_mapquest:
                Uri uri = Uri.parse("http://hello.mapquest.com/terms-of-use");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;

            case R.id.tou_osrm:
                uri = Uri.parse("https://github.com/Project-OSRM/osrm-backend/wiki/Api-usage-policy");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Road road;

    private void getOsrmRoute(final ArrayList<GeoPoint> waypoints) {

        final RoadManager roadManager;

        if (Data.sRoutingSource == Data.ROUTING_SRC_MAPQUEST) {

            roadManager = new MapQuestRoadManager(Data.sMapQuestKey);

            roadManager.addRequestOption(Data.sRoutingProfile);

        } else {

            roadManager = new OSRMRoadManager(this);
        }

        AsyncTask<Void, Void, Boolean> getHttpRequest = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {

            }

            @Override
            protected Boolean doInBackground(Void... params) {

                road = roadManager.getRoad(waypoints);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {

                if (road != null) {

                    Polyline roadOverlay = RoadManager.buildRoadOverlay(road);

                    Data.osrmRoute = new Route();

                    for (GeoPoint geoPoint : roadOverlay.getPoints()) {

                        RoutePoint routePoint = new RoutePoint();
                        routePoint.setLatitude(geoPoint.getLatitude());
                        routePoint.setLongitude(geoPoint.getLongitude());

                        Data.osrmRoute.addRoutePoint(routePoint);
                    }
                    Data.osrmRoute.setName("Auto_" + String.valueOf(System.currentTimeMillis())
                            .substring(7));

                    /*
                     * MapQuest supports routing types. Lets assign them to the route.
                     */
                    if (Data.sRoutingSource == Data.ROUTING_SRC_MAPQUEST) {

                        switch(Data.sRoutingProfile) {

                            case Data.MODE_BIKE:
                                Data.osrmRoute.setType(getResources().getString(R.string.route_type_bicycle));
                                break;

                            case Data.MODE_FOOT:
                                Data.osrmRoute.setType(getResources().getString(R.string
                                        .route_type_pedestrian));
                                break;

                            default:
                                Data.osrmRoute.setType(getResources().getString(R.string
                                        .route_type_car));
                                break;
                        }

                    } else {
                        Data.osrmRoute.setType("OSRM");
                    }

                    /*
                     * Let's simplify the path so that it had up to 10 route points per kilometer
                     * at maximum error 6 m.
                     */
                    double distance = GpxUtils.lengthOfRoute(Data.osrmRoute);
                    GpxUtils.simplifyRoute(Data.osrmRoute, (int) distance / 50, 2d);

                    refreshMap();

                } else {

                    Toast.makeText(getApplicationContext(), getString(R.string.no_osrm_reponse), Toast.LENGTH_SHORT).show();
                }
            }
        };
        getHttpRequest.execute();
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