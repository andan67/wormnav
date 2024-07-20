
class TrackView extends TrackViewCommon {

    function initialize() {
        TrackViewCommon.initialize();
        fontsize = Graphics.FONT_XTINY;
        padding = Graphics.getFontAscent(fontsize);
    }
}