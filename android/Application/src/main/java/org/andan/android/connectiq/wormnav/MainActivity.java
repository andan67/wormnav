package org.andan.android.connectiq.wormnav;

import android.Manifest;
import android.media.MediaScannerConnection;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.karambola.geo.Units;
import pt.karambola.gpx.beans.Gpx;
import pt.karambola.gpx.io.GpxFileIo;
import pt.karambola.gpx.util.GpxUtils;

public class MainActivity extends Utils implements ActivityCompat.OnRequestPermissionsResultCallback {

    Intent fileExploreIntent;
    String currentPath;
    private final int REQUEST_CODE_PICK_DIR = 1;
    private final int REQUEST_CODE_PICK_FILE = 2;

    String fileName = "myfile";

    int filePickerAction = -1;
    private final int ACTION_OPEN = 1;
    private final int ACTION_SAVE_AS = 2;
    private final int ACTION_SAVE_AS_PICKER = 3;

    boolean saveInProgress;

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private static final String TAG = "MainActivity";

    private TextView mSaveAsDialogPath;
    private EditText mSaveAsDialogFilename;

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
    private static final int PERMISSION_REQUEST_WRITE_STORAGE = 1;
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 2;
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

        Data.firstRun = preferences.getBoolean("firstRun", true);

        if (Data.firstRun) {

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("firstRun", false);
            editor.apply();
        }

        loadSettings();
        if(Data.loadLastOpenFile && Data.loadedFileFullPath.length() > 0) {
            Log.d(TAG,"load last open file");
            externalGpxFile = Data.loadedFileFullPath;
            new openExternalGpxFile().execute();
        } else {
            Data.loadedFileFullPath = "";
            refreshLoadedDataInfo();
        }

        // up-front permission handling
        List<String> appPermissionsNeeded = new ArrayList<>();
        for(String permission : APP_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                appPermissionsNeeded.add(permission);
            }
        }
        // request permission
        if(!appPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, appPermissionsNeeded.toArray(new String[appPermissionsNeeded.size()]), PERMISSION_REQUEST);
        }


        mLocationAcquired = false;

        // create shared folder

        File folder = new File(Environment.getExternalStorageDirectory() + "/WormNav");
        if(!folder.exists()) {
            if(folder.mkdirs()) {
                Toast.makeText(MainActivity.this, getString(R.string.shared_folder_created), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "failed to create WormNav folder");
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.shared_folder_failure), Toast.LENGTH_SHORT).show();
                folder = new File(Environment.getExternalStorageDirectory() + "");
            }
        }
        Data.defaultDirectoryPath = folder.toString();
        Log.d(TAG, "Data.defaultDirectoryPath:" + Data.defaultDirectoryPath);

        fileExploreIntent = new Intent(
                FileBrowserActivity.INTENT_ACTION_SELECT_FILE,
                null,
                this,
                FileBrowserActivity.class
        );

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

                if(Data.sPoiGpx != null) {
                    /*
                     * Let's work on a copy of POI data, (to be saved or not on exit).
                     */
                    Data.sCopiedPoiGpx = Utils.copyPoiGpx(Data.sPoiGpx);
                    Data.sCopiedPoiGpx.resetIsChanged();

                    Intent i;
                    i = new Intent(MainActivity.this, PoiActivity.class);

                    startActivity(i);
                }
                else {
                    Toast.makeText(MainActivity.this, "Load poi first!", Toast.LENGTH_SHORT).show();
                }
            }

        });

        routesButton = (TableRow) findViewById(R.id.main_routes_btn);
        routesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if(Data.sRoutesGpx != null) {
                    Intent i;
                    i = new Intent(MainActivity.this, RoutesBrowserActivity.class);

                    startActivity(i);
                }
                else {
                    Toast.makeText(MainActivity.this, "Load route first!", Toast.LENGTH_SHORT).show();
                }
            }

        });

        tracksButton = (TableRow) findViewById(R.id.main_tracks_btn);
        tracksButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if(true || Data.sTracksGpx != null) {
                    Intent i;
                    i = new Intent(MainActivity.this, TracksBrowserActivity.class);

                    startActivity(i);
                }
                else {
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

                fileOpen();
            }
        });

        saveButton = (LinearLayout) findViewById(R.id.bar_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showSaveAsDialog();
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
            HashMap<String,Integer> permissionResultMap = new HashMap<>();

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResultMap.put(permissions[i],grantResults[i]);
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
                        fileOpen();
                        break;

                    case 2:
                        showSaveAsDialog();
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

    private void fileOpen() {
        filePickerAction = ACTION_OPEN;
        final String path = Data.loadedFileFullPath.length()>0? getParentFromFullPath( Data.loadedFileFullPath): Data.defaultDirectoryPath;
        fileExploreIntent.putExtra(
                FileBrowserActivity.startDirectoryParameter,
                path
        );
        startActivityForResult(
                fileExploreIntent,
                REQUEST_CODE_PICK_FILE
        );
    }

    private void setupDrawer() {
        // enable ActionBar app icon to behave as action to toggle nav drawer
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
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
        rotationCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Data.sAllowRotation = !Data.sAllowRotation;
                rotationCheckBox.setChecked(Data.sAllowRotation);
            }
        });

        final CheckBox loadLastOpenFileCheckBox = (CheckBox) layout.findViewById(R.id.loadLastOpenFile);
        loadLastOpenFileCheckBox.setChecked(Data.loadLastOpenFile);
        loadLastOpenFileCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Data.loadLastOpenFile = !Data.loadLastOpenFile;
                loadLastOpenFileCheckBox.setChecked(Data.loadLastOpenFile);
            }
        });

        builder.setTitle(getResources().getString(R.string.settings))
                .setIcon(R.drawable.ico_settings)
                .setCancelable(false)
                .setView(layout)
                .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        saveSettings();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume()");
        refreshLoadedDataInfo();
        //loadSettings();

    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = getIntent();

        if (intent != null) {

            final Uri data = intent.getData();

            if (data != null) {

                final String filePath = data.getEncodedPath();

                if (filePath != null && !filePath.isEmpty()) {

                    intent.setData(null);
                    externalGpxFile = filePath;
                    new openExternalGpxFile().execute();
                }
            }
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

        TextView openFile = (TextView) findViewById(R.id.open_file);
        Log.d(TAG,"onActivityResult:" + requestCode + "/" + resultCode + "/" + data.hasExtra(FileBrowserActivity.returnDirectoryParameter));
        if (requestCode == REQUEST_CODE_PICK_FILE) {
            if(resultCode == RESULT_CANCELED) {
                Data.lastPickedDirectory = data.getStringExtra(FileBrowserActivity.returnDirectoryParameter);
                switch (filePickerAction) {
                    case ACTION_SAVE_AS_PICKER:
                        mSaveAsDialogPath.setText(Data.lastPickedDirectory);
                        break;
                }
            }
            if (resultCode == RESULT_OK) {
                Data.lastPickedDirectory = data.getStringExtra(FileBrowserActivity.returnDirectoryParameter);
                String fileFullPath = data.getStringExtra(
                        FileBrowserActivity.returnFileParameter);
                switch (filePickerAction) {

                    case ACTION_OPEN:

                        externalGpxFile = fileFullPath;
                        new openExternalGpxFile().execute();

                        break;

                    case ACTION_SAVE_AS:
                        Data.lastPickedFileFullPath = fileFullPath;
                        mSaveAsDialogPath.setText(getParentFromFullPath(Data.lastPickedFileFullPath));
                        mSaveAsDialogFilename.setText(getBaseFileNameFromFullPath(Data.lastPickedFileFullPath));
                        break;
                }


            } else {
                /*
                Toast.makeText(
                        this,
                        getString(R.string.no_file_selected),
                        Toast.LENGTH_LONG).show();
                        */
            }
        }
        refreshLoadedDataInfo();
    }

    private void showSaveAsDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View saveAsLayout = inflater.inflate(R.layout.save_gpx_dialog_layout, null);

        mSaveAsDialogFilename = (EditText) saveAsLayout.findViewById(R.id.save_new_filename);
        mSaveAsDialogPath = saveAsLayout.findViewById(R.id.save_new_path);

        final Intent fileExploreIntent = new Intent(
                FileBrowserActivity.INTENT_ACTION_SELECT_FILE,
                null,
                this,
                FileBrowserActivity.class
        );

        final String path = Data.loadedFileFullPath.length()>0? getParentFromFullPath( Data.loadedFileFullPath):Data.defaultDirectoryPath;
        final String fileBaseName = Data.loadedFileFullPath.length()>0? getBaseFileNameFromFullPath(Data.loadedFileFullPath): "myfile";
        Data.lastPickedDirectory = path;
        mSaveAsDialogPath.setText(path);
        mSaveAsDialogFilename.setText(fileBaseName);

        final AppCompatImageButton pickButton = saveAsLayout.findViewById(R.id.save_pick_button);
        pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePickerAction = ACTION_SAVE_AS_PICKER;

                fileExploreIntent.putExtra(
                        FileBrowserActivity.startDirectoryParameter,
                        path
                );
                startActivityForResult(
                        fileExploreIntent,
                        REQUEST_CODE_PICK_FILE
                );
            }
        });

        String dialogTitle = getResources().getString(R.string.dialog_savegpx_saveasnew);
        String saveText = getResources().getString(R.string.dialog_save_changes_save);
        String saveAsText = getResources().getString(R.string.file_pick);
        String cancelText = getResources().getString(R.string.dialog_cancel);

        builder.setTitle(dialogTitle)
                .setView(saveAsLayout)
                .setIcon(R.drawable.map_save)
                .setCancelable(true)
                .setNeutralButton(cancelText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setPositiveButton(saveText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        fileName = mSaveAsDialogFilename.getText().toString().trim();
                        saveGpxDestructive(Data.lastPickedDirectory, fileName);

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
        mSaveAsDialogFilename.addTextChangedListener(validate_name);
    }

    private void saveGpxDestructive(String path, String filename) {

        if (Data.sPoiGpx.getPoints().size() == 0 && Data.sRoutesGpx.getRoutes().size() == 0 && Data.sTracksGpx.getTracks().size() == 0) {
            Toast.makeText(this, getString(R.string.nothing_to_save), Toast.LENGTH_LONG).show();
            return;
        }

        boolean path_ok;

        File folder = new File(path);

        path_ok = folder.exists() || folder.mkdirs();

        if (path_ok) {

            final String new_file = folder.toString() + "/" + filename + ".gpx";

            if (new File(new_file).exists()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                String dialogTitle = getResources().getString(R.string.dialog_overwrite_title);
                String dialogMessage = getResources().getString(R.string.dialog_overwrite_message);
                String saveText = getResources().getString(R.string.dialog_save_changes_save);
                String cancelText = getResources().getString(R.string.dialog_cancel);

                builder.setTitle(dialogTitle)
                        .setIcon(R.drawable.map_warning)
                        .setMessage(dialogMessage)
                        .setCancelable(true)
                        .setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                        .setPositiveButton(saveText, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                String savingPoi = String.format(getString(R.string.poi_loaded), Data.sPoiGpx.getPoints().size());
                                String savingRoutes = String.format(getString(R.string.routes_loaded), Data.sRoutesGpx.getRoutes().size());
                                String savingTracks = String.format(getString(R.string.tracks_loaded), Data.sTracksGpx.getTracks().size());
                                Toast.makeText(getApplicationContext(), getString(R.string.saving) + " " + savingPoi + ", " + savingRoutes + ", " + savingTracks, Toast.LENGTH_LONG).show();

                                Data.mGpx = new Gpx();

                                Data.mGpx.addPoints(Data.sPoiGpx.getPoints());
                                Data.mGpx.addRoutes(Data.sRoutesGpx.getRoutes());
                                Data.mGpx.addTracks(Data.sTracksGpx.getTracks());

                                GpxFileIo.parseOut(Data.mGpx, new_file);
                            }
                        });

                AlertDialog alert = builder.create();

                alert.show();

            } else {

                // Just save
                String savingPoi = String.format(getString(R.string.poi_loaded), Data.sPoiGpx.getPoints().size());
                String savingRoutes = String.format(getString(R.string.routes_loaded), Data.sRoutesGpx.getRoutes().size());
                String savingTracks = String.format(getString(R.string.tracks_loaded), Data.sTracksGpx.getTracks().size());
                Toast.makeText(getApplicationContext(), getString(R.string.saving) + " " + savingPoi + ", " + savingRoutes + ", " + savingTracks, Toast.LENGTH_LONG).show();

                Data.mGpx = new Gpx();

                Data.mGpx.addPoints(Data.sPoiGpx.getPoints());
                Data.mGpx.addRoutes(Data.sRoutesGpx.getRoutes());
                Data.mGpx.addTracks(Data.sTracksGpx.getTracks());

                GpxFileIo.parseOut(Data.mGpx, new_file);

            }

            TextView openFile = (TextView) findViewById(R.id.open_file);
            Data.loadedFileFullPath = new_file;

            try {
                String[] splitFullPath = new_file.split("/");
                String filaname = splitFullPath[splitFullPath.length - 1];
                openFile.setText(filaname);
            } catch (Exception e) {
                openFile.setText(String.valueOf(e));
            }

        } else {

            Toast.makeText(getApplicationContext(), getString(R.string.failed_writing_gpx), Toast.LENGTH_LONG).show();
        }
    }

    public void refreshLoadedDataInfo() {

        Log.d(TAG, "refreshLoadedDataInfo():" + Data.loadedFileFullPath);
        TextView openFile = (TextView) findViewById(R.id.open_file);
        openFile.setText(getBaseFileNameFromFullPath(Data.loadedFileFullPath));

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

                        showSaveAsDialog();
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

    private void handleCorruptedFileError(String message, File file, Gpx gpx) {

        try {
            file.createNewFile();
            if (gpx == Data.sPoiGpx) {

                Data.sPoiGpx = new Gpx();
                GpxFileIo.parseOut(Data.sPoiGpx, file);

            } else if (gpx == Data.sRoutesGpx) {

                Data.sRoutesGpx = new Gpx();
                GpxFileIo.parseOut(Data.sRoutesGpx, file);

            } else if (gpx == Data.sTracksGpx) {

                Data.sTracksGpx = new Gpx();
                GpxFileIo.parseOut(Data.sTracksGpx, file);
            }


        } catch (Exception e) {
            Log.d(TAG, "Failed creating " + file.toString());
        }
        Toast.makeText(getApplicationContext(), message + " " + getString(R.string.default_file_corrupted), Toast.LENGTH_LONG).show();

    }

    private class openExternalGpxFile extends
            AsyncTask<Void, Boolean, Void> {

        AlertDialog alert;

        int purger_pois, purged_routes;

        @Override
        protected Void doInBackground(Void... params) {

            Gpx gpxIn = new Gpx();

            try {
                gpxIn = GpxFileIo.parseIn(externalGpxFile);

            } catch (Exception e) {

                Toast.makeText(getApplicationContext(), getString(R.string.error_opening_file) + " " + e, Toast.LENGTH_SHORT).show();

            }
            if (gpxIn != null) {
                Data.loadedFileFullPath = externalGpxFile;
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