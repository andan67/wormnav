using Toybox.WatchUi;

class GenericView extends WatchUi.View {
    var foregroundColor = Graphics.COLOR_BLACK;
    var backgroundColor = Graphics.COLOR_WHITE;
    var trackColor = Graphics.COLOR_RED;

    function initialize() {
        View.initialize();
        setDarkMode($.isDarkMode);
    }

    function drawStartStop(dc) {
         if($.sessionEvent == 1) {
            // display activity started symbol
            dc.setColor(Graphics.COLOR_DK_GREEN, Graphics.COLOR_TRANSPARENT);
            dc.fillPolygon([
                [Transform.pixelWidth2 - 0.12*Transform.pixelMin,Transform.pixelHeight2 - 0.21*Transform.pixelMin ],
                [Transform.pixelWidth2 - 0.12*Transform.pixelMin,Transform.pixelHeight2 + 0.21*Transform.pixelMin ],
                [Transform.pixelWidth2 + 0.24*Transform.pixelMin,Transform.pixelHeight2 ]]);
        } else if($.sessionEvent == 2) {
            // display activity stopped symbol
            dc.setColor(Graphics.COLOR_DK_RED, Graphics.COLOR_TRANSPARENT);
            dc.fillRectangle(Transform.pixelWidth2 - 0.21*Transform.pixelMin,
                Transform.pixelHeight2 - 0.21*Transform.pixelMin, 0.42*Transform.pixelMin, 0.42*Transform.pixelMin);
        }
    }

    function setDarkMode(isDarkMode) {
        if(isDarkMode) {
            foregroundColor = Graphics.COLOR_WHITE;
            backgroundColor = Graphics.COLOR_BLACK;
            trackColor = Graphics.COLOR_LT_GRAY;
        }
        else {
            foregroundColor = Graphics.COLOR_BLACK;
            backgroundColor = Graphics.COLOR_WHITE;
            trackColor = Graphics.COLOR_RED;
        }
    }
}