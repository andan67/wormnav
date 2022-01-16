class DataView extends DataViewCommon {

    function initialize(dataFields) {
        DataViewCommon.initialize(dataFields);
    }

    function onLayout(dc) {
        if(numberDataFields == 0) {
            return;
        }

        width = dc.getWidth();
        height = dc.getHeight();

        // define font sizes and layout parameters
        font = Graphics.FONT_SMALL;
        fontNumber = Graphics.FONT_NUMBER_MEDIUM;
        offset = 0.0;
        h = height;

        if(numberDataFields == 1) {
            font = Graphics.FONT_LARGE;
            fontNumber = Graphics.FONT_NUMBER_HOT;
            offset = -dc.getFontDescent(fontNumber);
        } else if(numberDataFields == 2) {
            font = Graphics.FONT_LARGE;
            fontNumber = Graphics.FONT_NUMBER_HOT;
            offset = 3.0 * height / 20.0;
            h = 7.0 * height / 20.0;
        } else {
            offset = 1.0 * height / 15.0;
            h = 13.0 * height / 45.0;
        }

         // top position of data label text in pixels from top of data field
        yl = 0.5 * (h - dc.getFontHeight(font) -  dc.getFontHeight(fontNumber) + dc.getFontDescent(fontNumber));
        // top position of data value text in pixels from top of data field
        yv = yl + dc.getFontAscent(font);
        w2 = 0.5 * width;
    }
}