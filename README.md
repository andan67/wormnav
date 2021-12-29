WormNav
===================================

This project provides a simple 'worm' navigation feature for selected Garmin devices.
It consists of two apps:
- An Android companion app to manage GPX tracks and routes and send them to the Garmin device via bluetooth
- The ConnectIQ app for the Garmin device to navigate along the track and to run activities

**Installation and User Guide**
- Download the sources and build binaries using Android Studio and Visual Studio Code with Monkey C extension
- Install Garmin's Connect app on Android device (prerequisite for transmission of tracks via bluetooth) 
- Deploy the built apps to the Android and Garmin devices
- Import tracks and routes with the Android companion app
- Couple Android and Garmin devices via bluetooth
- Send selected track or route to Garmin device
- Optionally reduce number of track/route points
- Open WormNav app on Garmin device
- Change settings as needed

For a comprehensive user guide see https://andan67.github.io/wormnav/

**Features**
- Manage tracks and routes with companion app
- Autonomous navigation with the Garmin device (i.e. no smartphone needed)
- Map zoom in/out and center
- Auto lap by configurable distance
- Show activity data on configurable data screens
- Optionally show track elevation profile
- Breadcrumb tracking by configurable distance or manual setting
 
**Limitations and knwon issues**
- The ConnectIQ app is optimized for Garmin FR 23x, but also runnable on FR 245, FR 6xx, FR 735xt, vivoactive 3&4, Venu/Venu 2
- Number of track points on FR 23x is limited to ~450 (without elevation data) and ~300 (with elevation data)