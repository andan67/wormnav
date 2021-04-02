using Toybox.Graphics;
using Toybox.WatchUi;
using Toybox.Lang;

class GenericListItemFactory extends WatchUi.PickerFactory {
    hidden var mItemValueList = [];
    hidden var mItemLabelList = [];
    hidden var mFormatString;
    hidden var mFont;

    function initialize(itemValueList, itemLabelList, options) {
        PickerFactory.initialize();
        
        // if itemLabelList is String split into array
        if(itemLabelList instanceof String) {
            itemLabelList = $.application.split(itemLabelList,'|');
        }

        if(itemValueList != null) {
            mItemValueList = itemValueList;
            if(itemLabelList == null || itemValueList.size() != itemLabelList.size()) {
                // create label list from values
                for(var i=0; i< itemValueList.size(); i+=1) {
                    mItemLabelList.add(mItemValueList[i].toString());    
                }
            } else {
                mItemLabelList = itemLabelList;
            }
        } else {
            if(itemLabelList != null) {
                mItemLabelList = itemLabelList;
                for(var i=0; i< itemLabelList.size(); i+=1) {
                    mItemValueList.add(i); 
                }
            }
        }

        if(options != null) {
            mFormatString = options.get(:format);
            mFont = options.get(:font);
        }

        if(mFont == null) {
            mFont = Graphics.FONT_LARGE;
        }
    }

    function getDrawable(index, selected) {
        return new WatchUi.Text( { :text=>mItemLabelList[index], :color=>Graphics.COLOR_WHITE, :font=> mFont, :locX =>WatchUi.LAYOUT_HALIGN_CENTER, :locY=>WatchUi.LAYOUT_VALIGN_CENTER } );
    }

    function getValue(index) {
        return mItemValueList[index];
    }
    
    function getIndex(value) {
        for(var i=0; i < mItemValueList.size(); i +=1) {
            if(mItemValueList[i]==value) {
                return i;
            }
        }
        return 0;
    }
    
    function getSize() {
        return mItemValueList.size();
    }

}
