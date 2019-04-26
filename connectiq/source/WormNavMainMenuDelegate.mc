using Toybox.WatchUi;
using Toybox.System;
using Trace;

class WormNavMainMenuDelegate extends WatchUi.MenuInputDelegate {

    function initialize() {
        MenuInputDelegate.initialize();
    }

    function onMenuItem(item) {
        if (item == :delete) {
            if(track!=null) {
                track=track.clean();
                Transform.setViewCenter(Trace.lat_last_pos,Trace.lon_last_pos);
                track=null;
                Trace.reset();
            }
            return true;

        }
        if (item == :north) {
            Transform.northHeading = !Transform.northHeading;
            Application.getApp().setProperty("northHeading", Transform.northHeading);
            return true;

        }
         if (item == :autolap) {
            var autolapMenu = new Rez.Menus.AutolapMenu();
            var als = "off";
            if(Trace.autolapDistance>0) {
                if(Trace.autolapDistance<1000) {
                    als = Trace.autolapDistance + "m";
                }
                else {
                    als = Trace.autolapDistance/1000 + "km";
                }
            }
            autolapMenu.setTitle("Autolap <" + als + ">");
            WatchUi.pushView(autolapMenu, new WormNavAutolapMenuDelegate(), WatchUi.SLIDE_UP);
            return true;

        }
        return false;
    }
}