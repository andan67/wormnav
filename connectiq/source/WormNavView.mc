using Toybox.WatchUi;
using Transform;
using Trace;

class WormNavView extends WatchUi.View {

    var screenShape;

    var cursorSizePixel;

    var posCursor;

    var isNewTrack=false;

    var activity_values;

    function draw_positions(dc) {
        dc.setColor(Graphics.COLOR_BLUE, Graphics.COLOR_TRANSPARENT);
        var xy_pos;

        if(Transform.northHeading) {
            for(var i=0; i < Trace.pos_nelements; i+=1) {
                var j = (Trace.pos_start_index +i) % Trace.BUFFER_SIZE;
                xy_pos = Transform.xy_2_screen(Trace.x_array[j], Trace.y_array[j]);
                dc.fillCircle(xy_pos[0],xy_pos[1] , 3);
            }
        }
        else {
           for(var i=0; i < Trace.pos_nelements; i+=1) {
                var j = (Trace.pos_start_index +i) % Trace.BUFFER_SIZE;
                xy_pos = Transform.xy_2_rot_screen(Trace.x_array[j], Trace.y_array[j]);
                dc.fillCircle(xy_pos[0],xy_pos[1] , 3);
           }
        }

    }


    function draw_scale(dc) {
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(2);

        dc.drawLine(Transform.scale_x1,Transform.scale_y1,Transform.scale_x1,Transform.scale_y2);
        dc.drawLine(Transform.scale_x1,Transform.scale_y2,Transform.scale_x2,Transform.scale_y2);
        dc.drawLine(Transform.scale_x2,Transform.scale_y2,Transform.scale_x2,Transform.scale_y1);
        dc.drawText(Transform.pixelWidth2, Transform.scale_y2-dc.getFontHeight( Graphics.FONT_MEDIUM ),
            Graphics.FONT_MEDIUM , Transform.formatScale(Transform.refScale), Graphics.TEXT_JUSTIFY_CENTER);

    }


    function draw_activity_info(dc) {
        var data;
        dc.setColor(Graphics.COLOR_BLACK, Graphics.COLOR_TRANSPARENT);
        if(session.isRecording() && Activity.getActivityInfo()!=null) {
            if( Activity.getActivityInfo().elapsedDistance!= null) {
               data = Activity.getActivityInfo().elapsedDistance/1000;
               if(data == null) {
                   activity_values[0] = "--";
               }
               else {
                    activity_values[0] = "Dist.: " + data.format("%.2f");
               }

            }
            if(Activity.getActivityInfo().elapsedTime!=null) {
               data = Activity.getActivityInfo().timerTime;
                if(data == null) {
                    activity_values[1] = "--";
                }
                else {
                    //activity_values[1] = "T: " + Utils.printTime(data);
                     activity_values[1] = "Time: " + Utils.msToTime(data);
                }
            }
        }
        var y = 0.5*dc.getFontAscent(Graphics.FONT_MEDIUM);
        dc.drawText(Transform.pixelWidth2, y, Graphics.FONT_MEDIUM , activity_values[0], Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
        dc.drawText(Transform.pixelWidth2, 3*y, Graphics.FONT_MEDIUM , activity_values[1], Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
    }


    function draw_track(dc) {
        dc.setColor(Graphics.COLOR_RED, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(2);


        var xy_pos1;
        var xy_pos2;

        var xya = $.track.xyArray;

        var step = 2;

       var i=0;
       if(Transform.northHeading || Transform.isTrackCentered) {
           xy_pos1 = Transform.xy_2_screen(xya[i],xya[i+1]);
           xy_pos2 = Transform.xy_2_screen(xya[i+step],xya[i+step+1]);
       } else {
           xy_pos1 = Transform.xy_2_rot_screen(xya[i],xya[i+1]);
           xy_pos2 = Transform.xy_2_rot_screen(xya[i+step],xya[i+step+1]);
       }
       dc.drawLine(xy_pos1[0],xy_pos1[1],xy_pos2[0],xy_pos2[1]);
       for(i = step; i < xya.size()-step-1; i+=step ) {
            xy_pos1 = xy_pos2;
            if(Transform.northHeading || Transform.isTrackCentered) {
                xy_pos2 = Transform.xy_2_screen(xya[i+step],xya[i+step+1]);
            }
            else {
                xy_pos2 = Transform.xy_2_rot_screen(xya[i+step],xya[i+step+1]);
            }
            dc.drawLine(xy_pos1[0],xy_pos1[1],xy_pos2[0],xy_pos2[1]);
        }

    }

    function draw_trace(dc) {

        draw_positions(dc);

        dc.setColor(Graphics.COLOR_BLUE, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(1);

        var sf = 1.8*cursorSizePixel;

        var heading;
        var dx1;
        var dy1;
        var dx2;
        var dy2;
        var dx3;
        var dy3;

        if(Transform.northHeading) {
            heading = Transform.heading_smooth;
            dx1= cursorSizePixel*Transform.sin_heading_smooth;
            dy1=-cursorSizePixel*Transform.cos_heading_smooth;
        }
        else {
            heading = 0;
            dx1=0;
            dy1=-cursorSizePixel;
        }

        dx2= sf*Math.sin(heading+Transform.ANGLE_R);
        dy2=-sf*Math.cos(heading+Transform.ANGLE_R);
        dx3= sf*Math.sin(heading+Transform.ANGLE_L);
        dy3=-sf*Math.cos(heading+Transform.ANGLE_L);

        var xy_pos = Transform.xy_2_screen(Transform.x_pos, Transform.y_pos);

        dc.drawCircle(xy_pos[0], xy_pos[1], 3);

        dc.setPenWidth(3);
        dc.drawLine(xy_pos[0]+dx1, xy_pos[1]+dy1, xy_pos[0]+dx2, xy_pos[1]+dy2);
        dc.drawLine(xy_pos[0]+dx2, xy_pos[1]+dy2, xy_pos[0]-dx1, xy_pos[1]-dy1);
        dc.drawLine(xy_pos[0]-dx1, xy_pos[1]-dy1, xy_pos[0]+dx3, xy_pos[1]+dy3);
        dc.drawLine(xy_pos[0]+dx3, xy_pos[1]+dy3, xy_pos[0]+dx1, xy_pos[1]+dy1);

        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_WHITE);
        dc.fillCircle(Transform.compass_x,Transform.compass_y,Transform.compass_size);
        if(Transform.northHeading) {
            dx1 = - 0.5*Transform.compass_size;
            dx2 = 0;
            dx3 = -dx1;
            dy1 = 0;
            dy2 = - Transform.compass_size;
            dy3 = 0;
        } else {
            dx1 = -0.5*Transform.compass_size*Transform.cos_heading_smooth;
            dy1 = +0.5*Transform.compass_size*Transform.sin_heading_smooth;
            dx2 = -Transform.compass_size*Transform.sin_heading_smooth;
            dy2 = -Transform.compass_size*Transform.cos_heading_smooth;
            dx3 =  -dx1;
            dy3 =  -dy1;
        }

        dc.setColor(Graphics.COLOR_BLUE, Graphics.COLOR_TRANSPARENT);
        var points = [[Transform.compass_x + dx1, Transform.compass_y + dy1],
                      [Transform.compass_x - dx2, Transform.compass_y - dy2],
                      [Transform.compass_x + dx3, Transform.compass_y + dy3]];
        dc.fillPolygon(points);

        dc.setColor(Graphics.COLOR_RED, Graphics.COLOR_TRANSPARENT);
        points = [[Transform.compass_x + dx1, Transform.compass_y + dy1],
                  [Transform.compass_x + dx2, Transform.compass_y + dy2],
                  [Transform.compass_x + dx3, Transform.compass_y + dy3]];
        dc.fillPolygon(points);
    }

    function initialize() {
        System.println("initialize()");
        View.initialize();
        Trace.reset();
        activity_values = new[2];
    }


    // Load your resources here
    function onLayout(dc) {
        System.println("onLayout(dc)");
        screenShape = System.getDeviceSettings().screenShape;
        Transform.setPixelDimensions(dc.getWidth(), dc.getHeight());
        cursorSizePixel=Transform.pixelWidth*Transform.SCALE_PIXEL*0.5;
    }

    // Called when this View is brought to the foreground. Restore
    // the state of this View and prepare it to be shown. This includes
    // loading resources into memory.
    function onShow() {
        System.println("onShow()");
        View.onShow();
        if(track==null) {
            Transform.setZoomLevel(5);
        }
    }


    // Update the view
    function onUpdate(dc) {
        // Call the parent onUpdate function to redraw the layout
        //View.onUpdate(dc);
        dc.setColor(Graphics.COLOR_WHITE, Graphics.COLOR_WHITE);
        dc.clear();

        if(isNewTrack && track!=null) {
            isNewTrack = false;
            Transform.newTrack();
        }

        if(track!=null) {
            draw_track(dc);
        }

        if(Transform.x_pos != null) {
            draw_trace(dc);
        }

        if(session!=null) {
            draw_activity_info(dc);
        }

        draw_scale(dc);
    }

    function setPosition(info) {
        Transform.isTrackCentered = false;
        Transform.setPosition(info);
        WatchUi.requestUpdate();
    }

    // Called when this View is removed from the screen. Save the
    // state of this View here. This includes freeing resources from
    // memory.
    function onHide() {
    }

}
