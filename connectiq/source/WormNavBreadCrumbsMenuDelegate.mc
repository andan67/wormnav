using Toybox.WatchUi;
using Toybox.System;
using Trace;

class WormNavBreadCrumbsMenuDelegate extends WatchUi.MenuInputDelegate {

    function initialize() {
        MenuInputDelegate.initialize();
    }

    function onMenuItem(item) {
        switch ( item ) {
            case :bcoff:
                setBreadCrumbs(0);
                break;
            case :bc100:
                setBreadCrumbs(100);
                break;
            case :bc200:
                setBreadCrumbs(200);
                break;
            case :bc400:
                setBreadCrumbs(400);
                break;
            case :bc500:
                setBreadCrumbs(500);
                break;
            case :bc1000:
                setBreadCrumbs(1000);
                break;
            case :bc2000:
                setBreadCrumbs(2000);
                break;
            case :bc5000:
                setBreadCrumbs(5000);
                break;
            default:
                return false;
        }
    }

    function setBreadCrumbs(distance) {
        Trace.breadCrumbDist = distance;
        Application.getApp().setProperty("breadCrumbDist", Trace.autolapDistance);
        return true;
    }

}