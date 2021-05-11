
using Toybox.WatchUi;
using Toybox.System;
using Toybox.Lang;
using Toybox.ActivityRecording;
using Toybox.Application;

using Trace;

module MenuDelegates {


    function getMenuItem(id, idx, options){
        switch(id) {
            case :autolap:
                return [["off","100m","200m","400m","500m","1km","2km","5km"],
                        [0.0,100.0,200.0,400.0,500.0,1000.0,2000.0,5000.0],
                        Trace.autolapDistance];
            case :bc_number:
                return [null, [1,2,5,10,20,50,100], Trace.breadCrumbNumber];
            case :bc_distance:
                return [["off","50m","100m","200m","500m","1km","2km","5km","10km","20km"],
                        [0.0,50.0,100.0,200.0,500.0,1000.0,2000.0,5000.0,10000.0,20000.0],
                        Trace.breadCrumbDist];
            case :orient:
                return [WatchUi.loadResource(Rez.Strings.orient_opts),
                        null,
                        Transform.getOrientation()];
            case :activity:
                return [WatchUi.loadResource(Rez.Strings.activities),
                        [ActivityRecording.SPORT_GENERIC, ActivityRecording.SPORT_RUNNING,
                         ActivityRecording.SPORT_WALKING, ActivityRecording.SPORT_CYCLING],
                         $.activityType];
            case :background:
                return [WatchUi.loadResource(Rez.Strings.color_opts),
                        [true, false], $.isDarkMode];
            case :track_update:
                return [["1s", "2s", "5s", "10s", "15s", "30s", "60s"],
                        [1,2,5,10,15,30,60],
                        $.trackViewPeriod];
            case :datascreen_nfields:
                return [null, [0,1,2,3,4], Data.getField(options[0], idx)];
            case :datascreen_field:
                return [Data.dataFieldMenuLabels, null, Data.getField(options[0], idx)];
            default:
                return null;
        }
    }

    function getValueLabelsForItems(ids, options) {
        var labels = [];
        for(var i = 0; i < ids.size(); i++) {
            var label = null;
            if(ids[i] != null) {
                var entry = getMenuItem(ids[i], i, options);
                if(entry != null) {
                    var labels = entry[0];
                    if(entry[0] instanceof Lang.String) {
                        labels = Application.getApp().split(entry[0],'|');
                    }
                    var values = entry[1];
                    var j;
                    if(values == null && labels instanceof Lang.Array ) {
                        values = [];
                        for(j = 0; j < labels.size(); j++) {
                            values.add(j);
                        }
                    }
                    var value = entry[2];
                    if(values != null &&  value != null) {
                        for(j = 0; j < values.size(); j++) {
                            if(values[j] ==  value) {
                                break;
                            }
                        }
                        if(labels != null) {
                            label = labels[j];
                        } else {
                            label = values[j].toString();
                        }
                    }
                }
            }
            labels.add(label);
        }
        return labels;
    }

    class MainMenuDelegate extends WatchUi.MenuInputDelegate {

        hidden var menu;

        function initialize(_menu) {
            MenuInputDelegate.initialize();
            menu = _menu;
        }

        function onMenuItem(item) {
            var defaultValue = null;
            var showValue = false;
            var valueList = null;
            var labelList = null;
            var idList = null;
            var newMenu;

            var menuItemId = menu.getSelectedId();

            var entry = getMenuItem(menuItemId, 0, null);
            if(entry != null) {
                //showValue = true;
                labelList = entry[0];
                valueList = entry[1];
                defaultValue = entry[2];
            } else {
                switch(menuItemId) {
                    case :delete:
                        if(track!=null) {
                            var message = WatchUi.loadResource(Rez.Strings.msg_continue);
                            var dialog = new WatchUi.Confirmation(message);
                            WatchUi.pushView(
                                dialog,
                                new DeleteConfirmationDelegate(),
                                WatchUi.SLIDE_IMMEDIATE
                            );
                        }
                        return true;
                    case :breadcrumbs:
                        defaultValue = null;
                        showValue = true;
                        idList = [:bc_set, :bc_clear, :bc_number, :bc_distance];
                        labelList = WatchUi.loadResource(Rez.Strings.bc_labels);
                        break;
                    case :screens:
                        defaultValue = null;
                        idList = null;
                        labelList = WatchUi.loadResource(Rez.Strings.ds_labels);
                        valueList = null;
                        break;
                    case :course:
                        defaultValue = null;
                        showValue = true;
                        idList = [:track_update, :track_info, :track_del];
                        labelList = WatchUi.loadResource(Rez.Strings.course_labels);
                        break;
                    default:
                        return false;
                }
            }
            newMenu = new ListMenu(menuItemId, menu.getSelectedLabel(), idList, labelList, valueList, defaultValue, showValue, null);
            WatchUi.pushView(newMenu, new ListMenuDelegate (newMenu, new SubMenuDelegate(newMenu)), WatchUi.SLIDE_UP);
            return true;
        }
    }

    class SubMenuDelegate extends WatchUi.MenuInputDelegate {
        hidden var menu;

        function initialize(_menu) {
            MenuInputDelegate.initialize();
            menu = _menu;
        }

        function onMenuItem(item) {
            var value = menu.getSelectedValue();
            switch(menu.id) {
                case :orient:
                    if(value == 0) {
                        Transform.northHeading = false;
                        Transform.centerMap = false;
                    } else {
                        Transform.northHeading = true;
                        Transform.centerMap = (value == 2);
                    }
                    Application.getApp().setProperty("northHeading", Transform.northHeading);
                    Application.getApp().setProperty("centerMap", Transform.centerMap);
                    break;
                case :background:
                    $.isDarkMode = value;
                    Application.getApp().setProperty("darkMode", $.isDarkMode);
                    if($.trackView != null) {
                        trackView.setDarkMode($.isDarkMode);
                    }
                    if($.dataView != null) {
                        dataView.setDarkMode($.isDarkMode);
                    }
                    break;
                case :autolap:
                    Trace.setAutolapDistance(value);
                    Application.getApp().setProperty("autolapDistance",value);
                    break;
                case :breadcrumbs:
                    switch(menu.getSelectedId()) {
                        case :bc_set:
                            Trace.putBreadcrumbLastPosition();
                            break;
                        case :bc_clear:
                            Trace.reset();
                            break;
                        case :bc_number:
                        case :bc_distance:
                            var entry = getMenuItem(menu.getSelectedId(), 0, null);
                            var newMenu = new ListMenu(menu.getSelectedId(), menu.getSelectedLabel(), null,
                                    entry[0], entry[1], entry[2], false, null);
                            WatchUi.pushView(newMenu, new ListMenuDelegate (newMenu, new SubMenuDelegate(newMenu)), WatchUi.SLIDE_UP);
                            return true;
                    }
                    break;
                case :bc_distance:
                    Trace.breadCrumbDist = value;
                    Application.getApp().setProperty("breadCrumbDist", Trace.breadCrumbDist);
                    break;
                case :bc_number:
                    Trace.setBreadCrumbNumber(value);
                    Application.getApp().setProperty("breadCrumbNumber", Trace.breadCrumbNumber);
                    break;
                case :activity:
                    $.activityType = value;
                    Application.getApp().setProperty("activityType", $.activityType);
                    Data.setMaxHeartRate();
                    break;
                case :course:
                    var newMenu;
                    switch(menu.getSelectedId()) {
                        case :track_update:
                            var entry = [];
                            entry = getMenuItem(menu.getSelectedId(), 0, null);
                            newMenu = new ListMenu(menu.getSelectedId(), menu.getSelectedLabel(), null,
                                    entry[0], entry[1], entry[2], false, null);
                            WatchUi.pushView(newMenu, new ListMenuDelegate (newMenu, new SubMenuDelegate(newMenu)), WatchUi.SLIDE_UP);
                            return true;
                        case :track_del:
                            if(track!=null) {
                                var message = menu.getSelectedId();

                                var dialog = new WatchUi.Confirmation(message);
                                WatchUi.pushView(
                                    dialog,
                                    new DeleteConfirmationDelegate(),
                                    WatchUi.SLIDE_IMMEDIATE
                                );
                            }
                            return true;
                        case :track_info:
                            if(track!=null) {
                                WatchUi.pushView(new TrackInfoView(),new TrackInfoDelegate(), WatchUi.SLIDE_IMMEDIATE);
                            }
                            return true;
                    }
                case :track_update:
                    $.trackViewPeriod = value;
                    Application.getApp().setProperty("trackViewPeriod", $.trackViewPeriod);
                    break;
                case :screens:
                    if(value == 0) {
                        Data.setDataScreens(Data.dataScreensDefault);
                        Application.getApp().setProperty("dataScreens", Data.dataScreens);
                        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
                        return true;
                    }
                    // value denotes screen
                    newMenu = new ListMenu(:dataScreen,  menu.getSelectedLabel(),
                            [:datascreen_nfields, :datascreen_field, :datascreen_field, :datascreen_field, :datascreen_field],
                            WatchUi.loadResource(Rez.Strings.dfm_labels),
                            null, null, true, [value-1]);
                    WatchUi.pushView(newMenu, new ListMenuDelegate (newMenu, new SubMenuDelegate (newMenu)), WatchUi.SLIDE_UP);
                    return true;
                case :dataScreen:
                    var defaultValue = Data.getField(menu.options[0], menu.selectedItemIndex());
                    var newOptions = [menu.options[0], menu.selectedItemIndex()];
                    if(menu.selectedItemIndex() == 0) {
                        newMenu = new ListMenu(:datascreen_nfields,  menu.getSelectedLabel(), null, null, [0,1,2,3,4],
                            defaultValue, false, newOptions);
                    } else {
                        newMenu = new ListMenu(:datascreen_fields,  menu.getSelectedLabel(), null, Data.dataFieldMenuLabels, null,
                            defaultValue, false, newOptions);
                    }
                    WatchUi.pushView(newMenu, new ListMenuDelegate (newMenu, new SubMenuDelegate (newMenu)), WatchUi.SLIDE_UP);
                    return true;
                case :datascreen_nfields:
                case :datascreen_fields:
                    Data.setField(menu.options[0], menu.options[1], value);
                    Application.getApp().setProperty("dataScreens",Data.dataScreens);
                    break;
                default:
                    break;
            }
            WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
            return true;
        }
    }

    class DeleteConfirmationDelegate extends WatchUi.ConfirmationDelegate {

        function initialize() {
            ConfirmationDelegate.initialize();
        }

        function onResponse(response) {
            if (response == WatchUi.CONFIRM_NO) {
            } else {
                $.track=$.track.clean();
                Transform.setViewCenter(Trace.lat_last_pos,Trace.lon_last_pos);
                $.track=null;
                //Trace.reset();
            }
        }
    }

    class TrackInfoDelegate extends WatchUi.BehaviorDelegate {

        function initialize() {
            BehaviorDelegate.initialize();
        }

        function onBack() {
            WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
        }
    }
}
