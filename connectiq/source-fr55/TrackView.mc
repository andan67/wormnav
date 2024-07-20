
class TrackView extends TrackViewCommon {

    function initialize() {
        TrackViewCommon.initialize();
        fontsize = Graphics.FONT_XTINY;
        padding = 0.6 * Graphics.getFontAscent(fontsize);
    }
}