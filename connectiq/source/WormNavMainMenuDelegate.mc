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
        if (item == :center) {
            Transform.centerMap = !Transform.centerMap;
            Application.getApp().setProperty("centerMap", Transform.centerMap);
            return true;

        }
        if (item == :autolap) {
            var autolapMenu = new Rez.Menus.AutolapMenu();
            var s = "off";
            if(Trace.autolapDistance>0) {
                if(Trace.autolapDistance<1000) {
                    s = Trace.autolapDistance + "m";
                }
                else {
                    s = Trace.autolapDistance/1000 + "km";
                }
            }
            autolapMenu.setTitle("Autolap <" + s + ">");
            WatchUi.pushView(autolapMenu, new WormNavAutolapMenuDelegate(), WatchUi.SLIDE_UP);
            return true;
        }
        if (item == :breadCrumbs) {
            var breadCrumbsMenu = new Rez.Menus.BreadCrumbsMenu();
            var s = "off";
            if(Trace.breadCrumbDist>0) {
                if(Trace.breadCrumbDist<1000) {
                    s = Trace.breadCrumbDist + "m";
                }
                else {
                    s = Trace.breadCrumbDist/1000 + "km";
                }
            }
            breadCrumbsMenu.setTitle("Bread crumbs <" + s + ">");
            WatchUi.pushView(breadCrumbsMenu, new WormNavBreadCrumbsMenuDelegate(), WatchUi.SLIDE_UP);
            return true;
        }


        return false;
    }
}