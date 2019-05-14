WormNav
===================================

## User Guide

This project provides a simple 'worm' navigation feature for selected Garmin devices.
It consists of two apps:
- An Android companion app to manage GPX tracks and routes and send them to the Garmin device via bluetooth.
- The ConnectIQ app for the Garmin device to navigate along the track and run activities.

### Installation and basic set-up
- Install [WormNav Companion app](https://play.google.com/store/apps/details?id=org.andan.android.connectiq.wormnav) from Google Play Store on your Android smartphone or tablet
- If not already done, install [Garmin Connect™ app](https://play.google.com/store/apps/details?id=com.garmin.android.apps.connectmobile) from Google Play Store
- Install [WormNav app](https://apps.garmin.com/de-DE/apps/07c0363a-a921-4c77-bb4f-93a6d6c4a2ed) from Connect IQ Store
- Connect your Gamin device with your Android smartphone/tablet via bluetooth

### Usage scenario
1. Choose or create a GPX track or route you would like to use for navigation with your Garmin device
2. Import this track/route into WormNav Companion app
3. Optimize the track/route to reduce number of waypoints
4. Connect your Garmin device with your smartphone/tablet via bluetooth
4. Send optimized track/route to connected Garmin device
5. Start activity on Garmin device
6. Follow shown track on Garmin device
7. Stop/resume/save activity

### Companion app

#### Key features and capabilities:
- Load GPX tracks and routes
- Show GPX tracks or routes in Open Street map
- Optimize routes (reduce number of waypoints given maximum error contraint)
- Store tracks and routes
- Send track/route to connected Garmin device

#### Screens

##### First run/Main screen
{::options parse_block_html="true" /}
<div style="float: left; width: 50%">
<img src="images/android_main_screen_loaded.png" width="300"/>
</div>
{::options parse_block_html="true" /}
<div style="float: right; width: 50%">
- On first run the app will ask to grant two permissions (STORAGE and LOCATION).
- Also the app will try to create a 'WormNav' subfolder in the external storage location that can be used to store all GPX files in one location. 
Of course, other accessible locations can also be used.
- Main functions available from the main screen:
    - Manage settings
    - Load or save GPX file with possibly multiple tracks or routes. If 'Open last loaded/saved file" is enabled in the settings (default), 
    the last loaded or saved GPX file will be opened on app start.
    - Manage loaded GPX tracks, routes, or points of interest (see below)
</div>
{::options parse_block_html="true" /}
<div style="clear: both"/>



##### Track/Route Manager - Import
<div style="float: left; width: 50%">
<img src="images/android_route_manager_import.png" width="300"/>
</div>

{::options parse_block_html="true" /}
<div style="float: right; width: 50%">
- Import GPX tracks or routes
- Delete loaded GPX tracks or routes
- The 'Save file' function will save all loaded tracks and routes into a single GPX file
</div>
{::options parse_block_html="true" /}
<div style="clear: both"/>

##### Track/Route Manager - Show tracks/routes on map
<div style="float: left; width: 50%">
<img src="images/android_route_manager_route.png" width="300"/>
</div>

{::options parse_block_html="true" /}
<div style="float: right; width: 50%">
- Show loaded tracks or routes on OpenStreet Map
- Select track/route by using arrows at screen buttom
</div>
{::options parse_block_html="true" /}
<div style="clear: both"/>

##### Route Manager - Optimize
<div style="float: left; width: 50%">
<img src="images/android_route_manager_optimize.png" width="300"/>
</div>

{::options parse_block_html="true" /}
<div style="float: right; width: 50%">
- Optimize route by reducing number of waypoints
- Shows maximum error caused by optimization
- Useful for performance reasons on low spec devices
- Recommendation for Garmin Forerunner 230/235: max 200 waypoints
- Tracks must be converted into routes for optimization
</div>
{::options parse_block_html="true" /}
<div style="clear: both"/>

##### Device Browser
<div style="float: left; width: 50%">
<img src="images/android_device_browser_success.png" width="300"/>
</div>

{::options parse_block_html="true" /}
<div style="float: right; width: 50%">
- Menu item 'Send selected to device' opens device browser with list of Garmin devices
- Shows status of last sent
- Click on device entry opens dialog to send selected track/route to connected device
</div>
{::options parse_block_html="true" /}
<div style="clear: both"/>

##### Send to device dialog
<div style="float: left; width: 50%">
<img src="images/android_send_to_device_menu.png" width="300"/>
</div>

{::options parse_block_html="true" /}
<div style="float: right; width: 50%">
- Click on 'Send' button to send selected track/route to Garmin device
- Watch for last sent status message on Device Browser screen
- On success, track is stored on Garmin device and ready for use with WormNav app

</div>
{::options parse_block_html="true" /}
<div style="clear: both"/>

### Connect IQ app

#### Key features and capabilities:
- Show track and current position
- Start/stop/save activity
- Configurable auto lap
- Configurable 'bread crumbs' (i.e. trace)
- Zoom in/out of map with showing scale
- Show position or track in view center
- Fixed north or heads-up map orientation

#### Button usage
<img src="images/garmin_description_export.png" width="1024"/>

#### Screens

##### Main menu
<div style="float: left; width: 50%">
<img src="images/garmin_main_menu.png" width="300"/>
</div>

{::options parse_block_html="true" /}
<div style="float: right; width: 50%">
Available menu items:
- Map orientation (north or heads-up)
- Auto lap distance (0 means off)
- Bread crumbs distance (0 means off)
- Track center view on/off
- Delete track

</div>
{::options parse_block_html="true" /}
<div style="clear: both"/>

##### Map view (north map orientation)
<div style="float: left; width: 50%">
<img src="images/garmin_track_nav_standard_2.png" width="300"/>
</div>

{::options parse_block_html="true" /}
<div style="float: right; width: 50%">
Standard view of map/track when activity has started
- Elapsed time and distance shown on top
- Track is shown as red line with north in up-direction
- Position cursor points into direction of movement 
- Bread crumbs are shown as blue filled circles
- Scale and compass at bottom

</div>
{::options parse_block_html="true" /}
<div style="clear: both"/>

##### Map view (heads-up)
<div style="float: left; width: 50%">
<img src="images/garmin_track_nav_heads_up.png" width="300"/>
</div>

{::options parse_block_html="true" /}
<div style="float: right; width: 50%">
Heads-up view of map/track
- Position cursor has fixed up-orientation  
- Track and compass are rotated according to actual direction of movement  

</div>
{::options parse_block_html="true" /}
<div style="clear: both"/>

##### Map view (track centered)
<div style="float: left; width: 50%">
<img src="images/garmin_track_nav_centered_2.png" width="300"/>
</div>

{::options parse_block_html="true" /}
<div style="float: right; width: 50%">
Map view with center of track as view center
- North is always in up-direction
- Useful for getting overview on position on or distance to track

</div>
{::options parse_block_html="true" /}
<div style="clear: both"/>

##### Data page
<div style="float: left; width: 50%">
<img src="images/garmin_data_page_2.png" width="300"/>
</div>

{::options parse_block_html="true" /}
<div style="float: right; width: 50%">
Data page with 4 fixed data fields
- Elapsed time
- Elapsed distance in km
- Average pace in min/km
- Heart rate (if sensor exists)

</div>
{::options parse_block_html="true" /}
<div style="clear: both"/>