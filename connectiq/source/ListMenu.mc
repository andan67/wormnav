using Toybox.WatchUi as Ui;
using Toybox.System as Sys;
using Toybox.Graphics as Gfx;

using MenuDelegates;


// List backed menu based on https://github.com/davebaldwin/Dmenu published under MIT Licence

class ListMenu extends Ui.View
{
    const TITLE_FONT = Gfx.FONT_SMALL;
    const LABEL_FONT = Gfx.FONT_SMALL;
    const SELECTED_LABEL_FONT = Gfx.FONT_LARGE;
    const VALUE_FONT = Gfx.FONT_MEDIUM;
    const PAD = 0;

    var itemValueList;
    var initValue;
    var showSubMenuValues = false;
    var itemLabelList;
    var itemIdList;
    var menuArray;
    var nItems;
    var title;
    var index;
    var id;
    var options;

    var nextIndex;
    var menuHeight = null;
    var onMenuTicks = -1;

    // Initialize menu with
    // _id: id of menu
    // _menuTitle: title of menu
    // _itemIdList: id list of sub menu items
    // _itemLabelList: labels of sub menu items
    // _itemValueList: values of sub menu items
    // _initValue: initial (actual) value
    // _showSubMenuValues: if true actual value is shown under main menu item
    // _options: dictionary container for additional options

    function initialize (_id, _menuTitle, _itemIdList, _itemLabelList, _itemValueList, _initValue, _showSubMenuValues, _options)
    {
        View.initialize ();
        id = _id;
        if(id == :MainMenu) {
            onMenuTicks = $.appTimerTicks;
        }
        itemValueList = _itemValueList;
        initValue = _initValue;
        showSubMenuValues = _showSubMenuValues;
        itemIdList = _itemIdList;
        options = _options;


        if(_itemLabelList instanceof String) {
            itemLabelList = Application.getApp().split(_itemLabelList,'|');
        } else {
            itemLabelList = _itemLabelList;
        }

        // determine number of items as minimum size of all item arrays
        nItems = itemIdList!= null? itemIdList.size() : 101;
        if(itemLabelList != null && itemLabelList.size() < nItems) {
            nItems = itemLabelList.size();
        }
        if(itemValueList != null && itemValueList.size() < nItems) {
            nItems = itemValueList.size();
        }

        // add index as default value in case no value list has been provided
        if(showSubMenuValues == 0 && itemValueList == null) {
            itemValueList = [];
            for(var i = 0; i < nItems; i++) {
                itemValueList.add(i);
            }
        }

        // derive labels from values if set
        if(itemLabelList == null) {
            itemLabelList = [];
            for(var i = 0; i < nItems; i++) {
                itemLabelList.add(itemValueList[i].toString());
            }
        }

        title = _menuTitle;

        index = 0;
        nextIndex = 0;
    }

    function getValueFromIndex(i) {
        return itemValueList[i];
    }

    function getSelectedValue() {
        return itemValueList[index];
    }

    function getSelectedId() {
        return itemIdList[index];
    }

    function getSelectedLabel() {
        return itemLabelList[index];
    }

    function getIndexForValue(value) {
       if(itemValueList != null) {
           for(var i=0; i < itemValueList.size(); i +=1) {
               if(itemValueList[i] == value) {
                   return i;
               }
            }
        }
        return 0;
    }

    function updateIndex (offset)
    {
        if (nItems <= 1)
        {
            return;
        }

        nextIndex = index + offset;

        // Cope with a 'feature' in modulo operator not handling -ve numbers as desired.
        nextIndex = nextIndex < 0 ? nItems + nextIndex : nextIndex;

        nextIndex = nextIndex % nItems;

        Ui.requestUpdate();
        index = nextIndex;
    }

    function selectedItemIndex ()
    {
        return index;
    }

    function onShow() {
        if(showSubMenuValues == 1) {
            itemValueList = MenuDelegates.getValueLabelsForItems(itemIdList, options);
        }
    }

    function onUpdate (dc)
    {
        var width = dc.getWidth ();
        var height = dc.getHeight ();
        menuHeight = height;
        dc.setColor(Gfx.COLOR_WHITE, Gfx.COLOR_BLACK);
        dc.fillRectangle(0, 0, width, height);

        drawMenu (dc);

        // Draw the decorations.
        // adjusted heights (active item 42% of height)
        var h31 = 0.29 * height;
        var h32 = height - h31;

        dc.setColor(Gfx.COLOR_BLACK, Gfx.COLOR_WHITE);
        dc.setPenWidth (2);
        dc.drawLine (0, h31, width, h31);
        dc.drawLine (0, h32, width, h32);

        drawArrows (dc);
    }

    function drawMenu (dc)
    {
        var width = dc.getWidth ();
        var height = dc.getHeight ();
        var h3 = height / 3;

        // y for the middle of the three items.
        var y = h3;

        // Depending on where we are in the menu and in the animation some of
        // these will be unnecessary but it is easier to draw everything and
        // rely on clipping to avoid unnecessary drawing calls.
        drawTitle (dc, y - nextIndex * h3 - h3);
        for (var i = -2; i < 3; i++)
        {
            drawItem (dc, nextIndex + i, y + h3 * i, i == 0);
        }
    }

    function drawTitle (dc, y)
    {
        var width = dc.getWidth ();
        var height =  dc.getHeight ();
        var h3 = height / 3;

        // Check if any of the title is visible.,
        if (y < -h3)
        {
            return;
        }
        var h31 = 0.29 * height;
        var h32 = height - h31;

        dc.setColor (Gfx.COLOR_BLACK, Gfx.COLOR_WHITE);
        dc.fillRectangle (0, y, width, h31);

        if (title != null)
        {
            var dims = dc.getTextDimensions (title, TITLE_FONT);
            var h = (h31 - dims[1]) / 2;
            dc.setColor (Gfx.COLOR_WHITE, Gfx.COLOR_BLACK);
            dc.drawText (width / 2, y + h, TITLE_FONT, title, Gfx.TEXT_JUSTIFY_CENTER);
        }
    }

    // highlight is the selected menu item that can optionally show a value.
    function drawItem (dc, idx, y, highlight)
    {
        var height =  dc.getHeight ();
        var h3 = height / 3;

        // Cannot see item if it doesn't exist or will not be visible.
        if (idx < 0 || idx >= nItems || y > dc.getHeight () || y < -h3)
        {
            return;
        }

        var value = itemValueList[idx];
        //if(idx == initIndex) {
        if(value != null && value == initValue) {
            dc.setColor (Gfx.COLOR_DK_RED, Gfx.COLOR_WHITE);
        } else {
            dc.setColor (Gfx.COLOR_BLACK, Gfx.COLOR_WHITE);
        }

        var width = dc.getWidth ();
        var lab = itemLabelList[idx];
        var font = highlight? SELECTED_LABEL_FONT : LABEL_FONT;
        var labDims = dc.getTextDimensions (lab, font );
        if(labDims[0] > 0.95 * width) {
            font = LABEL_FONT;
            labDims = dc.getTextDimensions (lab, font );
        }
        var yL, yV, h;


        if ((showSubMenuValues > 0) && highlight && value != null)
        {
            // Show label and value.
            var val = value.toString ();
            var valDims = dc.getTextDimensions (val, VALUE_FONT);

            h = labDims[1] + valDims[1] + PAD;
            yL = y + (h3 - h) / 2;
            yV = yL + labDims[1] + PAD;
            dc.drawText (width / 2, yV, VALUE_FONT, val, Gfx.TEXT_JUSTIFY_CENTER);
        }
        else
        {
            yL = y + (h3 - labDims[1]) / 2;
        }

        dc.drawText (width / 2, yL, font, lab, Gfx.TEXT_JUSTIFY_CENTER);
    }


    const GAP = 5;
    const TS = 5;

    // The arrows are drawn with lines as polygons don't give different sized triangles depending
    // on their orientation.
    function drawArrows (dc)
    {
        var x = dc.getWidth () / 2;
        var y;

        dc.setPenWidth (1);
        dc.setColor(Gfx.COLOR_BLACK, Gfx.COLOR_WHITE);

        if (nextIndex != 0)
        {
            y = GAP;

            for (var i = 0; i < TS; i++)
            {
                dc.drawLine (x - i, y + i, x + i + 1, y + i);
            }
        }

        if (nextIndex != nItems - 1)
        {
            y = dc.getHeight () - TS - GAP;

            var d;
            for (var i = 0; i < TS; i++)
            {
                d = TS - 1 - i;
                dc.drawLine (x - d, y + i, x + d + 1, y + i);
            }
        }
    }
}


class ListMenuDelegate extends Ui.BehaviorDelegate
{
    hidden var menu;
    hidden var userMenuDelegate;

    function initialize (_menu, _userMenuInputDelegate)
    {
        menu = _menu;
        userMenuDelegate = _userMenuInputDelegate;
        BehaviorDelegate.initialize ();
    }

    function onSwipe(swipeEvent)
    {
        var d = swipeEvent.getDirection();
        if (d == WatchUi.SWIPE_UP)
        {
            return onNextPage();
        }
        if (d == WatchUi.SWIPE_DOWN)
        {
            return onPreviousPage();
        }

        return false;

    }

    function onTap(clickEvent)
    {
        var c = clickEvent.getCoordinates();
        var t = clickEvent.getType();

        if (t == WatchUi.CLICK_TYPE_TAP)
        {
            if (menu.menuHeight != null)
            {
                var h = menu.menuHeight;
                if (c[1] > h * 0.71)
                {
                    return onNextPage();
                }
                else if (c[1] < h * 0.29)
                {
                    return onPreviousPage();
                }
            }

            userMenuDelegate.onMenuItem (menu.selectedItemIndex ());
            Ui.requestUpdate();
            return true;
        }
        return false;

    }


    function onNextPage()
    {
        menu.updateIndex (1);
        return true;
    }

    function onPreviousPage ()
    {
        // wait a bit to avoid that this event is handled in case the menu is activated by a long press of the menu button (this happens on FR235)
        if($.appTimerTicks - menu.onMenuTicks > 2) {
            menu.updateIndex (-1);
        }
        return true;
    }

    function onSelect ()
    {
        return false;
    }

    function onKey(keyEvent) {
        var k = keyEvent.getKey();

        if (k == WatchUi.KEY_START || k == WatchUi.KEY_ENTER )
        {
            userMenuDelegate.onMenuItem (menu.selectedItemIndex ());
            Ui.requestUpdate();
            return true;
        }
        return false;
    }

    function onBack ()
    {
        Ui.popView (Ui.SLIDE_RIGHT);
        return true;
    }
}
