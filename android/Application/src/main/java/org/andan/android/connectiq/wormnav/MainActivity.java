package org.andan.android.connectiq.wormnav;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.karambola.geo.Units;
import pt.karambola.gpx.beans.Gpx;
import pt.karambola.gpx.io.GpxFileIo;
import pt.karambola.gpx.io.GpxStreamIo;
import pt.karambola.gpx.util.GpxUtils;

public class MainActivity extends Utils implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final int REQUEST_CODE_LOAD_FILE = 1;
    private final int REQUEST_CODE_SAVE_FILE = 2;

    boolean saveInProgress;

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private static final String TAG = MainActivity.class.getName();

    boolean mLocationAcquired = false;

    TableRow poisButton;
    TableRow routesButton;
    TableRow tracksButton;

    LinearLayout newButton;
    LinearLayout openButton;
    LinearLayout saveButton;

    ListView list;

    String[] web = new String[3];

    Integer[] imageId = {
            R.drawable.bar_new,
            R.drawable.bar_open,
            R.drawable.bar_save
    };


    /* Id to identify Location permission request. */
    private static final int PERMISSION_REQUEST = 0;
    private static final String APP_PERMISSIONS[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ff006cb5")));
            actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#5500ffff")));
        }

        mTitle = mDrawerTitle = getTitle();

        preferences = getSharedPreferences("WormNavEditor", MODE_PRIVATE);

        loadSettings();

        Data.applicationFilesDir = getApplicationDir();
        Log.d(TAG, "applicationFilesDir:" + Data.applicationFilesDir.getPath());
                //  try loading last saved file from application dir
        if(Data.loadFromRepositoryOnStart && Data.applicationFilesDir != null && Data.applicationFilesDir.length()>0) {
            File inFile = new File(Data.applicationFilesDir, Data.applicationRepositoryFilename);
            try {
                new openExternalGpxFile().execute(new FileInputStream(inFile));
                Log.d(TAG, "onCreate: loaded from into: " + inFile.getPath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // up-front permission handling
        List<String> appPermissionsNeeded = new ArrayList<>();
        for (String permission : APP_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                appPermissionsNeeded.add(permission);
            }
        }
        // request permission
        if (!appPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, appPermissionsNeeded.toArray(new String[appPermissionsNeeded.size()]), PERMISSION_REQUEST);
        }


        mLocationAcquired = false;

        web[0] = getResources().getString(R.string.new_data);
        web[1] = getResources().getString(R.string.open_gpx);
        web[2] = getResources().getString(R.string.save_gpx);

        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        setAppsMapQuestKey();

        addDrawerItems();
        setupDrawer();

        poisButton = (TableRow) findViewById(R.id.main_poi_btn);
        poisButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (Data.sPoiGpx != null) {
                    /*
                     * Let's work on a copy of POI data, (to be saved or not on exit).
                     */
                    Data.sCopiedPoiGpx = Utils.copyPoiGpx(Data.sPoiGpx);
                    Data.sCopiedPoiGpx.resetIsChanged();

                    Intent i;
                    i = new Intent(MainActivity.this, PoiActivity.class);

                    startActivity(i);
                } else {
                    Toast.makeText(MainActivity.this, "Load poi first!", Toast.LENGTH_SHORT).show();
                }
            }

        });

        routesButton = (TableRow) findViewById(R.id.main_routes_btn);
        routesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (Data.sRoutesGpx != null) {
                    Intent i;
                    i = new Intent(MainActivity.this, RoutesBrowserActivity.class);

                    startActivity(i);
                } else {
                    Toast.makeText(MainActivity.this, "Load route first!", Toast.LENGTH_SHORT).show();
                }
            }

        });

        tracksButton = (TableRow) findViewById(R.id.main_tracks_btn);
        tracksButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (true || Data.sTracksGpx != null) {
                    Intent i;
                    i = new Intent(MainActivity.this, TracksBrowserActivity.class);

                    startActivity(i);
                } else {
                    Toast.makeText(MainActivity.this, "Load track first!", Toast.LENGTH_SHORT).show();
                }
            }

        });

        newButton = (LinearLayout) findViewById(R.id.bar_new);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clearData();
            }
        });

        openButton = (LinearLayout) findViewById(R.id.bar_open);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performGpxFileSearch(REQUEST_CODE_LOAD_FILE, Data.lastLoadedSavedUri);
            }
        });

        saveButton = (LinearLayout) findViewById(R.id.bar_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performGpxFileSave(REQUEST_CODE_SAVE_FILE,  Data.lastLoadedSavedUri);
            }
        });

    }

    /*
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST) {
            // Gather grant permission result
            HashMap<String, Integer> permissionResultMap = new HashMap<>();

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResultMap.put(permissions[i], grantResults[i]);
                }
            }
            // all permission must be granted, otherwise notify about denied permission and close app
            if (!permissionResultMap.isEmpty()) {
                for (String deniedPermission : permissionResultMap.keySet()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.permissions_insufficient) + " " + deniedPermission, Toast.LENGTH_LONG).show();
                }
                finish();
            }
        }
    }

    private void addDrawerItems() {

        CustomList adapter = new
                CustomList(MainActivity.this, web, imageId);
        list = (ListView) findViewById(R.id.navList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                switch (position) {
                    case 0:
                        clearData();
                        break;

                    case 1:
                        performGpxFileSearch(REQUEST_CODE_LOAD_FILE, Data.lastLoadedSavedUri);
                        break;

                    case 2:
                        performGpxFileSave(REQUEST_CODE_SAVE_FILE,  Data.lastLoadedSavedUri);
                        break;

                    default:
                        break;
                }

            }
        });
    }

    private void clearData() {

        if (Data.sPoiGpx.isChanged() || Data.sRoutesGpx.isChanged() || Data.sTracksGpx.isChanged()) {

            displayDataChangedDialog();

        } else {

            displayConfirmClearDialog();
        }
        refreshLoadedDataInfo();
    }

    private void setupDrawer() {
        // enable ActionBar app icon to behave as action to toggle nav drawer
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setIcon(R.mipmap.ic_launcher);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerToggle.setDrawerIndicatorEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        /* Here we can enable/disable menu items as shown below:
         *
         * menu.findItem(R.id.action_lv).setEnabled(Utils.mPeerId != null);
         */
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_about:
                displayAboutDialog();
                return true;

            case R.id.action_credits:
                displayCreditsDialog();
                return true;

            case R.id.action_settings:
                displaySettingsDialog();
                return true;

            case R.id.action_exit:
                //showSaveAsDialog();
                finish();
                return true;
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayAboutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.about_dialog, null);

        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            TextView version = (TextView) layout.findViewById(R.id.version_name);
            version.setText(String.format(getResources().getString(R.string.version_name), packageInfo.versionName));
        }

        final TextView gnu = (TextView) layout.findViewById(R.id.gnu);
        final TextView github = (TextView) layout.findViewById(R.id.github);

        String dialogTitle = getResources().getString(R.string.dialog_about);
        String okText = getResources().getString(R.string.dialog_ok);
        builder.setTitle(dialogTitle)
                .setIcon(R.mipmap.ic_launcher_round)
                .setCancelable(false)
                .setView(layout)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });


        AlertDialog alert = builder.create();

        gnu.setMovementMethod(LinkMovementMethod.getInstance());
        github.setMovementMethod(LinkMovementMethod.getInstance());

        alert.show();
    }

    private void displayCreditsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.credits_dialog, null);

        final List<TextView> clickableFields = new ArrayList<>();
        clickableFields.add((TextView) layout.findViewById(R.id.rambler_name));
        clickableFields.add((TextView) layout.findViewById(R.id.rambler_licence));
        clickableFields.add((TextView) layout.findViewById(R.id.aosp_name));
        clickableFields.add((TextView) layout.findViewById(R.id.aosp_license));
        clickableFields.add((TextView) layout.findViewById(R.id.karambola_name));
        clickableFields.add((TextView) layout.findViewById(R.id.karambola_license));
        clickableFields.add((TextView) layout.findViewById(R.id.osmdroid_name));
        clickableFields.add((TextView) layout.findViewById(R.id.osmdroid_license));
        clickableFields.add((TextView) layout.findViewById(R.id.osmbp_name));
        clickableFields.add((TextView) layout.findViewById(R.id.osmbp_license));
        clickableFields.add((TextView) layout.findViewById(R.id.osrm_name));
        clickableFields.add((TextView) layout.findViewById(R.id.osrm_license));
        clickableFields.add((TextView) layout.findViewById(R.id.mapquest_name));
        clickableFields.add((TextView) layout.findViewById(R.id.mapquest_license));
        clickableFields.add((TextView) layout.findViewById(R.id.osrm_license_demo_server));
        clickableFields.add((TextView) layout.findViewById(R.id.filebrowser_name));
        clickableFields.add((TextView) layout.findViewById(R.id.filebrowser_license));

        String dialogTitle = getResources().getString(R.string.credits);
        String okText = getResources().getString(R.string.dialog_ok);
        builder.setTitle(dialogTitle)
                .setIcon(R.drawable.ico_info)
                .setCancelable(true)
                .setView(layout)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();

        for (TextView textView : clickableFields) {
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        alert.show();
    }

    private void displaySettingsDialog() {

        //loadSettings();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_settings_main, null);

        final Spinner units_spinner = (Spinner) layout.findViewById(R.id.units_spinner);
        ArrayAdapter<String> dataAdapterUnits = new ArrayAdapter<>(this, android.R.layout
                .simple_spinner_item, getResources().getStringArray(R.array.units_array));
        dataAdapterUnits.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        units_spinner.setAdapter(dataAdapterUnits);
        units_spinner.setSelection(Data.sUnitsInUse.getCode());
        units_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {

                switch (pos) {
                    case 0:
                        Data.sUnitsInUse = Units.METRIC;
                        break;
                    case 1:
                        Data.sUnitsInUse = Units.IMPERIAL;
                        break;
                    case 2:
                        Data.sUnitsInUse = Units.NAUTICAL;
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final CheckBox rotationCheckBox = (CheckBox) layout.findViewById(R.id.rotationCheckBox);
        rotationCheckBox.setChecked(Data.sAllowRotation);
        rotationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Data.sAllowRotation = isChecked;
            }
        });

        final CheckBox loadFromRepoCheckBox = (CheckBox) layout.findViewById(R.id.loadFromRepoCheckBox);
        loadFromRepoCheckBox.setChecked(Data.loadFromRepositoryOnStart);
        loadFromRepoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Data.loadFromRepositoryOnStart = isChecked;
            }
        });

        final CheckBox saveIntoRepoCheckBox = (CheckBox) layout.findViewById(R.id.saveIntoRepoCheckBox);
        saveIntoRepoCheckBox.setChecked(Data.saveIntoRepositoryOnExit);
        saveIntoRepoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Data.saveIntoRepositoryOnExit = isChecked;
            }
        });

        final CheckBox defaultReduceTrackCheckbox = (CheckBox) layout.findViewById(R.id.defaultReduceTrackCheckbox);

        final EditText maxWptEditText = layout.findViewById(R.id.default_reduceMaxPoints);
        maxWptEditText.setText(String.valueOf(Data.defaultMaxPathWpt));

        final EditText maxError = layout.findViewById(R.id.default_reduceMaxError);
        maxError.setText(String.valueOf(Data.defaultMaxPathError));


        defaultReduceTrackCheckbox.setChecked(Data.useDefaultOptimization);
        maxWptEditText.setEnabled(Data.useDefaultOptimization);
        maxError.setEnabled(Data.useDefaultOptimization);


        defaultReduceTrackCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                maxWptEditText.setEnabled(isChecked);
                maxError.setEnabled(isChecked);
                Log.d(TAG, "useDefaultOptimization:" + isChecked);
                Data.useDefaultOptimization = isChecked;
            }
        });

        builder.setTitle(getResources().getString(R.string.settings))
                .setIcon(R.drawable.ico_settings)
                .setCancelable(false)
                .setView(layout)
                .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!maxWptEditText.getText().toString().isEmpty()) {
                            Data.defaultMaxPathWpt = Integer.valueOf(maxWptEditText.getText().toString());
                        }
                        if (!maxError.getText().toString().isEmpty()) {
                            Data.defaultMaxPathError = Double.valueOf(maxError.getText().toString());
                        }
                        saveSettings();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        refreshLoadedDataInfo();
        //loadSettings();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(Data.saveIntoRepositoryOnExit) {
            Data.mGpx = new Gpx();

            Data.mGpx.addPoints(Data.sPoiGpx.getPoints());
            Data.mGpx.addRoutes(Data.sRoutesGpx.getRoutes());
            Data.mGpx.addTracks(Data.sTracksGpx.getTracks());
            File outFile = new File(Data.applicationFilesDir, Data.applicationRepositoryFilename);
            GpxFileIo.parseOut(Data.mGpx, outFile);
            //Log.d(TAG, "onDestroy: saved into: " + outFile.getPath());
            Log.d(TAG, "onDestroy: saved into: " + Data.applicationFilesDir.getAbsolutePath() + File.pathSeparator + Data.applicationRepositoryFilename);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (!saveInProgress) {
                //new saveDefaultDataFiles().execute();

                return true;

            } else {

                Toast.makeText(this, getString(R.string.saving_wait), Toast.LENGTH_SHORT).show();
                return false;
            }

        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_LOAD_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    Data.lastImportedExportedUri = uri;
                    Log.d(TAG, "Load file: " + uri.toString());
                    new openExternalGpxFile().execute(getInputStreamFromUri(uri));
                }
                refreshLoadedDataInfo();
                break;
            case REQUEST_CODE_SAVE_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    Data.lastImportedExportedUri = uri;
                    Log.d(TAG, "Save file: " + uri.toString());
                    saveGpx(getOutputStreamFromUri(uri));
                }
                refreshLoadedDataInfo();
                break;
            default:
                break;

        }
    }

    private File getApplicationDir() {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            return getExternalFilesDir(null);
        } else {
            return getFilesDir();
        }
    }

    public void refreshLoadedDataInfo() {

        Log.d(TAG, "refreshLoadedDataInfo():" + Data.lastLoadedSavedUri.toString());
        TextView openFile = (TextView) findViewById(R.id.open_file);
        openFile.setText(getBaseFileNameFromUri(Data.lastLoadedSavedUri));

        if (Data.sPoiGpx == null || Data.sRoutesGpx == null || Data.sTracksGpx == null) {
            return;
        }

        try {
            TextView poiStatus = (TextView) findViewById(R.id.poi_manager_text);
            poiStatus.setText(String.format(getString(R.string.main_poi_loaded), Data.sPoiGpx.getPoints().size()));

            TextView routesStatus = (TextView) findViewById(R.id.route_manager_text);
            routesStatus.setText(String.format(getString(R.string.main_routes_loaded), Data.sRoutesGpx.getRoutes().size()));

            TextView tracksStatus = (TextView) findViewById(R.id.track_manager_text);
            tracksStatus.setText(String.format(getString(R.string.main_tracks_loaded), Data.sTracksGpx.getTracks().size()));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getString(R.string.read_error) + e, Toast.LENGTH_SHORT).show();
        }
    }

    private void displayDataChangedDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String dialogTitle = getResources().getString(R.string.dialog_data_changed_title);
        String messageText = getResources().getString(R.string.dialog_data_changed_message);
        String saveText = getResources().getString(R.string.dialog_save);
        String dontSaveText = getResources().getString(R.string.dialog_dont_save);
        String cancelText = getResources().getString(R.string.dialog_cancel);

        builder.setMessage(messageText)
                .setTitle(dialogTitle)
                .setIcon(R.drawable.map_question)
                .setCancelable(false)
                .setPositiveButton(saveText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        //showSaveAsDialog();
                    }
                })
                .setNegativeButton(dontSaveText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Data.sPoiGpx = new Gpx();
                        Data.sRoutesGpx = new Gpx();
                        Data.sTracksGpx = new Gpx();

                        Data.sPoiGpx.resetIsChanged();
                        Data.sRoutesGpx.resetIsChanged();
                        Data.sTracksGpx.resetIsChanged();

                        refreshLoadedDataInfo();
                    }
                })
                .setNeutralButton(cancelText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void displayConfirmClearDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getResources().getString(R.string.dialog_confirm_clear_message))
                .setTitle(getResources().getString(R.string.dialog_confirm_clear_title))
                .setIcon(R.drawable.map_question)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Data.sPoiGpx = new Gpx();
                        Data.sRoutesGpx = new Gpx();
                        Data.sTracksGpx = new Gpx();

                        refreshLoadedDataInfo();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void saveGpx(OutputStream os) {
        Data.mGpx = new Gpx();

        Data.mGpx.addPoints(Data.sPoiGpx.getPoints());
        Data.mGpx.addRoutes(Data.sRoutesGpx.getRoutes());
        Data.mGpx.addTracks(Data.sTracksGpx.getTracks());
        Log.d(TAG, "saveGpx: " + Data.mGpx.getTracks().size());
        GpxStreamIo.parseOut(Data.mGpx, os);
    }

    private class openExternalGpxFile extends
            AsyncTask<InputStream, Boolean, Void> {

        AlertDialog alert;

        int purger_pois, purged_routes;

        @Override
        protected Void doInBackground(InputStream... params) {

            InputStream gpxInputStream = params[0];
            Gpx gpxIn = new Gpx();

            try {
                gpxIn = GpxStreamIo.parseIn(gpxInputStream);

            } catch (Exception e) {

                Toast.makeText(getApplicationContext(), getString(R.string.error_opening_file) + " " + e, Toast.LENGTH_SHORT).show();

            }
            if (gpxIn != null) {
                Data.sPoiGpx = new Gpx();
                Data.sPoiGpx.setPoints(gpxIn.getPoints());

                purger_pois = GpxUtils.purgePointsSimilar(Data.sPoiGpx);

                Data.sPoiGpx.resetIsChanged();

                Data.sRoutesGpx = new Gpx();
                Data.sRoutesGpx.setRoutes(gpxIn.getRoutes());

                purged_routes = GpxUtils.purgeRoutesOverlapping(Data.sRoutesGpx);

                Data.sRoutesGpx.resetIsChanged();

                Data.sTracksGpx = new Gpx();
                Data.sTracksGpx.setTracks(gpxIn.getTracks());

                Data.sTracksGpx.resetIsChanged();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            alert.dismiss();
            refreshLoadedDataInfo();

            if (purger_pois != 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.removed) + " " + purger_pois + " " + getString(R.string.duplicated_poi), Toast.LENGTH_SHORT).show();
            }

            if (purged_routes != 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.removed) + " " + purged_routes + " " + getString(R.string.overlapping_routes), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setIcon(R.drawable.wait)
                    .setTitle(R.string.dialog_loading_data)
                    .setCancelable(false);

            alert = builder.create();
            alert.show();

        }
    }
}