using Toybox.WatchUi;
using Toybox.System;

class DiscardConfirmationDelegate extends WatchUi.ConfirmationDelegate {
    
    function initialize() {
        ConfirmationDelegate.initialize();
    }
    
    
    function onResponse(response) {
        if (response == WatchUi.CONFIRM_NO) {
            //System.println("Cancel");
            //return true;
        } else {
            //System.println("Confirm");
            session.discard();
            System.exit();            
        }
        //return false;
    }
}

class SaveMenuDelegate extends WatchUi.MenuInputDelegate {

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
            //session.discard();
            //System.exit();
            
        	//ASK_USER:
            //var message = "Exit App?";
            var message = WatchUi.loadResource(Rez.Strings.msg_discard);
                    
            var dialog = new WatchUi.Confirmation(message);
            WatchUi.pushView(
                        dialog,
                        new DiscardConfirmationDelegate(),
                        WatchUi.SLIDE_IMMEDIATE
                    ); 
            
            return true;              
            
            return true;
        }
        return false;
    }

}