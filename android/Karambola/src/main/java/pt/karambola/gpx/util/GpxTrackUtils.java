/*
 * GpxTrackUtils.java
 *
 * Copyright (c) 2016 Karambola. All rights reserved.
 *
 * This file is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3.0
 * of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this file; if not, write to the
 * Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package pt.karambola.gpx.util;

import java.util.ArrayList;
import java.util.List;

import pt.karambola.gpx.beans.Route;
import pt.karambola.gpx.beans.RoutePoint;
import pt.karambola.gpx.beans.Track;
import pt.karambola.gpx.beans.TrackPoint;
import pt.karambola.gpx.parser.DefaultGpxBeanFactory;
import pt.karambola.gpx.parser.GpxBeanFactory;

public class
GpxTrackUtils
{
    public static final GpxTrackUtils INSTANCE = new GpxTrackUtils( ) ;

    private final GpxBeanFactory	beanFactory ;

    public
    GpxTrackUtils( final GpxBeanFactory beanFactory )
	{
		this.beanFactory = beanFactory ;
	}

    public
    GpxTrackUtils( )
	{
		this( DefaultGpxBeanFactory.INSTANCE ) ;
	}


    /**
	 * One-to-one conversion of a track to a route. All track segments are joined, no track-point is discarded.
	 */
	public
	Route
	convertTrack( final Track track )
	{
		final Route route = beanFactory.newRoute( ) ;

		route.setName       ( track.getName()        ) ;
		route.setType       ( track.getType()        ) ;
		route.setDescription( track.getDescription() ) ;
		route.setComment    ( track.getComment()     ) ;
		route.setSrc        ( track.getSrc( ) != null ? track.getSrc( ) : GpxUtils.CREDITS ) ;

		final List<RoutePoint>	routePoints	= new ArrayList<>() ;

		for (TrackPoint trackPoint: track.getTrackPoints())
		{
			final RoutePoint routePoint = beanFactory.newRoutePoint( ) ;

			routePoint.setLatitude   ( trackPoint.getLatitude()    ) ;
			routePoint.setLongitude  ( trackPoint.getLongitude()   ) ;
			routePoint.setElevation  ( trackPoint.getElevation()   ) ;
			routePoint.setName       ( trackPoint.getName()        ) ;
			routePoint.setType       ( trackPoint.getType()        ) ;
			routePoint.setTime       ( trackPoint.getTime()        ) ;
			routePoint.setDescription( trackPoint.getDescription() ) ;
			routePoint.setComment    ( trackPoint.getComment()     ) ;

			routePoints.add( routePoint ) ;
		}

		route.setRoutePoints( routePoints ) ;

		return route ;
	}
}
