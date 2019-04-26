using Toybox.WatchUi;
using Transform;

var eventText;
var pageIndex;


class ExitConfirmationDelegate extends WatchUi.ConfirmationDelegate {
    function initialize() {
        ConfirmationDelegate.initialize();
    }

    function onResponse(response) {
        if (response == WatchUi.CONFIRM_NO) {
            System.println("Confirm No");
        } else {
            System.println("Confirm Yes");
        }
        return true;
    }
}

class WormNavDelegate extends WatchUi.BehaviorDelegate {

    function onKey(keyEvent) {
        return true;
    }

    function onKeyPressed(keyEvent) {
        //System.println("onKeyPressed:" + keyEvent.getKey());
        return false;
    }

    function onKeyReleased(keyEvent) {
        //System.println("onKeyReleased:");
        //mainView.setTextToDisplay(eventText);
        //WatchUi.requestUpdate();
        return false;
    }


    function initialize() {
        BehaviorDelegate.initialize();
    }

    // When a next page behavior occurs, onNextPage() is called.
    // @return [Boolean] true if handled, false otherwise

    function onNextPage() {
        System.println("onNextPage()");
        Transform.zoomOut();
        WatchUi.requestUpdate();
        return true;
    }


    // When a previous page behavior occurs, onPreviousPage() is called.
    // @return [Boolean] true if handled, false otherwise
    function onPreviousPage() {
        System.println("onPreviousPage()");
        Transform.zoomIn();
        WatchUi.requestUpdate();
        return true;
    }

    // When a back behavior occurs, onBack() is called.
    // @return [Boolean] true if handled, false otherwise
    function onBack() {
        System.println("onBack()");

        if(pageIndex==0 && ( session != null ) ) {
            System.println("session is recording");
            System.println("page index: " + pageIndex);
            if(dataView==null) {
               dataView = new WormNavDataView();
            }
            System.println("switch to data view");
            pageIndex=1;
            WatchUi.switchToView(dataView, self, WatchUi.SLIDE_IMMEDIATE);
        }
        else if(pageIndex==0) {
           System.exit();
        }
        else if(pageIndex==1) {
            if( session != null  &&  session.isRecording() == false ) {
                WatchUi.pushView(new Rez.Menus.SaveMenu(), new WormNavSaveMenuDelegate(), WatchUi.SLIDE_UP);
            }
            else if(session !=null) {
                pageIndex=0;
                WatchUi.switchToView(mainView, self, WatchUi.SLIDE_IMMEDIATE);
            }
        }
        return true;
    }

    // When a next mode behavior occurs, onNextMode() is called.
    // @return [Boolean] true if handled, false otherwise
    function onNextMode() {
        System.println("onNextMode()");
        return true;
    }

    // When a previous mode behavior occurs, onPreviousMode() is called.
    // @return [Boolean] true if handled, false otherwise
    function onPreviousMode() {
        System.println("onPreviousMode()");
        return true;
    }


    function onMenu() {
        System.println("onMenu()");
        var menu = new WatchUi.Menu();
        menu.setTitle("Main Menu");

        if(Transform.northHeading) {
            menu.addItem(WatchUi.loadResource(Rez.Strings.main_menu_label_1) + " off", :north);
        } else {
            menu.addItem(WatchUi.loadResource(Rez.Strings.main_menu_label_1) + " on", :north);
        }

        menu.addItem(WatchUi.loadResource(Rez.Strings.main_menu_label_2), :autolap);

        menu.addItem(Rez.Strings.main_menu_label_3, :delete);

        var delegate = new WormNavMainMenuDelegate(); // a WatchUi.MenuInputDelegate
        WatchUi.pushView(menu, delegate, WatchUi.SLIDE_IMMEDIATE);
        return true;
    }

    // When a previous mode behavior occurs, onPreviousMode() is called.
    // @return [Boolean] true if handled, false otherwise
    function onSelect() {
        System.println("onSelect()");
        if( Toybox has :ActivityRecording ) {
            if( ( session == null ) || ( session.isRecording() == false ) ) {
                System.println("start/resume session");
                if(session==null) {
                    session = ActivityRecording.createSession({:name=>"RUN", :sport=>ActivityRecording.SPORT_RUNNING});
                }
                Trace.lapTime = 0;
                Trace.elapsedlapTimeP = 0;
                Trace.elapsedLapDistanceP = 0.0;
                Trace.lapCounter = 0;
                Trace.lapPace = "";
                session.start();
                WatchUi.requestUpdate();
            }
            else if( ( session != null ) && session.isRecording() ) {
                System.println("stop session");
                session.stop();
                WatchUi.requestUpdate();
            }
        }

        //System.exit();
        return true;
    }

}