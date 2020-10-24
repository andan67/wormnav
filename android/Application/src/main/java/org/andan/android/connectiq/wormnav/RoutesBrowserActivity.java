package org.andan.android.connectiq.wormnav;

import android.Manifest;
import android.app.ActionBar;
//import android.app.AlertDialog;
import androidx.appcompat.app.AlertDialog;
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
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.karambola.commons.collections.ListUtils;
import pt.karambola.gpx.beans.Gpx;
import pt.karambola.gpx.beans.Point;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.RoutePoint;
import pt.karambola.gpx.beans.Track;
import pt.karambola.gpx.io.GpxFileIo;
import pt.karambola.gpx.parser.GpxParserOptions;
import pt.karambola.gpx.predicate.RouteFilter;
import pt.karambola.gpx.util.GpxUtils;

import static android.view.View.GONE;
import static org.andan.android.connectiq.wormnav.R.id.osmmap;

/**
 * Route Picker activity created by piotr on 02.05.17.
 */
public class RoutesBrowserActivity extends Utils {

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    ListView list;

    String[] web = new String[1];

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private final String TAG = RoutesBrowserActivity.class.getName();

    private final double MAX_ZOOM_LEVEL = 19;
    private final double MIN_ZOOM_LEVEL = 4;

    private int filePickerAction;
    private final int ACTION_IMPORT_ROUTES = 1;
    private final int ACTION_CONVERT_TRACKS = 2;

    private final int SAVE_SELECTED_ROUTE = 4;
    private final int SAVE_MULTIPLE_ROUTES = 5;

    private final int REQUEST_CODE_PICK_DIR = 1;
    private final int REQUEST_CODE_PICK_FILE = 2;

    private Button fitButton;
    private Button nextButton;
    private Button previousButton;
    private Button filterButton;
    private Button editButton;

    TextView routePrompt;

    TextView routesSummary;

    private MapView mMapView;
    private IMapController mapController;

    private MapEventsReceiver mapEventsReceiver;

    private MyLocationNewOverlay mLocationOverlay;

    private RotationGestureOverlay mRotationGestureOverlay;

    private List<GeoPoint> mAllGeopoints;

    private int mFilteredRoutesNumber = 0;

    private Gpx gpxOut = new Gpx();
    String fileName = "myfile";

    /**
     * route label marker -> index of selected route
     */
    private Map<Marker, Integer> markerToRouteIdx;

    /**
     * Drawer entry -> route index
     */
    private Map<Integer, Integer> drawerIdxToRouteIdx;

    /**
     * View filtering
     */
    boolean enable_type;
    boolean enable_dst;
    boolean enable_age;

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
        setContentView(R.layout.activity_routes_browser);

        mTitle = mDrawerTitle = getTitle();

        web[0] = getResources().getString(R.string.no_data_loaded);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_routes);

        addDrawerItems(web);
        setupDrawer();

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

                Data.sSelectedRouteIdx = null;
                mMapDragged = true;
                refreshMap(false);

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {

                return false;
            }
        };

        restoreMapPosition();

        mMapView.addMapListener(new MapListener() {
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

    private void refreshMap(boolean zoom_to_fit) {

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

        /*
         * We'll create bounding box around this
         */
        mAllGeopoints = new ArrayList<>();

        final int allRoutesNumber = Data.sRoutesGpx.getRoutes().size();

        Data.sFilteredRoutes = ListUtils.filter(Data.sRoutesGpx.getRoutes(), Data.sViewRouteFilter);

        if (Data.sFilteredRoutes == null || Data.sFilteredRoutes.isEmpty()) {
            Data.sSelectedRouteIdx = null;
        }

        if (updateDrawerItems() != null) {
            addDrawerItems(updateDrawerItems());
        } else {
            addDrawerItems(web);
        }

        mFilteredRoutesNumber = Data.sFilteredRoutes.size();

        markerToRouteIdx = new HashMap<>();

        for (int i = 0; i < mFilteredRoutesNumber; i++) {

            final Route route = Data.sFilteredRoutes.get(i);

            List<RoutePoint> routePoints = route.getRoutePoints();

            int halfWayPoint = routePoints.size() / 2;
            int lastWayPoint = routePoints.size() - 1;

            List<GeoPoint> geoPoints = new ArrayList<>();

            for (int j = 0; j < routePoints.size(); j++) {

                RoutePoint routePoint = routePoints.get(j);
                GeoPoint geoPoint = new GeoPoint(routePoint.getLatitude(), routePoint.getLongitude());
                geoPoints.add(geoPoint);

                if (Data.sSelectedRouteIdx == null) {
                    mAllGeopoints.add(geoPoint);

                    if (j == halfWayPoint) {

                        String name = route.getName() != null ? route.getName() : "?";
                        Drawable icon = new BitmapDrawable(getResources(), makeRouteNameBitmap(this, name));

                        Marker marker = new Marker(mMapView);
                        markerToRouteIdx.put(marker, i);
                        marker.setPosition(geoPoint);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marker.setDraggable(false);
                        marker.setIcon(icon);
                        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {

                                /*
                                 * Click the route label marker to select the route
                                 */
                                Data.sSelectedRouteIdx = markerToRouteIdx.get(marker);
                                refreshMap(false);
                                return true;
                            }
                        });

                        mMapView.getOverlays().add(marker);
                    }

                    if (j == lastWayPoint) {

                        Marker marker = new Marker(mMapView);
                        markerToRouteIdx.put(marker, i);
                        marker.setPosition(geoPoint);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marker.setDraggable(false);
                        marker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.route_end, null));
                        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {

                                /*
                                 * Click the route label marker to select the route
                                 */
                                Data.sSelectedRouteIdx = markerToRouteIdx.get(marker);
                                refreshMap(false);
                                return true;
                            }
                        });

                        mMapView.getOverlays().add(marker);

                    }

                } else {

                    if (i == Data.sSelectedRouteIdx) {
                        mAllGeopoints.add(geoPoint);

                        if (j == halfWayPoint) {

                            String name = route.getName() != null ? route.getName() : "?";
                            Drawable icon = new BitmapDrawable(getResources(), makeRouteNameBitmap(this, name));

                            Marker marker = new Marker(mMapView);
                            marker.setPosition(geoPoint);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            marker.setDraggable(false);
                            marker.setIcon(icon);
                            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker, MapView mapView) {
                                    return false;
                                }
                            });
                            mMapView.getOverlays().add(marker);
                        }

                        if (j == lastWayPoint) {

                            Marker marker = new Marker(mMapView);
                            markerToRouteIdx.put(marker, i);
                            marker.setPosition(geoPoint);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            marker.setDraggable(false);
                            marker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.route_end, null));
                            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker, MapView mapView) {
                                    return false;
                                }
                            });
                            mMapView.getOverlays().add(marker);
                        }
                    }
                }
            }

            final Polyline routeOverlay = new Polyline();
            routeOverlay.setPoints(geoPoints);

            if (Data.sSelectedRouteIdx != null) {

                if (i == Data.sSelectedRouteIdx) {

                    routeOverlay.setColor(Color.parseColor("#0099ff"));

                } else {

                    routeOverlay.setColor(Color.parseColor("#11000000"));
                }

            } else {

                routeOverlay.setColor(typeColors[i % N_COLOURS]);
            }
            routeOverlay.setWidth(15);

            mMapView.getOverlays().add(routeOverlay);
        }

        if (Data.sSelectedRouteIdx != null) {
            routePrompt.setText(GpxUtils.getRouteNameAnnotated(Data.sFilteredRoutes.get(Data.sSelectedRouteIdx), Data.sUnitsInUse));
        } else {
            routePrompt.setText(getResources().getString(R.string.route_edit_prompt));
        }
        routesSummary.setText(String.format(getResources().getString(R.string.x_of_y_routes), mFilteredRoutesNumber, allRoutesNumber));

        if (zoom_to_fit && mAllGeopoints.size() > 0) {
            mMapView.zoomToBoundingBox(findBoundingBox(mAllGeopoints).increaseByScale(1.1f), false);
        }

        if (showPoi) {
            drawPoi();
        }

        mMapView.invalidate();
        setButtonsState();
    }

    private void refreshMap() {
        refreshMap(true);
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
            Drawable icon = new BitmapDrawable(getResources(), makeMarkerBitmap(this, displayName, color, 180));

            Marker marker = new Marker(mMapView);
            marker.setPosition(markerPosition);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setDraggable(false);
            marker.setIcon(icon);

            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    return false;
                }
            });

            if (mMapViewBoundingBox.contains(markerPosition)) {
                mMapView.getOverlays().add(marker);
            }
        }
    }

    private void setUpButtons() {

        locationButton = (Button) findViewById(R.id.picker_location_button);
        locationButton.setEnabled(false);
        locationButton.getBackground().setAlpha(0);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapController.setZoom(18.0);
                mapController.setCenter(Data.sCurrentPosition);
                setButtonsState();
            }
        });

        fitButton = (Button) findViewById(R.id.picker_fit_button);
        fitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAllGeopoints != null && mAllGeopoints.size() > 0) {
                    mMapView.zoomToBoundingBox(findBoundingBox(mAllGeopoints).increaseByScale(1.1f), false);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_routes_in_view), Toast.LENGTH_SHORT).show();
                }
                mMapDragged = true;
                refreshMap();
            }
        });

        nextButton = (Button) findViewById(R.id.picker_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Data.sSelectedRouteIdx == null) {
                    Data.sSelectedRouteIdx = 0;
                } else {
                    if (Data.sSelectedRouteIdx < mFilteredRoutesNumber - 1) {
                        Data.sSelectedRouteIdx++;
                    } else {
                        Data.sSelectedRouteIdx = 0;
                    }
                }
                mMapDragged = true;
                refreshMap();
            }
        });
        previousButton = (Button) findViewById(R.id.picker_previous_button);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Data.sSelectedRouteIdx == null) {
                    Data.sSelectedRouteIdx = 0;
                } else {
                    if (Data.sSelectedRouteIdx > 0) {
                        Data.sSelectedRouteIdx--;
                    } else {
                        Data.sSelectedRouteIdx = mFilteredRoutesNumber - 1;
                    }
                }
                mMapDragged = true;
                refreshMap();
            }
        });

        filterButton = (Button) findViewById(R.id.picker_filter_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayFilterDialog();
            }
        });

        editButton = (Button) findViewById(R.id.picker_edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Data.sCopiedRoute = copyRoute(Data.sFilteredRoutes.get(Data.sSelectedRouteIdx));
                Data.sCopiedRoute.resetIsChanged();

                Intent i = new Intent(RoutesBrowserActivity.this, RouteEditorActivity.class);
                startActivityForResult(i, 90);
            }
        });
        editButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                displayEditDialog();
                return false;
            }
        });

        routesSummary = (TextView) findViewById(R.id.routes_summary);

        routePrompt = (TextView) findViewById(R.id.picker_route_prompt);

        final TextView copyright = (TextView) findViewById(R.id.copyright);
        copyright.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setButtonsState() {

        if (mFilteredRoutesNumber > 0) {
            nextButton.setEnabled(true);
            nextButton.getBackground().setAlpha(255);
        } else {
            nextButton.setEnabled(false);
            nextButton.getBackground().setAlpha(100);
        }

        if (mFilteredRoutesNumber > 0) {
            previousButton.setEnabled(true);
            previousButton.getBackground().setAlpha(255);
        } else {
            previousButton.setEnabled(false);
            previousButton.getBackground().setAlpha(100);
        }

        /*
         * Open a dialog to select a route by name
         */
        if (Data.sRoutesGpx != null && Data.sRoutesGpx.getRoutes().size() > 0) {
            filterButton.setEnabled(true);
            filterButton.getBackground().setAlpha(255);
        } else {
            filterButton.setEnabled(false);
            filterButton.getBackground().setAlpha(100);
        }

        if (!Data.sFilteredRoutes.isEmpty() && Data.sSelectedRouteIdx != null) {
            editButton.setEnabled(true);
            editButton.getBackground().setAlpha(255);
        } else {
            editButton.setEnabled(false);
            editButton.getBackground().setAlpha(100);
        }

        if (Data.sViewRouteFilter.isEnabled()) {
            fitButton.setBackgroundResource(R.drawable.map_filter_on);
        } else {
            fitButton.setBackgroundResource(R.drawable.map_fit);
        }

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
        mMapDragged = true;
        loadSettings();
        refreshMap(false);
    }

    @Override
    protected void onPause() {

        super.onPause();
        if(mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);

        Data.sLastZoom = mMapView.getZoomLevelDouble();
        Data.sLastCenter = new GeoPoint(mMapView.getMapCenter().getLatitude(), mMapView.getMapCenter().getLongitude());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_routes_browser, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        /*
         * We'll enable/disable menu options here
         */
        menu.findItem(R.id.routes_delete_selected).setEnabled(Data.sSelectedRouteIdx != null);
        menu.findItem(R.id.routes_send_selected_to_device).setEnabled(Data.sSelectedRouteIdx != null);
        menu.findItem(R.id.routes_edit_selected).setEnabled(Data.sSelectedRouteIdx != null);
        menu.findItem(R.id.routes_simplify_selected).setEnabled(Data.sSelectedRouteIdx != null);
        menu.findItem(R.id.routes_clear).setEnabled(Data.sRoutesGpx.getRoutes().size() > 0);
        menu.findItem(R.id.routes_simplify_merge_routes).setEnabled(Data.sRoutesGpx.getRoutes().size() > 0);
        menu.findItem(R.id.export).setEnabled(Data.sRoutesGpx.getRoutes().size() > 0);

        menu.findItem(R.id.show_poi).setChecked(showPoi);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final String path = Data.lastImportedFileFullPath.length()>0? getParentFromFullPath( Data.lastImportedFileFullPath):Data.defaultDirectoryPath;
        Intent fileExploreIntent = new Intent(
                FileBrowserActivity.INTENT_ACTION_SELECT_FILE,
                null,
                this,
                FileBrowserActivity.class
        );

        Intent i;

        switch (item.getItemId()) {

            case R.id.routes_new_route:

                Data.sCopiedRoute = new Route();
                Data.sCopiedRoute.setName(getResources().getString(R.string.unnamed));
                Data.sCopiedRoute.resetIsChanged();
                Data.sSelectedRouteIdx = null;

                i = new Intent(RoutesBrowserActivity.this, RouteEditorActivity.class);
                startActivityForResult(i, 90);

                return true;

            case R.id.routes_new_autorute:
                i = new Intent(RoutesBrowserActivity.this, RouteCreatorActivity.class);
                startActivityForResult(i, 90);
                return true;

            case R.id.routes_edit_selected:

                displayEditDialog();
                return true;

            case R.id.routes_simplify_selected:
                Data.sCopiedRoute = copyRoute(Data.sFilteredRoutes.get(Data.sSelectedRouteIdx));
                Data.sCopiedRoute.resetIsChanged();

                i = new Intent(RoutesBrowserActivity.this, RouteOptimizerActivity.class);
                startActivityForResult(i, 90);
                return true;


            case R.id.routes_simplify_merge_routes:
                displayConvertDialog(null);
                return true;

            case R.id.routes_delete_selected:

                final Route route = Data.sFilteredRoutes.get(Data.sSelectedRouteIdx);
                String deleteMessage;
                if (route.getName() != null) {
                    String deleteMessageFormat = getResources().getString(R.string.dialog_delete_route_message);
                    deleteMessage = String.format(deleteMessageFormat, route.getName());
                } else {
                    deleteMessage = getResources().getString(R.string.about_to_delete_route);
                }
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.map_warning)
                        .setTitle(R.string.dialog_delete_route)
                        .setMessage(deleteMessage)
                        .setPositiveButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Data.sRoutesGpx.removeRoute(route);
                                Data.sSelectedRouteIdx = null;
                                refreshMap();

                            }

                        })
                        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }

                        })
                        .show();
                return true;

            case R.id.routes_clear:

                if (Data.sRoutesGpx.getRoutes().size() > 0) {
                    clearRoutes();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_routes_to_clear), Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.routes_import_routes:

                filePickerAction = ACTION_IMPORT_ROUTES;

                fileExploreIntent.putExtra(
                        FileBrowserActivity.startDirectoryParameter,
                        path
                );
                startActivityForResult(
                        fileExploreIntent,
                        REQUEST_CODE_PICK_FILE
                );
                return true;

            case R.id.convert_tracks:

                filePickerAction = ACTION_CONVERT_TRACKS;

                fileExploreIntent.putExtra(
                        FileBrowserActivity.startDirectoryParameter,
                        path
                );
                startActivityForResult(
                        fileExploreIntent,
                        REQUEST_CODE_PICK_FILE
                );
                return true;

            case R.id.export:

                filePickerAction = SAVE_MULTIPLE_ROUTES;
                displayExportMultipleDialog();
                return true;

            case R.id.show_poi:

                showPoi = !showPoi;
                saveSettings();
                refreshMap();
                return true;

            case R.id.routes_send_selected_to_device:

                final Route routeSelected = Data.sFilteredRoutes.get(Data.sSelectedRouteIdx);
                SendToDeviceUtility.startDeviceBrowserActivity(RoutesBrowserActivity.this, routeSelected);
                return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addDrawerItems(String items[]) {

        CustomListNoIcons adapter = new
                CustomListNoIcons(RoutesBrowserActivity.this, items);
        list = (ListView) findViewById(R.id.routesNavList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (drawerIdxToRouteIdx != null && !drawerIdxToRouteIdx.isEmpty()) {
                    Data.sSelectedRouteIdx = drawerIdxToRouteIdx.get(position);
                    mMapDragged = true;
                    refreshMap();
                }
            }
        });
    }

    private void setupDrawer() {
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerToggle.setDrawerIndicatorEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_FILE) {
            Log.d(TAG, "RoutesBrowser resultCode:" + resultCode);
            if (resultCode == RESULT_OK) {

                String fileFullPath = data.getStringExtra(
                        FileBrowserActivity.returnFileParameter);
                Data.lastImportedFileFullPath = fileFullPath;
                saveSettings();
                switch (filePickerAction) {

                    case ACTION_IMPORT_ROUTES:
                        displayImportRoutesDialog(fileFullPath);
                        break;

                    case ACTION_CONVERT_TRACKS:
                        displayConvertDialog(fileFullPath);
                        break;

                    case SAVE_SELECTED_ROUTE:
                        // saveSelectedRoutes(fileFullPath);
                        break;

                    case SAVE_MULTIPLE_ROUTES:
                        saveSelectedRoutes(fileFullPath);
                        break;

                    default:
                        break;
                }

            } else {
                /*
                Toast.makeText(
                        this,
                        getResources().getString(R.string.no_file_selected),
                        Toast.LENGTH_LONG).show();
                        */
            }

        } else {

            if (resultCode == Data.NEW_ROUTE_ADDED) {

                refreshMap();
                displayEditDialog();

            }
        }
    }

    private void displayFilterDialog() {

        final List<String> rteTypes = GpxUtils.getDistinctRouteTypes(Data.sRoutesGpx.getRoutes());

        // just used to build the multichoice selector
        final String[] all_types = rteTypes.toArray(new String[rteTypes.size()]);

        final boolean[] selections = new boolean[rteTypes.size()];

        for (int i = 0; i < rteTypes.size(); i++) {
            if (Data.sViewRouteFilter.getAcceptedTypes().contains(rteTypes.get(i))) {
                selections[i] = true;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.routes_filter_dialog, null);

        final EditText dstStartMin = (EditText) layout.findViewById(R.id.route_distance_min);
        if (Data.sViewRouteFilter.getDistanceMin() != null) {
            double dst_min = Data.sViewRouteFilter.getDistanceMin() / 1000;
            dstStartMin.setText(String.valueOf(dst_min));
        } else {
            dstStartMin.setText("");
        }

        final EditText dstStartMax = (EditText) layout.findViewById(R.id.route_distance_max);
        if (Data.sViewRouteFilter.getDistanceMax() != null) {
            double dst_max = Data.sViewRouteFilter.getDistanceMax() / 1000;
            dstStartMax.setText(String.valueOf(dst_max));
        } else {
            dstStartMax.setText("");
        }

        final EditText lengthMin = (EditText) layout.findViewById(R.id.route_length_min);
        if (Data.sViewRouteFilter.getLengthMin() != null) {
            Double length_min = Data.sViewRouteFilter.getLengthMin() / 1000;
            lengthMin.setText(String.valueOf(length_min));
        } else {
            lengthMin.setText("");
        }

        final EditText lengthMax = (EditText) layout.findViewById(R.id.route_length_max);
        if (Data.sViewRouteFilter.getLengthMax() != null) {
            Double length_max = Data.sViewRouteFilter.getLengthMax() / 1000;
            lengthMax.setText(String.valueOf(length_max));
        } else {
            lengthMax.setText("");
        }

        final CheckBox dstCheckBox = (CheckBox) layout.findViewById(R.id.route_filter_distance_on);

        if (Data.sCurrentPosition == null) {
            dstCheckBox.setText(getResources().getString(R.string.location_unavailable));
            dstCheckBox.setChecked(false);
            dstCheckBox.setEnabled(false);
            enable_dst = false;
        } else {

            dstCheckBox.setChecked(Data.sViewRouteFilter.isDistanceFilterEnabled());
            dstCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    enable_dst = isChecked;
                    dstStartMin.setEnabled(isChecked);
                    dstStartMax.setEnabled(isChecked);
                }
            });
        }

        final CheckBox lengthCheckBox = (CheckBox) layout.findViewById(R.id.route_filter_length_on);
        lengthCheckBox.setChecked(Data.sViewRouteFilter.isLengthFilterEnabled());
        lengthCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enable_age = isChecked;
                lengthMin.setEnabled(isChecked);
                lengthMax.setEnabled(isChecked);
            }
        });

        String dialogTitle = getResources().getString(R.string.dialog_routes_filter_title);
        String okText = getResources().getString(R.string.dialog_filter_set);
        String clearText = getResources().getString(R.string.dialog_filter_clear);
        String cancelText = getResources().getString(R.string.dialog_cancel);
        builder.setTitle(dialogTitle)
                .setIcon(R.drawable.map_filter)
                .setCancelable(true)
                .setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNeutralButton(clearText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // clear current filters
                        Data.sViewRouteFilter = new RouteFilter();
                        Data.sSelectedRouteIdx = null;
                        mMapDragged = true;
                        refreshMap();

                    }
                })
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Data.sSelectedRouteTypes = new ArrayList<>();

                        for (int i = 0; i < selections.length; i++) {
                            if (selections[i]) {
                                Data.sSelectedRouteTypes.add(rteTypes.get(i));
                            }
                        }

                        if (!dstStartMin.getText().toString().isEmpty()) {
                            Data.sDstStartMinValue = Double.valueOf(dstStartMin.getText().toString()) * 1000;
                        } else {
                            Data.sDstStartMinValue = null;
                        }

                        if (!dstStartMax.getText().toString().isEmpty()) {
                            Data.sDstStartMaxValue = Double.valueOf(dstStartMax.getText().toString()) * 1000;
                        } else {
                            Data.sDstStartMaxValue = null;
                        }

                        if (!lengthMin.getText().toString().isEmpty()) {
                            Data.sLengthMinValue = Double.valueOf(lengthMin.getText().toString()) * 1000;
                        } else {
                            Data.sLengthMinValue = null;
                        }

                        if (!lengthMax.getText().toString().isEmpty()) {
                            Data.sLengthMaxValue = Double.valueOf(lengthMax.getText().toString()) * 1000;
                        } else {
                            Data.sLengthMaxValue = null;
                        }

                        final CheckBox typeCheckBox = (CheckBox) layout.findViewById(R.id.route_filter_types_on);
                        if (typeCheckBox.isChecked()) {
                            Data.sViewRouteFilter.enableTypeFilter(Data.sSelectedRouteTypes);
                        } else {
                            Data.sViewRouteFilter.disableTypeFilter();
                        }

                        final CheckBox dstCheckBox = (CheckBox) layout.findViewById(R.id.route_filter_distance_on);
                        if (dstCheckBox.isChecked()) {
                            Data.sViewRouteFilter.enableDistanceFilter(Data.sCurrentPosition.getLatitude(), Data.sCurrentPosition.getLongitude(),
                                    Data.sCurrentPosition.getAltitude(), Data.sDstStartMinValue, Data.sDstStartMaxValue);

                        } else {
                            Data.sViewRouteFilter.disableDistanceFilter();
                        }

                        final CheckBox lengthCheckBox = (CheckBox) layout.findViewById(R.id.route_filter_length_on);
                        if (lengthCheckBox.isChecked()) {
                            Data.sViewRouteFilter.enableLengthFilter(Data.sLengthMinValue, Data.sLengthMaxValue);
                        } else {
                            Data.sViewRouteFilter.disableLengthFilter();
                        }

                        Data.sSelectedRouteIdx = null;

                        mMapDragged = true;
                        refreshMap(true);

                    }
                });

        builder.setMultiChoiceItems(all_types, selections, new DialogInterface.OnMultiChoiceClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1, boolean arg2) {

                selections[arg1] = arg2;

                final CheckBox typeCheckBox = (CheckBox) layout.findViewById(R.id.route_filter_types_on);

                int selected_types_counter = 0;
                for (int i = 0; i < selections.length; i++) {
                    if (selections[i]) {
                        selected_types_counter++;
                    }
                }

                String display = getString(R.string.dialog_filter_type) + " " + String.format(getString(R.string.dialog_types_of_types), selected_types_counter, all_types.length);
                typeCheckBox.setText(display);

            }
        });
        builder.setView(layout);

        final AlertDialog alert = builder.create();

        alert.show();

        dstStartMax.setEnabled(Data.sViewRouteFilter.isDistanceFilterEnabled());
        dstStartMin.setEnabled(Data.sViewRouteFilter.isDistanceFilterEnabled());

        lengthMin.setEnabled(Data.sViewRouteFilter.isLengthFilterEnabled());
        lengthMax.setEnabled(Data.sViewRouteFilter.isLengthFilterEnabled());

        alert.getListView().setEnabled(Data.sViewRouteFilter.isTypeFilterEnabled());

        final CheckBox typeCheckBox = (CheckBox) layout.findViewById(R.id.route_filter_types_on);
        typeCheckBox.setChecked(Data.sViewRouteFilter.isTypeFilterEnabled());

        int selected_types_counter = 0;
        for (int i = 0; i < selections.length; i++) {
            if (selections[i]) {
                selected_types_counter++;
            }
        }

        String display = getString(R.string.dialog_filter_type) + " " + String.format(getString(R.string.dialog_types_of_types), selected_types_counter, all_types.length);
        typeCheckBox.setText(display);

        typeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enable_type = isChecked;
                if (isChecked) {
                    alert.getListView().setEnabled(true);
                    alert.getListView().setAlpha(1f);
                } else {
                    alert.getListView().setEnabled(false);
                    alert.getListView().setAlpha(0.5f);
                }
            }
        });
    }

    private String[] updateDrawerItems() {

        if (Data.sRoutesGpx.getRoutes().size() == 0 || Data.sFilteredRoutes.size() == 0) {
            return null;
        }


        Route[] sortedRoutesArray = new Route[Data.sFilteredRoutes.size()];
        sortedRoutesArray = Data.sFilteredRoutes.toArray(sortedRoutesArray);

        Arrays.sort(sortedRoutesArray, Data.rteComparator);

        List<String> gpxRteDisplayNames = new ArrayList<>();

        drawerIdxToRouteIdx = new HashMap<>();
        for (int i = 0; i < sortedRoutesArray.length; i++) {

            Route route = sortedRoutesArray[i];

            gpxRteDisplayNames.add(route.getName());

            drawerIdxToRouteIdx.put(i, Data.sFilteredRoutes.indexOf(route));
        }

        List<String> allNames = new ArrayList<>();
        allNames.addAll(gpxRteDisplayNames);

        String[] drawer_entries = new String[allNames.size()];
        drawer_entries = allNames.toArray(drawer_entries);

        return drawer_entries;
    }

    private void displayEditDialog() {

        if (Data.sFilteredRoutes.isEmpty() || Data.sSelectedRouteIdx == null) {

            return;
        }

        final Route picked_route = Data.sFilteredRoutes.get(Data.sSelectedRouteIdx);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View routeEditLayout = inflater.inflate(R.layout.route_edit_dialog, null);

        final EditText editName = (EditText) routeEditLayout.findViewById(R.id.route_name_edit);
        editName.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(99)
        });

        final EditText editNumber = (EditText) routeEditLayout.findViewById(R.id.route_number_edit);
        final EditText editType = (EditText) routeEditLayout.findViewById(R.id.route_type_edit);
        final EditText editDesc = (EditText) routeEditLayout.findViewById(R.id.route_description_edit);

        final Spinner spinner = (Spinner) routeEditLayout.findViewById(R.id.route_type_spinner);
        final List<String> rteTypes = GpxUtils.getDistinctRouteTypes(Data.sRoutesGpx.getRoutes());
        rteTypes.add(0, getResources().getString(R.string.type));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, rteTypes);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
        setUpSpinnerListener(spinner, editType);

        if (picked_route.getNumber() != null) {
            editNumber.setText(String.valueOf(picked_route.getNumber()));
        } else {
            editNumber.setText(String.valueOf(GpxUtils.getRoutesMaxNumber(Data.sRoutesGpx) + 1));
        }

        editName.setText(picked_route.getName());
        if (picked_route.getType() != null) editType.setText(picked_route.getType());

        if (picked_route.getDescription() != null) editDesc.setText(picked_route.getDescription());

        String dialogTitle = getResources().getString(R.string.picker_edit_dialog_title);
        String okText = getResources().getString(R.string.picker_edit_apply);

        String editText = getResources().getString(R.string.dialog_edit_points);
        String cancelText = getResources().getString(R.string.dialog_cancel);

        builder.setTitle(dialogTitle)
                .setIcon(R.drawable.map_edit)
                .setView(routeEditLayout)
                .setCancelable(true)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                        if (editName.getText().toString().isEmpty()) {
                            editName.setText(new Date().toString());
                        }

                        String name = editName.getText().toString();
                        if (!name.isEmpty()) {
                            if (name.length() > 99) name = name.substring(0, 100);
                            picked_route.setName(name);

                        } else {
                            picked_route.setName(null);
                        }

                        if (!editNumber.getText().toString().isEmpty()) {
                            picked_route.setNumber(Integer.valueOf(editNumber.getText().toString()));
                        } else {
                            picked_route.setNumber(null);
                        }

                        if (!editDesc.getText().toString().isEmpty()) {
                            picked_route.setDescription(editDesc.getText().toString().trim());
                        } else {
                            picked_route.setDescription(null);
                        }

                        if (!editType.getText().toString().isEmpty()) {
                            picked_route.setType(editType.getText().toString().trim());
                        } else {
                            picked_route.setType(null);
                        }

                        // Change time of the 1st waypoint to avoid purging the route
                        List<RoutePoint> rtePts = picked_route.getRoutePoints();
                        RoutePoint firstRtePt = rtePts.get(0);
                        firstRtePt.setTime(new Date());
                        picked_route.setRoutePoints(rtePts);

                        routePrompt.setText(GpxUtils.getRouteNameAnnotated(picked_route, Data.sUnitsInUse));

                        refreshMap();

                    }
                })
                .setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNeutralButton(editText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        /*
                         * We'll edit a copy of selected route!
                         */
                        Data.sCopiedRoute = copyRoute(Data.sFilteredRoutes.get(Data.sSelectedRouteIdx));
                        Data.sCopiedRoute.resetIsChanged();

                        Intent i = new Intent(RoutesBrowserActivity.this, RouteEditorActivity.class);
                        startActivityForResult(i, 90);

                    }
                });

        final AlertDialog alert = builder.create();

        if (alert.getWindow() != null) {
            alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        alert.show();
    }

    private void setUpSpinnerListener(Spinner spinner, final EditText edit_text) {

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {

                if (pos != 0) {
                    String item = adapterView.getItemAtPosition(pos).toString();
                    edit_text.setText(item);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void displayImportRoutesDialog(final String path_to_file) {

        Data.sSelectedRouteIdx = null;

        /*
         * Check if the file contains routes
         */
        Gpx gpxIn = GpxFileIo.parseIn(path_to_file, GpxParserOptions.ONLY_ROUTES);

        if (gpxIn == null) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_routes_not_gpx), Toast.LENGTH_LONG).show();
            return;
        }

        final List<Route> sortedRoutes = new ArrayList<>();
        //final List<String> sortedRteNames = GpxUtils.getRouteNamesSortedAlphabeticaly(gpxIn.getRoutes(), sortedRoutes);
        final List<String> sortedRteNames = GpxUtils.getRouteNamesUnsorted_LengthTypeArity(gpxIn.getRoutes(), Data.sUnitsInUse, sortedRoutes);

        if (sortedRteNames.isEmpty()) {
            /*
             * No routes found, don't show the dialog
             */
            Toast.makeText(RoutesBrowserActivity.this, getResources().getString(R.string.no_named_routes), Toast.LENGTH_SHORT).show();

        } else {

            final List<String> allNames = new ArrayList<>();
            allNames.addAll(sortedRteNames);

            String[] menu_entries = new String[allNames.size()];
            menu_entries = allNames.toArray(menu_entries);

            final boolean selected_values[] = new boolean[allNames.size()];

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            String dialogTitle = getResources().getString(R.string.dialog_select_routes_import);
            String buttonAll = getResources().getString(R.string.dialog_all);
            String buttonSelected = getResources().getString(R.string.dialog_selected);
            String buttonCancel = getResources().getString(R.string.dialog_cancel);

            builder.setTitle(dialogTitle)
                    .setIcon(R.drawable.ico_pick_many)
                    .setCancelable(true)

                    .setNeutralButton(buttonSelected, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            List<String> selectedNames = new ArrayList<>();

                            for (int i = 0; i < selected_values.length; i++) {

                                if (selected_values[i]) {
                                    selectedNames.add(allNames.get(i));
                                }
                            }

                            if (selectedNames.size() == 0) {

                                Toast.makeText(RoutesBrowserActivity.this, getResources().getString(R.string.no_routes_selected), Toast.LENGTH_SHORT).show();

                            } else {

                                ArrayList<Route> gpxRoutesPickedByUser = new ArrayList<>();

                                for (String nameOfGPXroutePickedByUser : selectedNames) {

                                    int idxOfRoute = sortedRteNames.indexOf(nameOfGPXroutePickedByUser);
                                    gpxRoutesPickedByUser.add(sortedRoutes.get(idxOfRoute));
                                }

                                Data.sRoutesGpx.addRoutes(gpxRoutesPickedByUser);

                                int purged_routes = GpxUtils.purgeRoutesOverlapping(Data.sRoutesGpx);

                                if (purged_routes != 0) {

                                    Toast.makeText(getApplicationContext(), getString(R.string.removed) + purged_routes + " " + getString(R.string.duplicates), Toast.LENGTH_SHORT).show();

                                } else {

                                    Toast.makeText(RoutesBrowserActivity.this, gpxRoutesPickedByUser.size() + " " + getString(R.string.routes_imported), Toast.LENGTH_LONG).show();
                                }
                                refreshMap();
                            }

                        }
                    })
                    .setNegativeButton(buttonCancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setPositiveButton(buttonAll, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            List<String> selectedNames = new ArrayList<>();
                            selectedNames.addAll(allNames);

                            ArrayList<Route> gpxRoutesPickedByUser = new ArrayList<>();

                            for (String nameOfGPXroutePickedByUser : selectedNames) {

                                int idxOfRoute = sortedRteNames.indexOf(nameOfGPXroutePickedByUser);
                                gpxRoutesPickedByUser.add(sortedRoutes.get(idxOfRoute));
                            }

                            Data.sRoutesGpx.addRoutes(gpxRoutesPickedByUser);

                            int purged_routes = GpxUtils.purgeRoutesOverlapping(Data.sRoutesGpx);

                            if (purged_routes != 0) {

                                Toast.makeText(getApplicationContext(), getString(R.string.removed) + purged_routes + " " + getString(R.string.duplicates), Toast.LENGTH_SHORT).show();

                            } else {

                                Toast.makeText(RoutesBrowserActivity.this, gpxRoutesPickedByUser.size() + " " + getString(R.string.routes_imported), Toast.LENGTH_LONG).show();
                            }
                            refreshMap();
                        }
                    });

            builder.setMultiChoiceItems(menu_entries, selected_values, new DialogInterface.OnMultiChoiceClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1, boolean arg2) {

                    selected_values[arg1] = arg2;
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void displayConvertDialog(final String path_to_file) {

        final List<Route> importedRoutes = new ArrayList<>();

        // import tracks from file and convert into routes
        if(path_to_file != null) {
            Gpx gpxOnlyTrks = GpxFileIo.parseIn(path_to_file, GpxParserOptions.ONLY_TRACKS);

            List<Track> tracksIn;
            try {
                tracksIn = gpxOnlyTrks.getTracks();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.no_tracks_not_gpx), Toast.LENGTH_LONG).show();
                return;
            }

            Log.d(TAG, "tracksIn.size():" + tracksIn.size());


            if (gpxOnlyTrks.getTracks().size() == 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.no_tracks_not_gpx), Toast.LENGTH_LONG).show();
                return;
            }

            for (Track track : tracksIn) {

                importedRoutes.add(Utils.convertTrackToRoute(track));
            }
        } else {
            importedRoutes.addAll(Data.sRoutesGpx.getRoutes());
        }

        final List<Route> sortedRoutes = new ArrayList<>();
        //final List<String> gpxRteDisplayNames = GpxUtils.getRouteNamesSortedAlphabeticaly(importedRoutes, sortedRoutes);
        final List<String> gpxRteDisplayNames = GpxUtils.getRouteNamesUnsorted_LengthTypeArity(importedRoutes, Data.sUnitsInUse, sortedRoutes);

        if (gpxRteDisplayNames != null && gpxRteDisplayNames.isEmpty()) {
            Toast.makeText(RoutesBrowserActivity.this, getString(R.string.no_named_tracks), Toast.LENGTH_SHORT).show();
        }
        else {

            final List<String> allNames = new ArrayList<>();
            if (gpxRteDisplayNames != null) {
                allNames.addAll(gpxRteDisplayNames);
            }

            String[] menu_entries = new String[allNames.size()];
            menu_entries = allNames.toArray(menu_entries);

            final boolean selected_values[] = new boolean[allNames.size()];

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);

            LayoutInflater inflater = getLayoutInflater();
            final View layout = inflater.inflate(R.layout.convert_dialog, null);

            final EditText maxWptEditText = (EditText) layout.findViewById(R.id.reduceMaxPoints);

            final EditText maxError = (EditText) layout.findViewById(R.id.reduceMaxError);

            final CheckBox reduceCheckBox = (CheckBox) layout.findViewById(R.id.reduceCheckbox);
            if(path_to_file != null) {
                reduceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        maxWptEditText.setEnabled(isChecked);
                        maxError.setEnabled(isChecked);
                    }
                });
            } else {
                layout.findViewById(R.id.reduceRow).setVisibility(GONE);
                reduceCheckBox.setVisibility(GONE);
                reduceCheckBox.setChecked(false);
            }

            final EditText mergeNameEditText = (EditText) layout.findViewById(R.id.mergeName);
            final CheckBox mergeCheckbox = (CheckBox) layout.findViewById(R.id.mergeCheckbox);
            if(path_to_file != null) {
                mergeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mergeNameEditText.setEnabled(isChecked);
                    }
                });
            } else {
                mergeCheckbox.setChecked(true);
                mergeCheckbox.setEnabled(false);
                mergeNameEditText.setEnabled(true);
            }

            final CheckBox deleteSourceCheckBox = (CheckBox) layout.findViewById(R.id.deleteSourceCheckbox);
            deleteSourceCheckBox.setVisibility(GONE);

            String dialogTitle = path_to_file!=null?
                    getResources().getString(R.string.dialog_select_tracks_import):getResources().getString(R.string.dialog_select_routes_optimize);
            String buttonAll = getResources().getString(R.string.dialog_all);
            String buttonSelected = getResources().getString(R.string.dialog_selected);
            String buttonCancel = getResources().getString(R.string.dialog_cancel);

            builder.setTitle(dialogTitle)
                    .setView(layout)
                    .setIcon(R.drawable.ico_pick_many)
                    .setCancelable(true)
                    .setNeutralButton(buttonSelected, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            List<String> selectedNames = new ArrayList<>();
                            Log.d(TAG, "selected_values.length: " + selected_values.length);
                            for (int i = 0; i < selected_values.length; i++) {
                                Log.d(TAG, "selected_values:" + selected_values[i]);
                                if (selected_values[i]) {

                                    selectedNames.add(allNames.get(i));
                                }
                            }

                            if (selectedNames.size() == 0) {

                                Toast.makeText(RoutesBrowserActivity.this, getResources().getString(R.string.no_routes_selected), Toast.LENGTH_SHORT).show();

                            } else {

                                ArrayList<Route> gpxRoutesPickedByUser = new ArrayList<>();

                                for (String nameOfGPXroutePickedByUser : selectedNames) {

                                    int idxOfRoute = gpxRteDisplayNames.indexOf(nameOfGPXroutePickedByUser);
                                    gpxRoutesPickedByUser.add(sortedRoutes.get(idxOfRoute));

                                }

                                int maxPathWpt = 100;
                                double maxPathError = 10d;

                                if (reduceCheckBox.isChecked()) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.simplifying), Toast.LENGTH_SHORT).show();

                                    if (!maxWptEditText.getText().toString().isEmpty()) {
                                        maxPathWpt = Integer.valueOf(maxWptEditText.getText().toString());
                                    }

                                    if (!maxError.getText().toString().isEmpty()) {
                                        maxPathError = Double.valueOf(maxError.getText().toString());
                                    }
                                }

                                if (!mergeCheckbox.isChecked()) {

                                    if (reduceCheckBox.isChecked()) {
                                        for (Route rteToSimplify : gpxRoutesPickedByUser) {
                                            if (reduceCheckBox.isChecked()) {
                                                GpxUtils.simplifyRoute(rteToSimplify, maxPathWpt, maxPathError);
                                            }
                                        }
                                    }
                                    Log.d(TAG,"Data.sRoutesGpx.getRoutes().size():" + Data.sRoutesGpx.getRoutes().size());
                                    Data.sRoutesGpx.addRoutes(gpxRoutesPickedByUser);
                                    Log.d(TAG,"gpxRoutesPickedByUser.size():" + gpxRoutesPickedByUser.size());
                                    Log.d(TAG,"Data.sRoutesGpx.getRoutes().size():" + Data.sRoutesGpx.getRoutes().size());

                                } else {
                                    final Route mergedRoute = new Route();
                                    String name = mergeNameEditText.getText().toString();
                                    if (name.isEmpty()) name = "Merged route";
                                    mergedRoute.setName(name);
                                    for (Route rteToSimplify : gpxRoutesPickedByUser) {
                                        mergedRoute.addRoutePoints(rteToSimplify.getRoutePoints());
                                    }
                                    if (reduceCheckBox.isChecked()) {

                                        GpxUtils.simplifyRoute(mergedRoute, maxPathWpt, maxPathError);
                                    }
                                    Data.sRoutesGpx.addRoute(mergedRoute);
                                }

                                int purged_routes = GpxUtils.purgeRoutesOverlapping(Data.sRoutesGpx);

                                if (purged_routes != 0) {

                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.removed) + " " + purged_routes + " "
                                            + getResources().getString(R.string.duplicates) + " ", Toast.LENGTH_SHORT).show();

                                } else {

                                    Toast.makeText(RoutesBrowserActivity.this, String.format(getResources().getString(R.string.tracks_converted), gpxRoutesPickedByUser.size()), Toast.LENGTH_LONG).show();
                                }
                                refreshMap();
                            }
                        }
                    })
                    .setNegativeButton(buttonCancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setPositiveButton(buttonAll, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            int maxPathWpt = 100;
                            double maxPathError = 50d;

                            if (reduceCheckBox.isChecked()) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.simplifying), Toast.LENGTH_SHORT).show();

                                if (!maxWptEditText.getText().toString().isEmpty()) {
                                    maxPathWpt = Integer.valueOf(maxWptEditText.getText().toString());
                                }

                                if (!maxError.getText().toString().isEmpty()) {
                                    maxPathError = Double.valueOf(maxError.getText().toString());
                                }
                            }


                            if (!mergeCheckbox.isChecked()) {

                                if (reduceCheckBox.isChecked()) {
                                    for (Route rteToSimplify : importedRoutes) {

                                        GpxUtils.simplifyRoute(rteToSimplify, maxPathWpt, maxPathError);

                                    }
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.simplifying), Toast.LENGTH_SHORT).show();
                                }
                                Data.sRoutesGpx.addRoutes(importedRoutes);
                            } else if(mergeCheckbox.isChecked()) {
                                final Route mergedRoute = new Route();
                                String name = mergeNameEditText.getText().toString();
                                if(name.isEmpty()) name = "Merged route";
                                mergedRoute.setName(name);
                                for (Route rteToSimplify : importedRoutes) {
                                    mergedRoute.addRoutePoints(rteToSimplify.getRoutePoints());
                                }

                                if (reduceCheckBox.isChecked()) {

                                    GpxUtils.simplifyRoute(mergedRoute, maxPathWpt, maxPathError);
                                }
                                Data.sRoutesGpx.addRoute(mergedRoute);
                            }

                            int purged_routes = GpxUtils.purgeRoutesOverlapping(Data.sRoutesGpx);

                            if (purged_routes != 0) {

                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.removed) + " " + purged_routes + " "
                                        + getResources().getString(R.string.duplicates) + " ", Toast.LENGTH_SHORT).show();

                            } else {

                                if (importedRoutes != null) {

                                    Toast.makeText(RoutesBrowserActivity.this, String.format(getResources().getString(R.string.tracks_converted),
                                            importedRoutes.size()), Toast.LENGTH_LONG).show();
                                }
                            }
                            refreshMap();
                        }
                    });

            ListView listView = (ListView) layout.findViewById(R.id.listItems);
            ArrayAdapter<String> adapter = new ArrayAdapter(
                    this,
                    //android.R.layout.simple_list_item_multiple_choice,
                    R.layout.dialog_multi_choice_item,
                    menu_entries
            );

            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            // Set an item click listener for the ListView
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    SparseBooleanArray clickedItemPositions = listView.getCheckedItemPositions();
                    selected_values[i] = clickedItemPositions.get(i,false);
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void clearRoutes() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String clearTextTitle = getResources().getString(R.string.dialog_clear_routes);
        String clearText = getResources().getString(R.string.dialog_clear);
        String cancelText = getResources().getString(R.string.dialog_cancel);

        builder.setCancelable(true)
                .setTitle(clearTextTitle)
                .setPositiveButton(clearText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Data.sRoutesGpx.clearRoutes();
                        Data.sFilteredRoutes.clear();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.routes_cleared), Toast.LENGTH_SHORT).show();
                        refreshMap();
                    }
                })
                .setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void displayExportMultipleDialog() {

        if (Data.sRoutesGpx.getRoutes().size() == 0) {

            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_routes_memory), Toast.LENGTH_LONG).show();
            return;
        }

        final List<Route> sortedRoutes = new ArrayList<>();
        //final List<String> gpxRteDisplayNames = GpxUtils.getRouteNamesSortedAlphabeticaly(Data.sRoutesGpx.getRoutes(), sortedRoutes);
        final List<String> gpxRteDisplayNames = GpxUtils.getRouteNamesUnsorted_LengthTypeArity(Data.sRoutesGpx.getRoutes(), Data.sUnitsInUse, sortedRoutes);

        final List<String> allNames = new ArrayList<>();
        allNames.addAll(gpxRteDisplayNames);

        String[] menu_entries = new String[allNames.size()];
        menu_entries = allNames.toArray(menu_entries);

        final boolean selected_values[] = new boolean[allNames.size()];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String dialogTitle = getResources().getString(R.string.dialog_select_routes_export);
        String buttonAll = getResources().getString(R.string.dialog_all);
        String buttonSelected = getResources().getString(R.string.dialog_selected);
        String buttonCancel = getResources().getString(R.string.dialog_cancel);

        builder.setTitle(dialogTitle)
                .setIcon(R.drawable.ico_pick_many)
                .setCancelable(true)

                .setNeutralButton(buttonSelected, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        List<String> selectedNames = new ArrayList<>();

                        for (int i = 0; i < selected_values.length; i++) {

                            if (selected_values[i]) {
                                selectedNames.add(allNames.get(i));
                            }
                        }

                        if (selectedNames.size() == 0) {

                            Toast.makeText(RoutesBrowserActivity.this, getResources().getString(R.string.no_routes_selected), Toast.LENGTH_SHORT).show();

                        } else {

                            ArrayList<Route> gpxRoutesPickedByUser = new ArrayList<>();

                            for (String nameOfGPXroutePickedByUser : selectedNames) {

                                int idxOfRoute = gpxRteDisplayNames.indexOf(nameOfGPXroutePickedByUser);
                                gpxRoutesPickedByUser.add(sortedRoutes.get(idxOfRoute));
                            }
                            gpxOut.addRoutes(gpxRoutesPickedByUser);

                            showSaveRoutesDialog();
                        }
                    }
                })
                .setNegativeButton(buttonCancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setPositiveButton(buttonAll, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        /*
                        List<String> selectedNames = new ArrayList<>();
                        selectedNames.addAll(allNames);

                        ArrayList<Route> gpxRoutesPickedByUser = new ArrayList<>();

                        for (String nameOfGPXroutesPickedByUser : selectedNames) {

                            int idxOfRoute = gpxRteDisplayNames.indexOf(nameOfGPXroutesPickedByUser);
                            gpxRoutesPickedByUser.add(sortedRoutes.get(idxOfRoute));
                        }
                        */
                        gpxOut.addRoutes(Data.sRoutesGpx.getRoutes());

                        showSaveRoutesDialog();
                    }
                });

        builder.setMultiChoiceItems(menu_entries, selected_values, new DialogInterface.OnMultiChoiceClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1, boolean arg2) {

                selected_values[arg1] = arg2;
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showSaveRoutesDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View saveAsLayout = inflater.inflate(R.layout.export_gpx_dialog_layout, null);

        final EditText filename = (EditText) saveAsLayout.findViewById(R.id.save_new_filename);

        final String path = Data.lastImportedFileFullPath.length()>0? getParentFromFullPath( Data.lastImportedFileFullPath):Data.defaultDirectoryPath;

        final Intent fileExploreIntent = new Intent(
                FileBrowserActivity.INTENT_ACTION_SELECT_FILE,
                null,
                this,
                FileBrowserActivity.class
        );

        filename.setText(fileName);

        String dialogTitle = getResources().getString(R.string.dialog_savegpx_saveasnew);
        String saveText = getResources().getString(R.string.dialog_save_changes_save);
        String saveAsText = getResources().getString(R.string.file_pick);
        String cancelText = getResources().getString(R.string.dialog_cancel);

        builder.setTitle(dialogTitle)
                .setView(saveAsLayout)
                .setIcon(R.drawable.map_save)
                .setCancelable(true)
                .setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNeutralButton(saveAsText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        fileExploreIntent.putExtra(
                                FileBrowserActivity.startDirectoryParameter,
                                path
                        );
                        startActivityForResult(
                                fileExploreIntent,
                                REQUEST_CODE_PICK_FILE
                        );
                    }
                })
                .setPositiveButton(saveText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        fileName = filename.getText().toString().trim();
                        File full_file_path = new File(path +"/" + fileName + ".gpx");
                        saveSelectedRoutes(full_file_path.toString());
                    }
                });

        AlertDialog alert = builder.create();

        alert.show();

        final Button saveButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);

        final TextWatcher validate_name = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

                saveButton.setEnabled(!arg0.toString().equals(""));
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {

                saveButton.setEnabled(!s.toString().equals(""));
            }
        };
        filename.addTextChangedListener(validate_name);

    }

    private void saveSelectedRoutes(String file) {

        File outputFile = new File(file);
        if (outputFile.exists()) {

            Gpx gpxToSave = GpxFileIo.parseIn(file);

            if (gpxToSave == null) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_gpx), Toast.LENGTH_LONG).show();
                return;

            } else {

                gpxToSave.addRoutes(gpxOut.getRoutes());

                int purged_routes = GpxUtils.purgeRoutesOverlapping(gpxToSave);

                if (purged_routes != 0) {

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.removed) + " " + purged_routes + " "
                            + getResources().getString(R.string.duplicates), Toast.LENGTH_SHORT).show();
                }
                GpxFileIo.parseOut(gpxToSave, file);

                Toast.makeText(getApplicationContext(), getResources().getString(R.string.selected_routes_exported), Toast.LENGTH_SHORT).show();
            }

        } else {

            Toast.makeText(getApplicationContext(), getResources().getString(R.string.saving_to) + " " + outputFile, Toast.LENGTH_LONG).show();

            boolean success = outputFile.exists();
            if (!outputFile.exists()) {
                try {
                    success = outputFile.createNewFile();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed_creating_file), Toast.LENGTH_SHORT).show();
                }
            }

            if (success) {

                Gpx gpxToSave = new Gpx();

                switch (filePickerAction) {
                    case SAVE_SELECTED_ROUTE:
                        gpxToSave.addRoute(Data.sFilteredRoutes.get(Data.sSelectedRouteIdx));
                        break;

                    case SAVE_MULTIPLE_ROUTES:
                        gpxToSave.addRoutes(gpxOut.getRoutes());
                        break;

                    default:
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.neither_single_nor_multiple_routes), Toast.LENGTH_SHORT).show();
                        break;
                }

                int purged_routes = GpxUtils.purgeRoutesOverlapping(gpxToSave);

                if (purged_routes != 0) {

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.removed) + " " + purged_routes
                            + " " + getResources().getString(R.string.duplicates), Toast.LENGTH_SHORT).show();
                }

                GpxFileIo.parseOut(gpxToSave, outputFile.toString());

                Toast.makeText(getApplicationContext(), getResources().getString(R.string.selected_routes_exported), Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed_writing_gpx), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_UP:

                if (mMapDragged) {

                    mMapDragged = false;

                    refreshMap(false);
                }
                break;
        }
        return true;

    }
}