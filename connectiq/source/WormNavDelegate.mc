using Toybox.WatchUi;
using Transform;

var eventText;

enum {
	TRACK_MODE,
	DATA_MODE
}

var mode;

var dataPage = 0;

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
        switch(mode) {
        	case TRACK_MODE:
        		Transform.zoomOut();
        		WatchUi.requestUpdate();
        		break;
        	case DATA_MODE:
        		dataPageChange(1);
				break;
		}
        return true;
    }

    // When a previous page behavior occurs, onPreviousPage() is called.
    // @return [Boolean] true if handled, false otherwise
    function onPreviousPage() {
        System.println("onPreviousPage()");
         switch(mode) {
        	case TRACK_MODE:
        		Transform.zoomIn();
        		WatchUi.requestUpdate();
        		break;
        	case DATA_MODE:
        		dataPageChange(-1);
				break;
		}
		return true;
    }
    
    
	private function dataPageChange(n) {
		if(Data.activeDataScreens.size() == 0) {
        	// this might happen when data screen settings have been changed
			onBack();
        } else {
			dataPage = (dataPage + n) % Data.activeDataScreens.size();
			dataView.setDataFields(Data.activeDataScreens[dataPage]);
			WatchUi.switchToView(dataView, self, WatchUi.SLIDE_IMMEDIATE);
		}
		return;
	}

    // When a back behavior occurs, onBack() is called.
    // @return [Boolean] true if handled, false otherwise
    function onBack() {
        System.println("onBack()");

        if(mode==TRACK_MODE && ( session != null ) && Data.activeDataScreens.size() > 0 ) {
            System.println("session is recording");
			if(dataView==null) {
				//var dataFields = [Data.TIMER, Data.DISTANCE, Data.AVGERAGE_PACE, Data.CURRENT_HEART_RATE];
               	dataView = new WormNavDataView(Data.activeDataScreens[dataPage]);
            }
            System.println("switch to data view");
			mode=DATA_MODE;
            WatchUi.switchToView(dataView, self, WatchUi.SLIDE_IMMEDIATE);
        }
        else if(mode==TRACK_MODE && (session == null || session.isRecording() == false)) {
           System.exit();
        }
        else if(mode==DATA_MODE) {
            if( session != null  &&  session.isRecording() == false ) {
                WatchUi.pushView(new Rez.Menus.SaveMenu(), new WormNavSaveMenuDelegate(), WatchUi.SLIDE_UP);
            }
            else if(session !=null) {
                mode=TRACK_MODE;
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
       	var menu = new Rez.Menus.MainMenu();
       	menu.setTitle("Main Menu");
        WatchUi.pushView(menu, new WormNavMainMenuDelegate(), WatchUi.SLIDE_IMMEDIATE);
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