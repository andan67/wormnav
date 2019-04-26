using Toybox.WatchUi;
using Toybox.System;

class WormNavSaveMenuDelegate extends WatchUi.MenuInputDelegate {

    function initialize() {
        MenuInputDelegate.initialize();
    }

    function onMenuItem(item) {
        if (item == :resume) {
            session.start();
            return true;
        } else if (item == :save) {
            session.save();
            System.exit();
            return true;
        } else {
            session.discard();
            System.exit();
            return true;
        }
        return false;
    }

}