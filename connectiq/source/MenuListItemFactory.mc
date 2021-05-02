using Toybox.Graphics;
using Toybox.WatchUi;
using Toybox.Lang;
using Toybox.Application;

class MenuItemList {
    hidden var symbolList;
    hidden var itemValueList;
    hidden var itemLabelList;
    hidden var symbValueMap;

    function initialize(_symbolList, _itemValueList, _itemLabelList) {
        System.println("MenuItemList: " + _symbolList.size() );
        System.println("MenuItemList: " + _itemValueList.size() );
        System.println("MenuItemList: " + _symbolList instanceof Array );
        System.println("MenuItemList: " +_itemValueList instanceof Array );

        if(_symbolList instanceof Array && _itemValueList instanceof Array && _symbolList.size() == _itemValueList.size() ) {
            symbolList = _symbolList;
            itemValueList = _itemValueList;
            symbValueMap = {};
            for(var i=0; i < symbolList.size(); i +=1) {
                symbValueMap.put(symbolList[i], itemValueList[i] );
            }
            //System.println("MenuItemList: " +symbolList.size() );
            //System.println(symbValueMap);
            if(_itemLabelList instanceof String) {
                itemLabelList = Application.getApp().split(_itemLabelList,'|');
            } else {
                itemLabelList = _itemLabelList;
            }
            if(itemLabelList == null || itemValueList.size() != itemLabelList.size()) {
                // create label list from values
                for(var i=0; i< itemValueList.size(); i+=1) {
                    itemLabelList.add(itemValueList[i].toString());    
                }
            }
        }
    }

    function createMenu(_title, _default) {
        var menu = new WatchUi.Menu();
        menu.setTitle(_title);
        var i = 0;
        if(_default != null) {
            i = getIndex(_default);
        }
        for(var j = 0; j < itemValueList.size(); j++) {
            var k = (j + i) % itemValueList.size();
            System.println("j: " + j);
            System.println("k: " + k);
            System.println("itemLabelList[k]: " + itemLabelList[k]);
            System.println("symbolList[k]: " + symbolList[k]);
            menu.addItem(itemLabelList[k], symbolList[k]);
        }
        return menu;
    }

    function getValueFromIndex(index) {
        return itemValueList[index];
    }

    function getValueFromSymbol(_symb) {
        return symbValueMap.get(_symb);
    }
    
    function getIndex(value) {
        for(var i=0; i < itemValueList.size(); i +=1) {
            if(itemValueList[i]==value) {
                return i;
            }
        }
        return 0;
    }
    
    function getSize() {
        return symbolList.size();
    }

}
