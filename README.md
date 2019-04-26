WormNav
===================================

This project provides a simple 'worm' navigation feature for Garmin devices.
It consists of two apps:
- An Android companion app to manage gpx tracks and routes and send them to the Garmin device via bluetooth.
- The ConnectIQ app for the Garmin device to navigate along the track and run activities.

**Notes**
- The apps are still in beta and currently only works for the Garmin devices Forerunner 230 and 235
- For performance and memory reasons, the number of track/way points used for the navigation on the Garmin device is limited. Thus it might be necessary to reduce the number of points of existing tracks or routes by the 'optimize' feature.

**Installation and User Guide**
- Download the sources and build binaries using Android Studio and Eclipse with Garmin plugin.
- Deploy the built apps to the Android and Garmin devices.
- Import tracks and routes with the Android companion app. If necessary, optimize the routes to limit the number of track or way points (150 might be a reasonable number). Tracks need to be converted to routes for applying optimization.
- Couple Android and Garmin devices via bluetooth
- Send selected track or route to Garmin device
- Open WormNav app on Garmin device
- Apply settings if necessary:
    - North heading of map on/off (off means rotate with track heading)
    - Autolap distance
 
 
**Limitations**
- Only one data page with four fixed fields 
