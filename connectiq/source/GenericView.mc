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

        var pixelwidth2 = 0.5 * dc.getWidth();
        var pixelHeight2 = 0.5 * dc.getHeight();
        var pixelMin = dc.getWidth() < dc.getHeight() ? dc.getHeight() : dc.getHeight();


        if($.sessionEvent == 1) {
        // display activity started symbol
            dc.setColor(Graphics.COLOR_DK_GREEN, Graphics.COLOR_TRANSPARENT);
            dc.fillPolygon([
                [pixelWidth2 - 0.12 * pixelMin, pixelHeight2 - 0.21 * pixelMin ],
                [pixelWidth2 - 0.12 * pixelMin, pixelHeight2 + 0.21 * pixelMin ],
                [pixelWidth2 + 0.24 * pixelMin, pixelHeight2 ]]);
        } else if($.sessionEvent == 2) {
            // display activity stopped symbol
            dc.setColor(Graphics.COLOR_DK_RED, Graphics.COLOR_TRANSPARENT);
            dc.fillRectangle(pixelWidth2 - 0.21 * pixelMin,
                pixelHeight2 - 0.21 * pixelMin, 0.42 * pixelMin, 0.42 * pixelMin);
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