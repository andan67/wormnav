package org.andan.android.connectiq.wormnav;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.LocationServices;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.karambola.gpx.beans.Gpx;
import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.Track;
import pt.karambola.gpx.beans.TrackPoint;
import pt.karambola.gpx.io.GpxStreamIo;
import pt.karambola.gpx.parser.GpxParserOptions;
import pt.karambola.gpx.util.GpxUtils;

import static org.andan.android.connectiq.wormnav.R.id.osmmap;

import org.andan.android.connectiq.wormnav.BuildConfig;

/**
 * Route Picker activity created by piotr on 02.05.17.
 */
public class TracksBrowserActivity extends Utils {

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    ListView list;

    String[] web = new String[1];

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private final String TAG = TracksBrowserActivity.class.getName();

    private final double MAX_ZOOM_LEVEL = 19;
    private final double MIN_ZOOM_LEVEL = 4;

    private final int REQUEST_CODE_IMPORT_TRACKS = 1;
    private final int REQUEST_CODE_EXPORT_TRACKS = 2;

    private Button fitButton;
    private Button nextButton;
    private Button previousButton;

    private Button editButton;

    private int mAllTracksNumber = 0;

    TextView trackPrompt;

    TextView routesSummary;

    private MapView mMapView;
    private IMapController mapController;

    private MapEventsReceiver mapEventsReceiver;

    private MyLocationNewOverlay mLocationOverlay;

    private RotationGestureOverlay mRotationGestureOverlay;

    private List<GeoPoint> mAllGeopoints;

    private Gpx gpxOut = new Gpx();

    /**
     * Used in the displayConvertTracksDialog()
     */
    private boolean mDeleteSourceTracks = false;
    private int maxPathWpt = 300;
    private double maxPathError = 10d;

    /**
     * Track label marker -> index of selected track
     */
    private Map<Marker, Integer> markerToTrackIdx;

    /**
     * Drawer entry -> route index
     */
    private Map<Integer, Integer> drawerIdxToTrackIdx;

    /**
     * View filtering todo delete me
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
        setContentView(R.layout.activity_tracks_browser);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

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

                Data.sSelectedTrackIdx = null;
                refreshMap(false);

                return false;
            }

            @Override

            public boolean longPressHelper(GeoPoint p) {

                return false;
            }
        };

        restoreMapPosition();

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

        if (Data.sTracksGpx != null) {
            Data.sAllTracks = Data.sTracksGpx.getTracks();
        } else {
            Data.sAllTracks = null;
        }

        if (Data.sAllTracks == null || Data.sAllTracks.isEmpty()) {
            Data.sSelectedTrackIdx = null;
            mAllTracksNumber = 0;
        } else {

            mAllTracksNumber = Data.sAllTracks.size();
        }

        if (updateDrawerItems() != null) {
            addDrawerItems(updateDrawerItems());
        } else {
            addDrawerItems(web);
        }

        markerToTrackIdx = new HashMap<>();

        for (int i = 0; i < mAllTracksNumber; i++) {

            final Track track = Data.sAllTracks.get(i);

            List<TrackPoint> trackPoints = track.getTrackPoints();

            int halfWayPoint = trackPoints.size() / 2;
            int lastTrackPoint = trackPoints.size() - 1;

            List<GeoPoint> geoPoints = new ArrayList<>();

            for (int j = 0; j < trackPoints.size(); j++) {

                TrackPoint trackPoint = trackPoints.get(j);
                GeoPoint geoPoint = new GeoPoint(trackPoint.getLatitude(), trackPoint.getLongitude());
                geoPoints.add(geoPoint);

                if (Data.sSelectedTrackIdx == null) {
                    mAllGeopoints.add(geoPoint);

                    if (j == halfWayPoint) {

                        String name = track.getName() != null ? track.getName() : "?";
                        Drawable icon = new BitmapDrawable(getResources(), makeRouteNameBitmap(this, name));

                        Marker marker = new Marker(mMapView);
                        markerToTrackIdx.put(marker, i);
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
                                Data.sSelectedTrackIdx = markerToTrackIdx.get(marker);
                                refreshMap(false);
                                return true;
                            }
                        });

                        mMapView.getOverlays().add(marker);
                    }

                    if (j == lastTrackPoint) {

                        Marker marker = new Marker(mMapView);
                        markerToTrackIdx.put(marker, i);
                        marker.setPosition(geoPoint);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marker.setDraggable(false);
                        marker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.route_end, null));
                        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {

                                /*
                                 * Click the track label marker to select the track
                                 */
                                Data.sSelectedTrackIdx = markerToTrackIdx.get(marker);
                                refreshMap(false);
                                return true;
                            }
                        });

                        mMapView.getOverlays().add(marker);

                    }

                } else {

                    if (i == Data.sSelectedTrackIdx) {
                        mAllGeopoints.add(geoPoint);

                        if (j == halfWayPoint) {

                            String name = track.getName() != null ? track.getName() : "?";
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

                        if (j == lastTrackPoint) {

                            Marker marker = new Marker(mMapView);
                            markerToTrackIdx.put(marker, i);
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

            if (Data.sSelectedTrackIdx != null) {

                if (i == Data.sSelectedTrackIdx) {

                    routeOverlay.getOutlinePaint().setColor(Color.parseColor("#0099ff"));

                } else {

                    routeOverlay.getOutlinePaint().setColor(Color.parseColor("#11000000"));
                }

            } else {

                routeOverlay.setColor(typeColors[i % N_COLOURS]);
            }
            routeOverlay.setWidth(15);

            mMapView.getOverlays().add(routeOverlay);
        }

        if (Data.sSelectedTrackIdx != null) {
            trackPrompt.setText(GpxUtils.getTrackNameAnnotated(Data.sAllTracks.get(Data.sSelectedTrackIdx), Data.sUnitsInUse));
        } else {
            trackPrompt.setText(getResources().getString(R.string.track_edit_prompt));
        }
        routesSummary.setText(String.format(getResources().getString(R.string.x_tracks), mAllTracksNumber));

        if (zoom_to_fit && mAllGeopoints.size() > 0) {
            mMapView.zoomToBoundingBox(findBoundingBox(mAllGeopoints).increaseByScale(1.1f),false);
        }

        mMapView.invalidate();
        setButtonsState();
    }

    private void refreshMap() {
        refreshMap(true);
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
                    Log.d(TAG, "zoomLevel:" + mMapView.getZoomLevelDouble());
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_routes_in_view), Toast.LENGTH_SHORT).show();
                }
            }
        });

        nextButton = (Button) findViewById(R.id.picker_next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Data.sSelectedTrackIdx == null) {
                    Data.sSelectedTrackIdx = 0;
                } else {
                    if (Data.sSelectedTrackIdx < mAllTracksNumber - 1) {
                        Data.sSelectedTrackIdx++;
                    } else {
                        Data.sSelectedTrackIdx = 0;
                    }
                }
                refreshMap();
            }
        });
        previousButton = (Button) findViewById(R.id.picker_previous_button);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Data.sSelectedTrackIdx == null) {
                    Data.sSelectedTrackIdx = 0;
                } else {
                    if (Data.sSelectedTrackIdx > 0) {
                        Data.sSelectedTrackIdx--;
                    } else {
                        Data.sSelectedTrackIdx = mAllTracksNumber - 1;
                    }
                }
                refreshMap();
            }
        });

        editButton = (Button) findViewById(R.id.picker_edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                displayEditDialog();
            }
        });

        routesSummary = (TextView) findViewById(R.id.routes_summary);

        trackPrompt = (TextView) findViewById(R.id.picker_route_prompt);

        final TextView copyright = (TextView) findViewById(R.id.copyright);
        copyright.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setButtonsState() {

        if (mAllTracksNumber > 0) {
            nextButton.setEnabled(true);
            nextButton.getBackground().setAlpha(255);
        } else {
            nextButton.setEnabled(false);
            nextButton.getBackground().setAlpha(100);
        }

        if (mAllTracksNumber > 0) {
            previousButton.setEnabled(true);
            previousButton.getBackground().setAlpha(255);
        } else {
            previousButton.setEnabled(false);
            previousButton.getBackground().setAlpha(100);
        }

        if (Data.sAllTracks != null && Data.sAllTracks.isEmpty() && Data.sSelectedTrackIdx != null) {
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
        getMenuInflater().inflate(R.menu.menu_tracks_browser, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        /*
         * We'll enable/disable menu options here
         */

        menu.findItem(R.id.tracks_delete_selected).setEnabled(Data.sSelectedTrackIdx != null);
        menu.findItem(R.id.tracks_clear).setEnabled(Data.sTracksGpx!= null && Data.sTracksGpx.getTracks().size() > 0);
        menu.findItem(R.id.tracks_convert).setEnabled(Data.sTracksGpx!= null && Data.sTracksGpx.getTracks().size() > 0);
        menu.findItem(R.id.tracks_edit_properties).setEnabled(Data.sSelectedTrackIdx != null);
        menu.findItem(R.id.tracks_export).setEnabled(Data.sTracksGpx!= null && Data.sTracksGpx.getTracks().size() > 0);
        menu.findItem(R.id.tracks_send_selected_to_device).setEnabled(Data.sSelectedTrackIdx != null);
        /*
        menu.findItem(R.id.routes_edit_selected).setEnabled(Data.sSelectedRouteIdx != null);
        menu.findItem(R.id.routes_simplify_selected).setEnabled(Data.sSelectedRouteIdx != null);
        */

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.tracks_edit_properties:

                displayEditDialog();
                return true;

            case R.id.tracks_convert:

                displayConvertTracksDialog();
                return true;

            case R.id.tracks_delete_selected:

                final Track track = Data.sTracksGpx.getTracks().get(Data.sSelectedTrackIdx);
                String deleteMessage;
                if (track.getName() != null) {
                    String deleteMessageFormat = getResources().getString(R.string.dialog_delete_track_message);
                    deleteMessage = String.format(deleteMessageFormat, track.getName());
                } else {
                    deleteMessage = getResources().getString(R.string.about_to_delete_track);
                }
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.map_warning)
                        .setTitle(R.string.dialog_delete_route)
                        .setMessage(deleteMessage)
                        .setPositiveButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                /*
                                 * Unfortunately the GeoKarambola library has no removeTrack(track)
                                 * method. Let's do it 'manually' ;)
                                 */
                                List<Track> allTracks = Data.sTracksGpx.getTracks();
                                allTracks.remove(track);
                                Data.sTracksGpx.setTracks(allTracks);
                                Data.sSelectedTrackIdx = null;
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

            case R.id.tracks_clear:

                if (Data.sTracksGpx.getTracks().size() > 0) {
                    clearTracks();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.no_routes_to_clear), Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.tracks_import_tracks:

                performGpxFileSearch(REQUEST_CODE_IMPORT_TRACKS, Data.lastImportedExportedUri);
                return true;

            case R.id.tracks_export:

                displayExportMultipleDialog();
                //performGpxFileSave();
                return true;

            case R.id.tracks_send_selected_to_device:

                final Track trackSelected = Data.sTracksGpx.getTracks().get(Data.sSelectedTrackIdx);
                SendToDeviceUtility.startDeviceBrowserActivity(TracksBrowserActivity.this, trackSelected);
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
                CustomListNoIcons(TracksBrowserActivity.this, items);
        list = (ListView) findViewById(R.id.routesNavList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (drawerIdxToTrackIdx != null && !drawerIdxToTrackIdx.isEmpty()) {
                    Data.sSelectedTrackIdx = drawerIdxToTrackIdx.get(position);
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
        switch (requestCode) {
            case REQUEST_CODE_IMPORT_TRACKS:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    Data.lastImportedExportedUri = uri;
                    displayImportTracksDialog(uri);
                }
                break;
            case REQUEST_CODE_EXPORT_TRACKS:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    Data.lastImportedExportedUri = uri;
                    saveSelectedTracks(uri);
                }
                break;
            default:
                break;

        }
    }

    private String[] updateDrawerItems() {

        if (Data.sTracksGpx==null || Data.sTracksGpx.getTracks().size() == 0 || Data.sAllTracks.size() == 0) {
            return null;
        }

        Track[] sortedTracksArray = new Track[Data.sAllTracks.size()];
        sortedTracksArray = Data.sAllTracks.toArray(sortedTracksArray);

        Arrays.sort(sortedTracksArray, Data.trkComparator);

        List<String> gpxTrackDisplayNames = new ArrayList<>();

        drawerIdxToTrackIdx = new HashMap<>();
        for (int i = 0; i < sortedTracksArray.length; i++) {

            Track track = sortedTracksArray[i];

            gpxTrackDisplayNames.add(track.getName());

            drawerIdxToTrackIdx.put(i, Data.sAllTracks.indexOf(track));
        }

        List<String> allNames = new ArrayList<>();
        allNames.addAll(gpxTrackDisplayNames);

        String[] drawer_entries = new String[allNames.size()];
        drawer_entries = allNames.toArray(drawer_entries);

        return drawer_entries;
    }

    private void displayEditDialog() {

        if (Data.sAllTracks.isEmpty() || Data.sSelectedTrackIdx == null) {

            return;
        }

        final Track picked_track = Data.sAllTracks.get(Data.sSelectedTrackIdx);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View trackEditLayout = inflater.inflate(R.layout.track_edit_dialog, null);


        final EditText editName = (EditText) trackEditLayout.findViewById(R.id.track_name_edit);
        editName.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(99)
        });

        final EditText editType = (EditText) trackEditLayout.findViewById(R.id.track_type_edit);
        final EditText editDesc = (EditText) trackEditLayout.findViewById(R.id.track_description_edit);

        final Spinner spinner = (Spinner) trackEditLayout.findViewById(R.id.track_type_spinner);
        final List<String> trkTypes = GpxUtils.getDistinctTrackTypes(Data.sTracksGpx.getTracks());
        trkTypes.add(0, getResources().getString(R.string.type));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, trkTypes);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
        setUpSpinnerListener(spinner, editType);

        editName.setText(picked_track.getName());
        if (picked_track.getType() != null) editType.setText(picked_track.getType());

        if (picked_track.getDescription() != null) editDesc.setText(picked_track.getDescription());

        String dialogTitle = getResources().getString(R.string.picker_edit_dialog_title);
        String okText = getResources().getString(R.string.picker_edit_apply);

        String cancelText = getResources().getString(R.string.dialog_cancel);

        builder.setTitle(dialogTitle)
                .setIcon(R.drawable.map_edit)
                .setView(trackEditLayout)
                .setCancelable(true)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if (editName.getText().toString().isEmpty()) {
                            editName.setText(new Date().toString());
                        }

                        String name = editName.getText().toString();
                        if (!name.isEmpty()) {
                            if (name.length() > 99) name = name.substring(0, 100);
                            picked_track.setName(name);

                        } else {
                            picked_track.setName(null);
                        }

                        if (!editDesc.getText().toString().isEmpty()) {
                            picked_track.setDescription(editDesc.getText().toString().trim());
                        } else {
                            picked_track.setDescription(null);
                        }

                        if (!editType.getText().toString().isEmpty()) {
                            picked_track.setType(editType.getText().toString().trim());
                        } else {
                            picked_track.setType(null);
                        }

                        trackPrompt.setText(GpxUtils.getTrackNameAnnotated(picked_track, Data.sUnitsInUse));

                        refreshMap();
                    }
                })
                .setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

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

    private void displayImportTracksDialog(final Uri uri) {

        Data.sSelectedTrackIdx = null;

        /*
         * Check if the file contains any tracks
         */

        Log.d("import_uri", "uri: " + uri.getPath());
        Gpx gpxIn = GpxStreamIo.parseIn(getInputStreamFromUri(uri), GpxParserOptions.ONLY_TRACKS);

        if (gpxIn == null) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_tracks_not_gpx), Toast.LENGTH_LONG).show();
            return;
        }

        /*
         * Check for tracks with no name (e.g. Endomondo exports like this), insert generics if so
         */
        final List<Track> foundTracks = gpxIn.getTracks();

        for (int i = 0; i < foundTracks.size(); i++) {

            Track track = foundTracks.get(i);
            if (track.getName() == null) {
                track.setName("Track" + i);
            }
        }

        final List<Track> sortedTracks = new ArrayList<>();
        //final List<String> sortedTrackNames = GpxUtils.getTrackNamesSortedAlphabeticaly(foundTracks, Data.sUnitsInUse, sortedTracks);
        // sortedTracksName must be unique!
        final List<String> sortedTrackNames = GpxUtils.getTrackNamesSUnsorted_LengthTypeArity(foundTracks, Data.sUnitsInUse, sortedTracks);

        if (sortedTrackNames.isEmpty()) {
            /*
             * No tracks found, don't show the dialog
             */
            Toast.makeText(TracksBrowserActivity.this, getResources().getString(R.string.no_named_tracks), Toast.LENGTH_SHORT).show();

        } else {

            final List<String> allNames = new ArrayList<>();
            allNames.addAll(sortedTrackNames);

            String[] menu_entries = new String[allNames.size()];
            menu_entries = allNames.toArray(menu_entries);

            final boolean selected_values[] = new boolean[allNames.size()];

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            String dialogTitle = getResources().getString(R.string.dialog_select_tracks_import);
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

                                Toast.makeText(TracksBrowserActivity.this, getResources().getString(R.string.no_tracks_selected), Toast.LENGTH_SHORT).show();

                            } else {

                                ArrayList<Track> gpxTracksPickedByUser = new ArrayList<>();

                                for (String nameOfGpxTrackPickedByUser : selectedNames) {

                                    int idxOfTrack = sortedTrackNames.indexOf(nameOfGpxTrackPickedByUser);
                                    gpxTracksPickedByUser.add(sortedTracks.get(idxOfTrack));
                                }

                                if(Data.sTracksGpx==null) {
                                    Data.sTracksGpx = new Gpx();
                                }
                                Data.sTracksGpx.addTracks(gpxTracksPickedByUser);

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

                            ArrayList<Track> gpxTracksPickedByUser = new ArrayList<>();

                            for (String nameOfGPXtrackPickedByUser : selectedNames) {
                                int idxOfTrack = sortedTrackNames.indexOf(nameOfGPXtrackPickedByUser);
                                gpxTracksPickedByUser.add(sortedTracks.get(idxOfTrack));
                            }

                            if(Data.sTracksGpx==null) {
                                Data.sTracksGpx = new Gpx();
                            }
                            Data.sTracksGpx.addTracks(gpxTracksPickedByUser);

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

    private void displayConvertTracksDialog() {

        if (Data.sAllTracks.size() == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_tracks_in_memory), Toast.LENGTH_LONG).show();
            return;
        }

        final List<Track> sortedTracks = new ArrayList<>();

        //final List<String> gpxTrkDisplayNames = GpxUtils.getTrackNamesSortedAlphabeticaly(Data.sAllTracks, Data.sUnitsInUse, sortedTracks);
        final List<String> gpxTrkDisplayNames = GpxUtils.getTrackNamesSUnsorted_LengthTypeArity(Data.sAllTracks, Data.sUnitsInUse, sortedTracks);

        if (gpxTrkDisplayNames != null && gpxTrkDisplayNames.isEmpty()) {

            Toast.makeText(TracksBrowserActivity.this, getString(R.string.no_named_tracks), Toast.LENGTH_SHORT).show();

        } else {

            final List<String> allNames = new ArrayList<>();

            if (gpxTrkDisplayNames != null) {
                allNames.addAll(gpxTrkDisplayNames);
            }

            String[] menu_entries = new String[allNames.size()];
            menu_entries = allNames.toArray(menu_entries);

            final boolean selected_values[] = new boolean[allNames.size()];

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);

            LayoutInflater inflater = getLayoutInflater();
            final View layout = inflater.inflate(R.layout.convert_dialog, null);
            final EditText maxWptEditText = (EditText) layout.findViewById(R.id.reduceMaxPoints);
            maxWptEditText.setText(String.valueOf(maxPathWpt));

            final EditText maxError = (EditText) layout.findViewById(R.id.reduceMaxError);
            maxError.setText(String.valueOf(maxPathError));

            final CheckBox reduceCheckBox = (CheckBox) layout.findViewById(R.id.reduceCheckbox);
            reduceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    maxWptEditText.setEnabled(isChecked);
                    maxError.setEnabled(isChecked);
                }
            });

            final EditText mergeNameEditText = (EditText) layout.findViewById(R.id.mergeName);
            final CheckBox mergeCheckbox = (CheckBox) layout.findViewById(R.id.mergeCheckbox);
            mergeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mergeNameEditText.setEnabled(isChecked);
                }
            });

            final CheckBox deleteSourceCheckBox = (CheckBox) layout.findViewById(R.id.deleteSourceCheckbox);
            deleteSourceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mDeleteSourceTracks = isChecked;
                }
            });

            String dialogTitle = getResources().getString(R.string.dialog_select_tracks_convert);
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

                            for (int i = 0; i < selected_values.length; i++) {

                                if (selected_values[i]) {
                                    selectedNames.add(allNames.get(i));
                                }
                            }

                            if (selectedNames.size() == 0) {

                                Toast.makeText(TracksBrowserActivity.this, getResources().getString(R.string.no_tracks_selected), Toast.LENGTH_SHORT).show();

                            } else {

                                ArrayList<Track> gpxTracksPickedByUser = new ArrayList<>();

                                for (String nameOfGPXtrackPickedByUser : selectedNames) {

                                    int idxOfTrack = gpxTrkDisplayNames.indexOf(nameOfGPXtrackPickedByUser);
                                    gpxTracksPickedByUser.add(sortedTracks.get(idxOfTrack));
                                }

                                if (reduceCheckBox.isChecked()) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.simplifying), Toast.LENGTH_SHORT).show();

                                    if (!maxWptEditText.getText().toString().isEmpty()) {
                                        maxPathWpt = Integer.valueOf(maxWptEditText.getText().toString());
                                    }

                                    if (!maxError.getText().toString().isEmpty()) {
                                        maxPathError = Double.valueOf(maxError.getText().toString());
                                    }
                                }


                                if(!mergeCheckbox.isChecked()) {
                                    final List<Route> convertedRoutes = new ArrayList<>();
                                    for (Track track : gpxTracksPickedByUser) {

                                        Route convertedRoute = Utils.convertTrackToRoute(track);

                                        if (reduceCheckBox.isChecked()) {

                                            GpxUtils.simplifyRoute(convertedRoute, maxPathWpt, maxPathError);
                                        }

                                        convertedRoutes.add(convertedRoute);
                                    }

                                    Data.sRoutesGpx.addRoutes(convertedRoutes);
                                } else {
                                    final Route mergedRoute = new Route();
                                    String name = mergeNameEditText.getText().toString();
                                    if(name.isEmpty()) name = "Merged route";
                                    mergedRoute.setName(name);
                                    for (Track track : gpxTracksPickedByUser) {
                                        Route convertedRoute = Utils.convertTrackToRoute(track);
                                        mergedRoute.addRoutePoints(convertedRoute.getRoutePoints());
                                    }
                                    if (reduceCheckBox.isChecked()) {

                                        GpxUtils.simplifyRoute(mergedRoute, maxPathWpt, maxPathError);
                                    }
                                    Data.sRoutesGpx.addRoute(mergedRoute);

                                }

                                if (mDeleteSourceTracks) {

                                    for (String name : selectedNames) {

                                        int index = gpxTrkDisplayNames.indexOf(name);
                                        Data.sAllTracks.remove(index);
                                        Data.sTracksGpx.setTracks(Data.sAllTracks);
                                    }
                                }

                                int purged_routes = GpxUtils.purgeRoutesOverlapping(Data.sRoutesGpx);

                                if (purged_routes != 0) {

                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.removed) + " " + purged_routes + " "
                                            + getResources().getString(R.string.duplicates) + " ", Toast.LENGTH_SHORT).show();

                                } else {

                                    Toast.makeText(TracksBrowserActivity.this, String.format(getResources().getString(R.string.tracks_converted), gpxTracksPickedByUser.size()), Toast.LENGTH_LONG).show();
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
                            if (Data.sAllTracks == null || Data.sAllTracks.isEmpty()) {

                                Toast.makeText(TracksBrowserActivity.this, getResources().getString(R.string.no_tracks_in_memory), Toast.LENGTH_SHORT).show();

                            } else {

                                if (reduceCheckBox.isChecked()) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.simplifying), Toast.LENGTH_SHORT).show();

                                    if (!maxWptEditText.getText().toString().isEmpty()) {
                                        maxPathWpt = Integer.valueOf(maxWptEditText.getText().toString());
                                    }

                                    if (!maxError.getText().toString().isEmpty()) {
                                        maxPathError = Double.valueOf(maxError.getText().toString());
                                    }
                                }

                                final List<Route> importedRoutes = new ArrayList<>();

                                int counter = 0;


                                if(!mergeCheckbox.isChecked()) {
                                    for (Track track : Data.sAllTracks) {

                                        Route convertedRoute = Utils.convertTrackToRoute(track);

                                        if (reduceCheckBox.isChecked()) {

                                            GpxUtils.simplifyRoute(convertedRoute, maxPathWpt, maxPathError);
                                        }

                                        importedRoutes.add(convertedRoute);

                                        counter++;
                                    }

                                    Data.sRoutesGpx.addRoutes(importedRoutes);
                                } else {
                                    Log.d(TAG, "mergeTracks.isChecked(): " + mergeCheckbox.isChecked());
                                    final Route mergedRoute = new Route();
                                    String name = mergeNameEditText.getText().toString();
                                    if(name.isEmpty()) name = "Merged route";
                                    mergedRoute.setName(name);
                                    for (Track track : Data.sAllTracks) {
                                        Route convertedRoute = Utils.convertTrackToRoute(track);
                                        mergedRoute.addRoutePoints(convertedRoute.getRoutePoints());
                                        counter++;
                                    }
                                    if (reduceCheckBox.isChecked()) {

                                        GpxUtils.simplifyRoute(mergedRoute, maxPathWpt, maxPathError);
                                    }
                                    Data.sRoutesGpx.addRoute(mergedRoute);

                                }

                                if (mDeleteSourceTracks) {
                                    Data.sTracksGpx.clearTracks();
                                }

                                int purged_routes = GpxUtils.purgeRoutesOverlapping(Data.sRoutesGpx);

                                if (purged_routes != 0) {

                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.removed) + " " + purged_routes + " "
                                            + getResources().getString(R.string.duplicates) + " ", Toast.LENGTH_SHORT).show();

                                } else {

                                    Toast.makeText(TracksBrowserActivity.this, String.format(getResources().getString(R.string.tracks_converted), counter), Toast.LENGTH_LONG).show();
                                }
                                refreshMap();
                            }
                        }
                    });

            ListView listView = (ListView) layout.findViewById(R.id.listItems);
            ArrayAdapter<String> adapter = new ArrayAdapter(
                    this,
                    //android.R.layout.select_dialog_multichoice_material,
                    R.layout.dialog_multi_choice_item,
                    menu_entries
            );
            listView.setAdapter(adapter);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
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

    public void clearTracks() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String clearTextTitle = getResources().getString(R.string.dialog_clear_tracks);
        String clearText = getResources().getString(R.string.dialog_clear);
        String cancelText = getResources().getString(R.string.dialog_cancel);

        builder.setCancelable(true)
                .setTitle(clearTextTitle)
                .setPositiveButton(clearText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Data.sTracksGpx.clearTracks();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.tracks_cleared), Toast.LENGTH_SHORT).show();
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

        if (Data.sTracksGpx.getTracks().size() == 0) {

            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_tracks_in_memory), Toast.LENGTH_LONG).show();
            return;
        }

        final List<Track> sortedTracks = new ArrayList<>();
        final List<String> gpxTrackDisplayNames = GpxUtils.getTrackNamesSUnsorted_LengthTypeArity(Data.sTracksGpx.getTracks(), Data.sUnitsInUse, sortedTracks);

        final List<String> allNames = new ArrayList<>();
        allNames.addAll(gpxTrackDisplayNames);

        String[] menu_entries = new String[allNames.size()];
        menu_entries = allNames.toArray(menu_entries);

        final boolean selected_values[] = new boolean[allNames.size()];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String dialogTitle = getResources().getString(R.string.dialog_select_tracks_export);
        String buttonAll = getResources().getString(R.string.dialog_all);
        String buttonSelected = getResources().getString(R.string.dialog_selected);
        String buttonCancel = getResources().getString(R.string.dialog_cancel);
        gpxOut.clearTracks();
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

                            Toast.makeText(TracksBrowserActivity.this, getResources().getString(R.string.no_tracks_selected), Toast.LENGTH_SHORT).show();

                        } else {

                            ArrayList<Track> gpxTracksPickedByUser = new ArrayList<>();

                            for (String nameOfTrackPickedByUser : selectedNames) {

                                int idxOfTrack = gpxTrackDisplayNames.indexOf(nameOfTrackPickedByUser);
                                gpxTracksPickedByUser.add(sortedTracks.get(idxOfTrack));
                            }
                            gpxOut.addTracks(gpxTracksPickedByUser);

                            //showSaveTracksDialog();
                            exportTracks();
                        }
                    }
                })
                .setNegativeButton(buttonCancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setPositiveButton(buttonAll, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        gpxOut.addTracks(Data.sTracksGpx.getTracks());
                        exportTracks();
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

    private void exportTracks() {
        performGpxFileSave(REQUEST_CODE_EXPORT_TRACKS, Data.lastImportedExportedUri);
    }

    private void saveSelectedTracks(Uri uri) {
        Gpx gpxToSave = new Gpx();
        gpxToSave.addTracks(gpxOut.getTracks());
        GpxStreamIo.parseOut(gpxToSave, getOutputStreamFromUri(uri));
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.selected_tracks_exported), Toast.LENGTH_SHORT).show();
    }
}