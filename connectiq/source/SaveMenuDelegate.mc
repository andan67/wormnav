using Toybox.WatchUi;
using Toybox.System;

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
            var message = WatchUi.loadResource(Rez.Strings.msg_exit_app);
                    
            var dialog = new WatchUi.Confirmation(message);
            WatchUi.pushView(
                        dialog,
                        new ExitConfirmationDelegate(),
                        WatchUi.SLIDE_IMMEDIATE
                    ); 
            
            return true;              
            
            return true;
        }
        return false;
    }

}