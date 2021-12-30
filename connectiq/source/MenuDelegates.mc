using Toybox.WatchUi;
using Toybox.System;
using Toybox.Lang;
using Toybox.ActivityRecording;
using Toybox.Application;

using Track;

module MenuDelegates {

    var app = Application.getApp();

    function pushSubMenuView(menu) {
        WatchUi.pushView(menu, new ListMenuDelegate (menu, new SubMenuDelegate(menu)), WatchUi.SLIDE_IMMEDIATE);
    }

    function getMenuItem(id, idx, options){
        switch(id) {
            case :autolap:
                return [["off","100m","200m","400m","500m","1km","2km","5km"],
                        [0.0,100.0,200.0,400.0,500.0,1000.0,2000.0,5000.0],
                        Track.autolapDistance];
            case :bc_number:
                return [null, [5,10,20,50,100,200,500], Track.breadCrumbNumber];
            case :bc_distance:
                return [["off","50m","100m","200m","500m","1km","2km","5km","10km","20km"],
                        [0.0,50.0,100.0,200.0,500.0,1000.0,2000.0,5000.0,10000.0,20000.0],
                        Track.breadCrumbDist];
            case :orient:
                return [WatchUi.loadResource(Rez.Strings.orient_opts),
                        null,
                        Track.getOrientation()];
            case :activity:
                return [WatchUi.loadResource(Rez.Strings.activities),
                        [ActivityRecording.SPORT_GENERIC, ActivityRecording.SPORT_RUNNING,
                         ActivityRecording.SPORT_WALKING, ActivityRecording.SPORT_CYCLING],
                         $.activityType];
            case :background:
                return [WatchUi.loadResource(Rez.Strings.color_opts),
                        [true, false], $.isDarkMode];
            case :data_field:
                if(idx == 0) {
                    return  [null, [0,1,2,3,4], Data.getField(options[0], idx)];
                } else {
                    return [Data.dataFieldMenuLabels, null, Data.getField(options[0], idx)];
                }
            case :track_update:
                return [["1s", "2s", "5s", "10s", "15s", "30s", "60s"],
                        [1,2,5,10,15,30,60],
                        $.trackViewPeriod];
            case :track_large_font:
                return [WatchUi.loadResource(Rez.Strings.onoff_opts),
                        [true, false],
                        $.trackViewLargeFont];
            case :track_profile:
                return [WatchUi.loadResource(Rez.Strings.onoff_opts),
                        [true, false],
                        $.trackElevationPlot];
            case :track_storage:
                return [WatchUi.loadResource(Rez.Strings.onoff_opts),
                        [true, false],
                        $.trackStorage];
        }
    }

    function getValueLabelsForItems(ids, options) {
        var labels = [];
        for(var i = 0; i < ids.size(); i++) {
            var label = null;
            if(ids[i] != null) {
                var entry = getMenuItem(ids[i], i, options);
                if(entry != null) {
                    var mlabels = entry[0];
                    if(entry[0] instanceof Lang.String) {
                        mlabels = app.split(entry[0],'|');
                    }
                    var values = entry[1];
                    var j;
                    if(values == null && mlabels instanceof Lang.Array ) {
                        values = [];
                        for(j = 0; j < mlabels.size(); j++) {
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
                        if(mlabels != null) {
                            label = mlabels[j];
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

        function initialize(menu) {
            MenuInputDelegate.initialize();
            self.menu = menu;
        }


        function onMenuItem(item) {
            var defaultValue = null;
            var showValue = 0;
            var valueList = null;
            var labelList = null;
            var idList = null;
            var newMenu;

            var menuItemId = menu.getSelectedId();

            var entry = getMenuItem(menuItemId, 0, null);
            if(entry != null) {
                labelList = entry[0];
                valueList = entry[1];
                defaultValue = entry[2];
            } else {
                switch(menuItemId) {
                    case :info:
                        showValue = 2;
                        labelList =  app.split(WatchUi.loadResource(Rez.Strings.info_labels),'|');
                        valueList = [WatchUi.loadResource(Rez.Strings.AppVersion)];
                        if(Track.hasTrackData) {
                            valueList.add(Track.name);
                            valueList.add((0.001 * Track.length).format("%.2f") + " km");
                            valueList.add(Track.nPoints);
                            if(Track.hasElevationData) {
                                valueList.add(Track.eleMin.format("%.1f") + " m");
                                valueList.add(Track.eleMax.format("%.1f") + " m");
                                valueList.add(Track.eleTotAscent.format("%.1f") + " m");
                                valueList.add(Track.eleTotDescent.format("%.1f") + " m");
                            }
                        }
                        while(valueList.size() < labelList.size() ) {
                            // fill with n/a
                            valueList.add("n/a");
                        }
                        break;
                    case :breadcrumbs:
                        showValue = 1;
                        idList = [:bc_set, :bc_clear, :bc_number, :bc_distance];
                        labelList = WatchUi.loadResource(Rez.Strings.bc_labels);
                        break;
                    case :screens:
                        labelList = WatchUi.loadResource(Rez.Strings.ds_labels);
                        break;
                    case :course:
                        showValue = 1;
                        idList = [:track_large_font, :track_profile, :track_storage, :track_update, :track_del];
                        labelList = WatchUi.loadResource(Rez.Strings.course_labels);
                        break;
                    default:
                        return false;
                }
            }
            newMenu = new ListMenu(menuItemId, menu.getSelectedLabel(), idList, labelList, valueList, defaultValue, showValue, null);
            pushSubMenuView(newMenu);
            return true;
        }
    }

    class SubMenuDelegate extends WatchUi.MenuInputDelegate {
        hidden var menu;

        function initialize(menu) {
            MenuInputDelegate.initialize();
            self.menu = menu;
        }

        function onMenuItem(item) {
            var value = menu.getSelectedValue();
            var newMenu;
            var entry = [];

            switch(menu.id) {
                case :orient:
                    if(value == 0) {
                        Track.northHeading = false;
                        Track.centerMap = false;
                    } else {
                        Track.northHeading = true;
                        Track.centerMap = (value == 2);
                    }
                    app.setProperty("northHeading", Track.northHeading);
                    app.setProperty("centerMap", Track.centerMap);
                    break;
                case :background:
                    $.isDarkMode = value;
                    app.setProperty("isDarkMode", $.isDarkMode);
                    if($.trackView != null) {
                        trackView.setDarkMode($.isDarkMode);
                    }
                    if($.dataView != null) {
                        dataView.setDarkMode($.isDarkMode);
                    }
                    break;
                case :autolap:
                    Track.setAutolapDistance(value);
                    app.setProperty("autolapDistance",value);
                    break;
                case :breadcrumbs:
                    switch(menu.getSelectedId()) {
                        case :bc_set:
                            Track.putBreadcrumbLastPosition();
                            break;
                        case :bc_clear:
                            Track.resetBreadCrumbs(null);
                            break;
                        case :bc_number:
                        case :bc_distance:
                            entry = getMenuItem(menu.getSelectedId(), 0, null);
                            newMenu = new ListMenu(menu.getSelectedId(), menu.getSelectedLabel(), null,
                                entry[0], entry[1], entry[2], 0, null);
                            pushSubMenuView(newMenu);
                            return true;
                    }
                    break;
                case :bc_distance:
                    Track.breadCrumbDist = value;
                    app.setProperty("breadCrumbDist", Track.breadCrumbDist);
                    break;
                case :bc_number:
                    Track.resetBreadCrumbs(value);
                    app.setProperty("breadCrumbNumber", Track.breadCrumbNumber);
                    break;
                case :activity:
                    $.activityType = value;
                    app.setProperty("activityType", $.activityType);
                    Data.setMaxHeartRate();
                    break;
                case :course:
                    switch(menu.getSelectedId()) {
                        case :track_update:
                        case :track_large_font:
                        case :track_profile:
                        case :track_storage:
                            entry = getMenuItem(menu.getSelectedId(), 0, null);
                            newMenu = new ListMenu(menu.getSelectedId(), menu.getSelectedLabel(), null,
                                    entry[0], entry[1], entry[2], 0, null);
                            pushSubMenuView(newMenu);
                            return true;
                        case :track_del:
                            if(Track.hasTrackData) {
                                var message = WatchUi.loadResource(Rez.Strings.msg_delete_track);

                                var dialog = new WatchUi.Confirmation(message);
                                WatchUi.pushView(
                                    dialog,
                                    new DeleteConfirmationDelegate(),
                                    WatchUi.SLIDE_IMMEDIATE
                                );
                            }
                            return true;
                    }
                case :track_update:
                    $.trackViewPeriod = value;
                    app.setProperty("trackViewPeriod", $.trackViewPeriod);
                    break;
                case :track_large_font:
                    $.trackViewLargeFont = value;
                    app.setProperty("trackViewLargeFont", $.trackViewLargeFont);
                    break;
                case :track_profile:
                    $.trackElevationPlot = value;
                    app.setProperty("trackElevationPlot", $.trackElevationPlot);
                    break;
                 case :track_storage:
                    $.trackStorage = value;
                    app.setProperty("trackStorage", $.trackStorage);
                    break;
                case :screens:
                    if(value == 0) {
                        Data.setDataScreens(Data.dataScreensDefault);
                        app.setProperty("dataScreens", Data.dataScreens);
                        WatchUi.popView(WatchUi.SLIDE_IMMEDIATE);
                        return true;
                    }
                    // value denotes screen
                    newMenu = new ListMenu(:dataScreen,  menu.getSelectedLabel(),
                            [:data_field, :data_field, :data_field, :data_field, :data_field],
                            WatchUi.loadResource(Rez.Strings.dfm_labels),
                            null, null, 1, [value-1]);
                    pushSubMenuView(newMenu);
                    return true;
                case :dataScreen:
                    entry = getMenuItem(menu.getSelectedId(),  menu.selectedItemIndex(), [menu.options[0]]);
                    newMenu = new ListMenu(:data_field,  menu.getSelectedLabel(), null, entry[0], entry[1], entry[2], 0,  [menu.options[0], menu.selectedItemIndex()]);
                    pushSubMenuView(newMenu);
                    return true;
                case :data_field:
                    Data.setField(menu.options[0], menu.options[1], value);
                    app.setProperty("dataScreens",Data.dataScreens);
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
                Track.deleteTrack();
                app.clearTrackStorage();
            }
        }
    }
}
