using Toybox.Graphics;
using Toybox.WatchUi;
using Toybox.Lang;

class GenericListItemFactory extends WatchUi.PickerFactory {
    hidden var mItemValueList = [];
    hidden var mItemTextList = [];
    hidden var mFormatString;
    hidden var mFont;

    function initialize(itemValueList, itemTextList, options) {
        PickerFactory.initialize();
        
        for(var i=0; i< itemValueList.size(); i+=1) {
            switch(itemValueList[i]) {
                case instanceof String:
                case instanceof Char:
                case instanceof Number:
                case instanceof Long:
                case instanceof Boolean:
                case instanceof Float:
                case instanceof Double:
                    mItemValueList.add(itemValueList[i]);
                    if(itemTextList!=null && itemValueList.size()==itemTextList.size() && 
                        itemTextList[i] instanceof String) {
                        mItemTextList.add(itemTextList[i]);
                    } else {
                        mItemTextList.add(itemValueList[i].toString());
                    }
                    break;
                default:
                    break;
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
        return new WatchUi.Text( { :text=>mItemTextList[index], :color=>Graphics.COLOR_WHITE, :font=> mFont, :locX =>WatchUi.LAYOUT_HALIGN_CENTER, :locY=>WatchUi.LAYOUT_VALIGN_CENTER } );
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
