WormNav
===================================

This project provides a simple 'worm' navigation feature for selected Garmin devices.
It consists of two apps:
- An Android companion app to manage gpx tracks and routes and send them to the Garmin device via bluetooth
- The ConnectIQ app for the Garmin device to navigate along the track and to run activities

**Installation and User Guide**
- Download the sources and build binaries using Android Studio and Eclipse with Garmin plugin
- Install Garmin's Connect app on Android device (prerequisite for transmission of tracks via bluetooth) 
- Deploy the built apps to the Android and Garmin devices
- Import tracks and routes with the Android companion app
- Couple Android and Garmin devices via bluetooth
- Send selected track or route to Garmin device
- Optionally reduce number of track/route points
- Open WormNav app on Garmin device
- Change settings as needed

**Features**
- Manage tracks and routes with companion app
- Standalone navigation along selected track using Garmin device
- Configurable auto lap and bread crumb navigation
- Map zoom in/out and center
- Show activity data on configurable data screens
 
**Limitations**
- The ConnectIQ app is optimized for Garmin FR 23x, but also runnable on FR 245, FR 6xx, FR 735xt, vivoactive 3&4, Venu
- Potential issue with Garmin's Android BLE library on Android 11 devices