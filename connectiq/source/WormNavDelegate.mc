using Toybox.WatchUi;
using MenuDelegates;
using Toybox.Lang;
using Track;

class ExitConfirmationDelegate extends WatchUi.ConfirmationDelegate {

    function initialize() {
        ConfirmationDelegate.initialize();
    }


    function onResponse(response) {
        if (response == WatchUi.CONFIRM_NO) {
            return false;
        } else {
            if($.session != null) {
                $.session.discard();
            }
            System.exit();
            return true;
        }
    }
}

class SaveMenuDelegate extends WatchUi.MenuInputDelegate {

    function initialize() {
        MenuInputDelegate.initialize();
    }

    function onMenuItem(item) {
        if (item == :resume) {
            //session.start();
            return true;
        } else if (item == :save) {
            session.save();
            System.exit();
            return true;
        } else {

            var message = WatchUi.loadResource(Rez.Strings.msg_discard);

            var dialog = new WatchUi.Confirmation(message);
            WatchUi.pushView(
                        dialog,
                        new ExitConfirmationDelegate(),
                        WatchUi.SLIDE_IMMEDIATE
                    );

            return true;
        }
        return false;
    }

}

class WormNavDelegate extends WatchUi.BehaviorDelegate {


    // Workaround on FR230 and 235 where holding menu button also fires onPreviousPage
    // see https://forums.garmin.com/developer/connect-iq/f/discussion/4294/fr230-holding-menu-button-triggers-onpreviouspage-and-then-onmenu
    var onPrevPageTick = 0;
    var lastPage = 1;

    function initialize() {
        BehaviorDelegate.initialize();
    }

    function onKey(keyEvent) {
        // System.println("onKey:" + keyEvent.getKey());
        if(keyEvent.getKey() == WatchUi.KEY_ENTER || keyEvent.getKey == WatchUi.KEY_START) {
            startStopActivity();
            return true;
        }
        return false;
    }

    // When a next page behavior occurs, onNextPage() is called.
    // @return [Boolean] true if handled, false otherwise
    function onNextPage() {
        //System.println("onNextPage()");
        if($.page == -1) {
            $.trackView.setZoomLevel(-2);
            updateView();
        } else {
            dataPageChange(1);
        }
        return true;
    }

    // When a previous page behavior occurs, onPreviousPage() is called.
    // @return [Boolean] true if handled, false otherwise
    function onPreviousPage() {
        //System.println("onPreviousPage()");
        if($.page == -1) {
            $.trackView.setZoomLevel(-1);
            updateView();
        } else {
            dataPageChange(-1);
        }
        return true;
    }

    // When a back behavior occurs, onBack() is called.
    // @return [Boolean] true if handled, false otherwise
    function onBack() {
        //System.println("onBack");
        // If active session is stopped asked for discard/save/resume
        if( $.session != null  &&  $.session.isRecording() == false ) {
            WatchUi.pushView(new Rez.Menus.SaveMenu(), new SaveMenuDelegate(), WatchUi.SLIDE_UP);
            return true;
        }

        // If there is no session exit;
        if ($.session == null || $.session.isRecording() == false) {
            var message = WatchUi.loadResource(Rez.Strings.msg_exit_app);

            var dialog = new WatchUi.Confirmation(message);
            WatchUi.pushView(
                        dialog,
                        new ExitConfirmationDelegate(),
                        WatchUi.SLIDE_IMMEDIATE
                    );

            return true;

        }

        if($.page == -1) {
            // track view -> swtich to data view (page > 0) or elevation plot (page == 0)
            if(lastPage == 0 && Track.hasElevation()) {
                $.page = 0;
                $.trackView.showElevationPlot = true;
                WatchUi.switchToView($.trackView, self, WatchUi.SLIDE_IMMEDIATE);
            } else if(Data.activeDataScreens.size() > 0 ) {
                if(lastPage == 0) {
                    // elevation plot disabled
                    $.page = 1;
                } else {
                    $.page = lastPage;
                }
                if(dataView == null) {
                    dataView = new DataView(Data.activeDataScreens[$.page - 1]);
                } else {
                    $.dataView.setDataFields(Data.activeDataScreens[$.page - 1]);
                }
                WatchUi.switchToView($.dataView, self, WatchUi.SLIDE_IMMEDIATE);
            }
        }
        else {
            lastPage = $.page;
            $.page = -1;
            // switch to track view
            $.trackView.showElevationPlot = false;
            WatchUi.switchToView($.trackView, self, WatchUi.SLIDE_IMMEDIATE);
        }
        return true;
    }

    function onMenu() {
        //Workaround for issue
        if($.page == -1 && $.appTimerTicks - onPrevPageTick < 3) {
            // counteract
            onNextPage();
        }

        var menu = new ListMenu(:MainMenu, WatchUi.loadResource(Rez.Strings.mm_title),
                                [:info, :orient, :breadcrumbs, :autolap, :activity, :course, :screens, :background],
                                WatchUi.loadResource(Rez.Strings.mm_labels),
                                null, null, 1, null);
        WatchUi.pushView(menu, new ListMenuDelegate (menu, new MenuDelegates.MainMenuDelegate (menu)), WatchUi.SLIDE_IMMEDIATE);

        return true;
    }

    private function startStopActivity() {
       if( Toybox has :ActivityRecording ) {
            if( ( $.session == null ) || ( $.session.isRecording() == false ) ) {
                if($.session == null) {
                    var sname = "WormNavActivity";
                    if($.track != null && $.track.name != null) {
                        sname = $.track.name.substring(0, $.track.name.length() < 16 ? $.track.name.length() : 15);
                    }
                    $.session = ActivityRecording.createSession({:name => sname, :sport => $.activityType});
                }
                $.session.start();
                $.sessionEvent = 1;
                updateView();
                if (Attention has :playTone) {
                    Attention.playTone(Attention.TONE_START);
                }
            }
            else if( ( $.session != null ) && $.session.isRecording() ) {
                // System.println("stop session");
                $.session.stop();
                $.sessionEvent = 2;
                updateView();
                if (Attention has :playTone) {
                    Attention.playTone(Attention.TONE_STOP);
                }
            }
        }
    }

    private function dataPageChange(n) {
        if(Data.activeDataScreens.size() == 0 && !$.trackView.showElevationPlot) {
            // this might happen settings have been changed
            onBack();
        } else {
            $.page += n;
            if($.page > Data.activeDataScreens.size()) {
                $.page = Track.hasElevation()? 0 : 1;
            } else if($.page == 0 && !Track.hasElevation() || $.page < 0 && Track.hasElevation() ) {
                $.page = Data.activeDataScreens.size();
            }
            if($.page > 0) {
                if(dataView == null) {
                    dataView = new DataView(Data.activeDataScreens[$.page - 1]);
                } else {
                    $.dataView.setDataFields(Data.activeDataScreens[$.page - 1]);
                }
                WatchUi.switchToView($.dataView, self, WatchUi.SLIDE_IMMEDIATE);
            } else {
                $.trackView.showElevationPlot = true;
                WatchUi.switchToView($.trackView, self, WatchUi.SLIDE_IMMEDIATE);
            }
        }
        return;
    }

    private function updateView() {
        if($.page <= 0) {
            $.trackViewCounter = 0;
        }
        WatchUi.requestUpdate();
    }

}